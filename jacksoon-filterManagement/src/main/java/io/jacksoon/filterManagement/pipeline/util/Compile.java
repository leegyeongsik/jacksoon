package io.jacksoon.filterManagement.pipeline.util;

import io.jacksoon.common.filter.FilterConfigDto;
import io.jacksoon.init.annotation.Init;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Init
public class Compile {
    private final Path workRoot = Path.of("plugins/work");

    public Path compile(byte[] sourceBytes, FilterConfigDto config, long version) {
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
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new IllegalStateException();
            }

            ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
            int result;
            try (PrintStream errorStream = new PrintStream(errorOutput)) {
                result = compiler.run(
                        null,
                        null,
                        errorStream,
                        "-encoding", "UTF-8",
                        "-classpath", System.getProperty("java.class.path"),
                        "-d", classRoot.toString(),
                        javaFile.toString()
                );
            }

            if (result != 0) {
                throw new IllegalStateException();
            }
            return classRoot;
        } catch (IOException e) {
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
            for (Path path : paths.sorted((left, right) -> right.compareTo(left)).toList()) {
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
