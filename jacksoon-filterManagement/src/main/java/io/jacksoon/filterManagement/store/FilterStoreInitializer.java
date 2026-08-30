package io.jacksoon.filterManagement.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.filter.FilterBundleMetadata;
import io.jacksoon.common.filter.FilterConfigDto;
import io.jacksoon.filterManagement.exception.FilterStoreException;
import io.jacksoon.init.annotation.Init;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Init
public class FilterStoreInitializer {

    private static final String METADATA_ENTRY_NAME = "META-INF/filter-bundle.json";
    private static final Pattern BUNDLE_FILE_PATTERN = Pattern.compile("bundle-(\\d+)\\.jar");
    private static final Pattern VERSION_DIRECTORY_PATTERN = Pattern.compile("\\d+");

    private final Path bundleRoot = Path.of("plugins/bundles");
    private final Path workRoot = Path.of("plugins/work");

    private final FilterStore filterStore;
    private final ObjectMapper objectMapper;

    public FilterStoreInitializer(FilterStore filterStore, ObjectMapper objectMapper) {
        this.filterStore = filterStore;
        this.objectMapper = objectMapper;
    }

    public void initialize() {
        List<BundleCandidate> candidates = findBundleCandidates();
        if (candidates.isEmpty()) {
            System.out.println("filter store initialize: no local bundle");
            return;
        }
        for (BundleCandidate candidate : candidates) {
            try {
                RestoredState restoredState = restore(candidate);
                filterStore.initialize(restoredState.filters(), restoredState.version(), restoredState.bundlePath());
                System.out.println("filter store initialized: version=" + restoredState.version() + ", filters=" + restoredState.filters().size());
                return;
            } catch (Exception e) {
                System.err.println("filter store initialize failed: " + candidate.path() + ", reason=" + e.getMessage());
            }
        }
        throw new FilterStoreException("filter store initialize: no valid local bundle");
    }
    private RestoredState restore(BundleCandidate candidate) throws IOException {
        FilterBundleMetadata metadata = readMetadata(candidate.path());
        validateMetadata(candidate, metadata);

        Map<String, FilterDefinition> filters = new HashMap<>();
        for (FilterConfigDto config : metadata.filters()) {
            validateConfig(config);
            if (filters.containsKey(config.filterName())) {
                throw new FilterStoreException("duplicated filter: " + config.filterName());
            }
            Artifact artifact = findLatestArtifact(config, metadata.version()).orElseThrow(() -> new FilterStoreException("artifact not found: " + config.filterName()));
            filters.put(config.filterName(), new FilterDefinition(config, artifact.version(), artifact.path()));
        }
        return new RestoredState(metadata.version(), candidate.path(), Map.copyOf(filters));
    }
    private List<BundleCandidate> findBundleCandidates() {
        if (!Files.isDirectory(bundleRoot)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.list(bundleRoot)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(this::toBundleCandidate)
                    .flatMap(Optional::stream)
                    .sorted(Comparator.comparingLong(BundleCandidate::version).reversed())
                    .toList();
        } catch (IOException e) {
            throw new FilterStoreException("Failed to list filter bundles", e);
        }
    }

    private Optional<BundleCandidate> toBundleCandidate(Path path) {
        Matcher matcher = BUNDLE_FILE_PATTERN.matcher(path.getFileName().toString());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        try {
            long version = Long.parseLong(matcher.group(1));
            if (version < 1L) {
                return Optional.empty();
            }
            return Optional.of(new BundleCandidate(version, path));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private FilterBundleMetadata readMetadata(Path bundlePath) throws IOException {
        try (JarFile jarFile = new JarFile(bundlePath.toFile())) {
            JarEntry metadataEntry = jarFile.getJarEntry(METADATA_ENTRY_NAME);
            if (metadataEntry == null) {
                throw new FilterStoreException("bundle metadata not found");
            }

            try (InputStream inputStream = jarFile.getInputStream(metadataEntry)) {
                return objectMapper.readValue(inputStream, FilterBundleMetadata.class);
            }
        }
    }

    private void validateMetadata(BundleCandidate candidate, FilterBundleMetadata metadata) {
        if (metadata == null) {
            throw new FilterStoreException("metadata is null");
        }
        if (metadata.version() != candidate.version()) {
            throw new FilterStoreException("bundle version mismatch");
        }
        if (metadata.version() < 1L) {
            throw new FilterStoreException("invalid bundle version");
        }
        if (metadata.filters() == null) {
            throw new FilterStoreException("filters is null");
        }
    }

    private Optional<Artifact> findLatestArtifact(FilterConfigDto config, long bundleVersion) {
        if (!Files.isDirectory(workRoot)) {
            return Optional.empty();
        }

        List<Long> versions = new ArrayList<>();
        try (Stream<Path> paths = Files.list(workRoot)) {
            paths.filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> VERSION_DIRECTORY_PATTERN.matcher(name).matches())
                    .map(Long::parseLong)
                    .filter(version -> version >= 1L && version <= bundleVersion)
                    .sorted(Comparator.reverseOrder())
                    .forEach(versions::add);
        } catch (IOException e) {
            throw new FilterStoreException("Failed to list filter work directories", e);
        }

        for (long artifactVersion : versions) {
            Path jarPath = workRoot
                    .resolve(String.valueOf(artifactVersion))
                    .resolve(config.filterName())
                    .resolve("jar")
                    .resolve(config.filterName() + ".jar");

            if (!Files.isRegularFile(jarPath)) {
                continue;
            }
            if (!containsFilterClass(jarPath, config.className())) {
                continue;
            }
            return Optional.of(new Artifact(artifactVersion, jarPath));
        }

        return Optional.empty();
    }

    private boolean containsFilterClass(Path jarPath, String className) {
        if (className == null || className.isBlank()) {
            return false;
        }
        String classEntryName = className.replace('.', '/') + ".class";

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            return jarFile.getJarEntry(classEntryName) != null;
        } catch (IOException e) {
            return false;
        }
    }

    private void validateConfig(FilterConfigDto config) {
        if (config == null) {
            throw new FilterStoreException("filter config is null");
        }
        if (config.filterName() == null || config.filterName().isBlank()) {
            throw new FilterStoreException("filter name is empty");
        }
        if (config.className() == null || config.className().isBlank()) {
            throw new FilterStoreException("filter class name is empty");
        }
        if (config.timing() == null || config.pipeline() == null) {
            throw new FilterStoreException("filter metadata is invalid");
        }
    }

    private record BundleCandidate(long version, Path path) {
    }

    private record Artifact(long version, Path path) {
    }

    private record RestoredState(
            long version,
            Path bundlePath,
            Map<String, FilterDefinition> filters
    ) {
    }
}
