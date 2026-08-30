package io.jacksoon.filterManagement.pipeline.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.filter.FilterBundleMetadata;
import io.jacksoon.common.filter.FilterConfigDto;
import io.jacksoon.filterManagement.exception.FilterBundleException;
import io.jacksoon.filterManagement.exception.FilterCompileException;
import io.jacksoon.filterManagement.exception.InvalidFilterRequestException;
import io.jacksoon.filterManagement.store.FilterDefinition;
import io.jacksoon.init.annotation.Init;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipException;
import java.util.stream.Stream;

@Init
public class Jar {

    private static final String METADATA_ENTRY_NAME =
            "META-INF/filter-bundle.json";

    private final Path workRoot =
            Path.of("plugins/work");

    private final Path bundleRoot =
            Path.of("plugins/bundles");

    private final ObjectMapper objectMapper;

    public Jar(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Path create(FilterConfigDto config, long version) {
        validateFilter(config, version);
        Path classRoot = resolveClassRoot(config, version);
        Path jarPath = resolveJarPath(config, version);
        Path jarDirectory = jarPath.getParent();
        Path tempJarPath = jarDirectory.resolve(config.filterName() + ".jar.tmp");
        if (!Files.isDirectory(classRoot)) {
            throw new FilterCompileException("Compiled class directory does not exist. path=" + classRoot);
        }
        try {
            Files.createDirectories(jarDirectory);
            Files.deleteIfExists(tempJarPath);
            createJarFromDirectory(classRoot, tempJarPath);
            moveAtomically(tempJarPath, jarPath);
            return jarPath;
        } catch (IOException e) {
            deleteQuietly(tempJarPath);
            throw new FilterBundleException("Failed to create filter jar. path=" + jarPath, e);
        }
    }

    public Path saveUploadedJar(byte[] jarBytes, FilterConfigDto config, long version) {
        validateFilter(config, version);
        if (jarBytes == null || jarBytes.length == 0) {
            throw new InvalidFilterRequestException("Uploaded filter jar is empty");
        }

        Path jarPath = resolveJarPath(config, version);
        Path jarDirectory = jarPath.getParent();
        Path tempJarPath = jarDirectory.resolve(config.filterName() + ".jar.tmp");

        try {
            Files.createDirectories(jarDirectory);
            Files.deleteIfExists(tempJarPath);
            Files.write(tempJarPath, jarBytes);
            try (JarFile ignored = new JarFile(tempJarPath.toFile())) {
            }
            moveAtomically(tempJarPath, jarPath);
            return jarPath;
        } catch (ZipException e) {
            deleteQuietly(tempJarPath);
            throw new InvalidFilterRequestException("Uploaded filter jar is invalid", e);
        } catch (IOException e) {
            deleteQuietly(tempJarPath);
            throw new FilterBundleException("Failed to store uploaded filter jar. path=" + jarPath, e);
        }
    }

    public Path getJarPath(FilterConfigDto config, long version) {
        validateFilter(config, version);
        Path jarPath = resolveJarPath(config, version);
        if (!Files.isRegularFile(jarPath)) {
            throw new FilterBundleException("Filter jar does not exist. path=" + jarPath);
        }
        return jarPath;
    }

    public Path createBundle(Map<String, FilterDefinition> filters, long version) {
        validateBundle(filters, version);
        Path bundlePath = resolveBundlePath(version);
        Path tempBundlePath = bundleRoot.resolve("bundle-" + version + ".jar.tmp");
        try {
            Files.createDirectories(bundleRoot);
            Files.deleteIfExists(tempBundlePath);

            createBundleJar(filters, version, tempBundlePath);
            moveAtomically(tempBundlePath, bundlePath);
            return bundlePath;
        } catch (IOException e) {
            deleteQuietly(tempBundlePath);
            throw new FilterBundleException("Failed to create filter bundle. path=" + bundlePath, e);
        }
    }

    void createJarFromDirectory(Path classRoot, Path jarPath) throws IOException {

        try (
                OutputStream outputStream = Files.newOutputStream(jarPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                JarOutputStream jarOutputStream = new JarOutputStream(outputStream);
                Stream<Path> paths = Files.walk(classRoot)
        ) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .sorted(
                            Comparator.comparing(
                                    Path::toString
                            )
                    )
                    .toList();
            if (files.isEmpty()) {
                throw new FilterCompileException("No compiled class files found. path=" + classRoot);
            }
            for (Path file : files) {
                addFileToJar(classRoot, file, jarOutputStream);
            }
        }
    }

    private void createBundleJar(Map<String, FilterDefinition> filters, long version, Path bundlePath) throws IOException {

        try (
                OutputStream outputStream = Files.newOutputStream(bundlePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                JarOutputStream bundleOutput = new JarOutputStream(outputStream)
        ) {
            Set<String> addedEntries = new HashSet<>();
            List<FilterDefinition> definitions =
                    filters.values()
                            .stream()
                            .sorted(
                                    Comparator
                                            .comparingInt(
                                                    (FilterDefinition definition) ->
                                                            definition
                                                                    .config()
                                                                    .order()
                                            )
                                            .thenComparing(
                                                    definition ->
                                                            definition
                                                                    .config()
                                                                    .filterName()
                                            )
                            )
                            .toList();

            for (FilterDefinition definition : definitions) {
                mergeJar(definition.jarPath(), bundleOutput, addedEntries);
            }

            addBundleMetadata(definitions, version, bundleOutput, addedEntries);
        }
    }

    private void mergeJar(Path sourceJarPath, JarOutputStream bundleOutput, Set<String> addedEntries) throws IOException {
        if (!Files.isRegularFile(sourceJarPath)) {
            throw new FilterBundleException("Source filter jar does not exist. path=" + sourceJarPath);
        }

        try (JarFile sourceJar = new JarFile(sourceJarPath.toFile())) {
            var entries = sourceJar.entries();

            while (entries.hasMoreElements()) {
                JarEntry sourceEntry = entries.nextElement();
                if (sourceEntry.isDirectory()) {
                    continue;
                }
                String entryName = sourceEntry.getName();
                if (shouldSkipEntry(entryName)) {
                    continue;
                }
                if (!addedEntries.add(entryName)) {
                    throw new FilterBundleException("Duplicate jar entry while creating bundle: " + entryName);
                }
                JarEntry bundleEntry = new JarEntry(entryName);
                bundleEntry.setTime(0L);
                bundleOutput.putNextEntry(bundleEntry);
                try (InputStream inputStream = sourceJar.getInputStream(sourceEntry)) {
                    inputStream.transferTo(bundleOutput);
                }
                bundleOutput.closeEntry();
            }
        }
    }

    private void addBundleMetadata(List<FilterDefinition> definitions, long version, JarOutputStream bundleOutput, Set<String> addedEntries) throws IOException {

        if (!addedEntries.add(METADATA_ENTRY_NAME)) {
            throw new FilterBundleException("Duplicate bundle metadata entry");
        }

        List<FilterConfigDto> configs = definitions.stream().map(FilterDefinition::config).toList();

        FilterBundleMetadata metadata = new FilterBundleMetadata(version, configs);
        byte[] metadataBytes = objectMapper.writeValueAsBytes(metadata);
        JarEntry metadataEntry = new JarEntry(METADATA_ENTRY_NAME);
        metadataEntry.setTime(0L);
        bundleOutput.putNextEntry(metadataEntry);
        bundleOutput.write(metadataBytes);
        bundleOutput.closeEntry();
    }

    private void addFileToJar(Path root, Path file, JarOutputStream jarOutputStream) throws IOException {
        String entryName = root.relativize(file).toString().replace('\\', '/');
        JarEntry jarEntry = new JarEntry(entryName);
        jarEntry.setTime(0L);

        jarOutputStream.putNextEntry(jarEntry);
        Files.copy(file, jarOutputStream);
        jarOutputStream.closeEntry();
    }

    private boolean shouldSkipEntry(String entryName) {
        String upperEntryName = entryName.toUpperCase();

        return upperEntryName.equals("META-INF/MANIFEST.MF") ||
                upperEntryName.equals("META-INF/INDEX.LIST") ||
                upperEntryName.endsWith(".SF") ||
                upperEntryName.endsWith(".RSA") ||
                upperEntryName.endsWith(".DSA");
    }

    private Path resolveFilterWorkRoot(FilterConfigDto config, long version) {
        return workRoot.resolve(String.valueOf(version)).resolve(config.filterName());
    }

    private Path resolveClassRoot(FilterConfigDto config, long version) {
        return resolveFilterWorkRoot(config, version).resolve("classes");
    }

    private Path resolveJarPath(FilterConfigDto config, long version) {
        return resolveFilterWorkRoot(config, version).resolve("jar").resolve(config.filterName() + ".jar");
    }

    private Path resolveBundlePath(long version) {
        if (version < 1) {
            throw new FilterBundleException("Bundle version must be greater than zero. version=" + version);
        }

        return bundleRoot.resolve("bundle-" + version + ".jar");
    }

    private void moveAtomically(Path source, Path target) throws IOException {

        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void validateFilter(FilterConfigDto config, long version) {
        if (config == null) {
            throw new InvalidFilterRequestException("Filter config is null");
        }
        String filterName = config.filterName();
        if (filterName == null || filterName.isBlank()) {
            throw new InvalidFilterRequestException("Filter name must not be blank");
        }

        if (!filterName.matches("[a-zA-Z0-9._-]+")) {
            throw new InvalidFilterRequestException("Filter name contains invalid characters: " + filterName);
        }

        if (version < 1) {
            throw new InvalidFilterRequestException("Filter version must be greater than zero. version=" + version);
        }
    }

    private void validateBundle(Map<String, FilterDefinition> filters, long version) {
        if (filters == null) {
            throw new FilterBundleException("Filter bundle definitions must not be null");
        }

        if (version < 1) {
            throw new FilterBundleException("Filter bundle version must be greater than zero. version=" + version);
        }

        for (Map.Entry<String, FilterDefinition> entry : filters.entrySet()) {
            String filterName = entry.getKey();
            FilterDefinition definition = entry.getValue();
            if (filterName == null || filterName.isBlank()) {
                throw new FilterBundleException("Filter bundle entry name must not be blank");
            }

            if (definition == null) {
                throw new FilterBundleException("Filter bundle definition must not be null. filterName=" + filterName);
            }

            if (definition.config() == null) {
                throw new FilterBundleException("Filter bundle config must not be null. filterName=" + filterName);
            }

            if (definition.jarPath() == null) {
                throw new FilterBundleException("Filter bundle jar path must not be null. filterName=" + filterName);
            }

            if (!Files.isRegularFile(definition.jarPath())) {
                throw new FilterBundleException("Filter bundle jar file does not exist. filterName=" + filterName);
            }
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

}
