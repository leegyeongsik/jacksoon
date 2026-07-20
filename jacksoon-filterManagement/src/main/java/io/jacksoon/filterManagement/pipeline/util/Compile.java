package io.jacksoon.filterManagement.pipeline.util;

import io.jacksoon.common.filter.FilterConfigDto;
import io.jacksoon.init.annotation.Init;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Init
public class Compile {

    private final Path workRoot = Path.of("plugins/work");

    public void compile(byte[] sourceBytes, FilterConfigDto config, long version) {
        validate(sourceBytes, config, version);
        Path filterRoot = workRoot.resolve(String.valueOf(version)).resolve(config.filterName());
        Path sourceRoot = filterRoot.resolve("src");
        Path classRoot = filterRoot.resolve("classes");
        Path javaFile = sourceRoot.resolve(classNameToPath(config.className()));
        try {
            deleteRecursively(filterRoot);
            Files.createDirectories(javaFile.getParent());
            Files.createDirectories(classRoot);
            Files.write(javaFile, sourceBytes);
            compileSource(javaFile, classRoot);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private void compileSource(Path javaFile, Path classRoot) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException();
        }
        int result = compiler.run(
                null,
                null,
                null,
                "-encoding", "UTF-8",
                "-classpath", System.getProperty("java.class.path"),
                "-proc:none",
                "-d", classRoot.toString(),
                javaFile.toString()
        );
        if (result != 0) {
            throw new IllegalStateException();
        }
    }

    private Path classNameToPath(String className) {
        return Path.of(className.replace('.', '/') + ".java");
    }

    private void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private void validate(byte[] sourceBytes, FilterConfigDto config, long version) {
        if (sourceBytes == null || sourceBytes.length == 0) {
            throw new IllegalArgumentException();
        }
        if (config == null) {
            throw new IllegalArgumentException();
        }
        if (version < 1) {
            throw new IllegalArgumentException();
        }
    }
}