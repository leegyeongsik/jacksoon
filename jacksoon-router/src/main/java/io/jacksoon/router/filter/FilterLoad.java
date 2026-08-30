package io.jacksoon.router.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jacksoon.common.filter.FilterBundleMetadata;
import io.jacksoon.common.filter.FilterConfigDto;
import io.jacksoon.common.filter.FilterRegistryKey;
import io.jacksoon.common.filter.RouterFilter;
import io.jacksoon.init.annotation.Init;
import io.jacksoon.router.exception.RouterFilterException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Init
public class FilterLoad {

    private static final String METADATA_ENTRY_NAME = "META-INF/filter-bundle.json";
    private final Path bundleRoot = Path.of("plugins/filter-bundles");
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FilterRegistry filterRegistry;

    public FilterLoad(FilterRegistry filterRegistry) {
        this.filterRegistry = filterRegistry;
    }

    public long requestVersion(String path) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(path)).GET().build();
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new RouterFilterException("Filter version request failed. status=" + response.statusCode());
            }
            byte[] responseBody = response.body();
            if (responseBody == null || responseBody.length != Long.BYTES) {
                throw new RouterFilterException("Invalid filter version response body");
            }

            return ByteBuffer.wrap(responseBody).getLong();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RouterFilterException("Interrupted while requesting filter version", e);
        } catch (IOException e) {
            throw new RouterFilterException("Failed to request filter version", e);
        }
    }

    public long loadAndReplace(String bundleUrl) {
        Path tempBundlePath = null;
        URLClassLoader classLoader = null;
        try {
            byte[] bundleBytes = requestBundle(bundleUrl);

            tempBundlePath = saveTempBundle(bundleBytes);

            FilterBundleMetadata metadata = readMetadata(tempBundlePath);
            validateMetadata(metadata);

            Path bundlePath = moveToVersionPath(tempBundlePath, metadata.version());
            tempBundlePath = null;
            classLoader = createClassLoader(bundlePath);
            Map<FilterRegistryKey, List<RegisteredFilter>> candidateFilters = createFilterRegistry(metadata, classLoader);
            LoadedFilterBundle candidateBundle = new LoadedFilterBundle(metadata.version(), bundlePath, classLoader, candidateFilters);
            // 여기서 가져온 번들 jar를 버전 , path위치 , classLoad ,
            // ((타이밍 , 파이프라인) -> 필터s )로 번들info를 만들고 가령 ( pre , router -> {aFilter ,bFilter } ) , ( post , router -> {aFilter , cFilter } )
            // 새 버전의 번들정보를 저장함
            filterRegistry.replace(candidateBundle);
            classLoader = null;
            return metadata.version();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RouterFilterException("Interrupted while loading filter bundle", e);
        } catch (Exception e) {
            if (e instanceof RouterFilterException routerFilterException) {
                throw routerFilterException;
            }
            throw new RouterFilterException("Failed to load filter bundle. url=" + bundleUrl, e);
        } finally {
            closeQuietly(classLoader);
            deleteQuietly(tempBundlePath);
        }
    }

    private byte[] requestBundle(String bundleUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(bundleUrl)).GET().build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new RouterFilterException("Filter bundle request failed. status=" + response.statusCode());
        }
        byte[] bundleBytes = response.body();
        if (bundleBytes == null || bundleBytes.length == 0) {
            throw new RouterFilterException("Filter bundle response body is empty");
        }
        return bundleBytes;
    }

    private Path saveTempBundle(byte[] bundleBytes
    ) throws IOException {
        Files.createDirectories(bundleRoot);
        Path tempBundlePath = Files.createTempFile(bundleRoot, "bundle-download-", ".jar.tmp");
        Files.write(tempBundlePath, bundleBytes);
        return tempBundlePath;
    }

    private FilterBundleMetadata readMetadata(Path bundlePath
    ) throws IOException {
        try (JarFile jarFile = new JarFile(bundlePath.toFile())) {
            JarEntry metadataEntry = jarFile.getJarEntry(METADATA_ENTRY_NAME);
            if (metadataEntry == null) {
                throw new RouterFilterException("Filter bundle metadata entry not found");
            }

            try (InputStream inputStream = jarFile.getInputStream(metadataEntry)) {
                return objectMapper.readValue(inputStream, FilterBundleMetadata.class);
            }
        }
    }

    private void validateMetadata(FilterBundleMetadata metadata) {
        if (metadata == null) {
            throw new RouterFilterException("Filter bundle metadata is null");
        }

        if (metadata.version() < 1) {
            throw new RouterFilterException("Filter bundle version must be greater than zero");
        }

        if (metadata.filters() == null) {
            throw new RouterFilterException("Filter bundle filters must not be null");
        }
    }

    private Path moveToVersionPath(Path tempBundlePath, long version) throws IOException {
        Path bundlePath = bundleRoot.resolve("bundle-" + version + ".jar");

        try {
            return Files.move(tempBundlePath, bundlePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        } catch (AtomicMoveNotSupportedException e) {
            return Files.move(tempBundlePath, bundlePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private URLClassLoader createClassLoader(Path bundlePath) throws IOException {
        URL bundleUrl = bundlePath.toUri().toURL();
        return new URLClassLoader(new URL[]{bundleUrl}, FilterLoad.class.getClassLoader());
    }

    private Map<FilterRegistryKey, List<RegisteredFilter>> createFilterRegistry(FilterBundleMetadata metadata, URLClassLoader classLoader) throws Exception {
        Map<FilterRegistryKey, List<RegisteredFilter>> candidate = new HashMap<>();
        for (FilterConfigDto config : metadata.filters()) {
            validateFilterConfig(config);
            RouterFilter filter = createFilterObject(config, classLoader);
            FilterRegistryKey key = new FilterRegistryKey(config.timing(), config.pipeline());
            RegisteredFilter registeredFilter = new RegisteredFilter(config, filter);
            candidate.computeIfAbsent(key, ignored -> new ArrayList<>()).add(registeredFilter); // key에 등록된 필터를 넣음 만약에 키가 a+b면 a+b에 afitler를 집어넣음
        }                                                                                                      // 또 키가 a+b고 필터가 bfitler라면 a+b가져와서 집어넣음
        candidate.values().forEach(
                filters -> filters.sort(
                        Comparator
                                .comparingInt(
                                        (RegisteredFilter registered) ->
                                                registered.config().order()
                                )
                                .thenComparing(
                                        registered ->
                                                registered.config().filterName()
                                )
                )
        );
        Map<FilterRegistryKey, List<RegisteredFilter>> immutableCandidate = new HashMap<>();
        candidate.forEach((key, filters) -> immutableCandidate.put(key, List.copyOf(filters)));
        return Map.copyOf(immutableCandidate);
    }

    private RouterFilter createFilterObject(FilterConfigDto config, URLClassLoader classLoader) throws Exception {
        String className = config.className();
        Class<?> loadedClass = Class.forName(className, true, classLoader);
        if (!RouterFilter.class.isAssignableFrom(loadedClass)) {
            throw new RouterFilterException("Loaded class does not implement RouterFilter. class=" + className);
        }
        Class<? extends RouterFilter> filterClass = loadedClass.asSubclass(RouterFilter.class);
        return filterClass.getDeclaredConstructor().newInstance();
    }

    private void validateFilterConfig(FilterConfigDto config) {
        if (config == null) {
            throw new RouterFilterException("Filter config must not be null");
        }
        if (config.filterName() == null || config.filterName().isBlank()) {
            throw new RouterFilterException("Filter name must not be blank");
        }
        if (config.timing() == null) {
            throw new RouterFilterException("Filter timing must not be null");
        }

        if (config.pipeline() == null) {
            throw new RouterFilterException("Filter pipeline must not be null");
        }
    }

    private void closeQuietly(URLClassLoader classLoader) {
        if (classLoader == null) {
            return;
        }
        try {
            classLoader.close();
        } catch (IOException ignored) {
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

}
