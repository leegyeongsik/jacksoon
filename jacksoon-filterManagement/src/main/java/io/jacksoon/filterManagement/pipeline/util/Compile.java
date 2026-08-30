package io.jacksoon.filterManagement.pipeline.util;

import io.jacksoon.common.filter.FilterConfigDto;
import io.jacksoon.filterManagement.exception.FilterCompileException;
import io.jacksoon.filterManagement.exception.InvalidFilterRequestException;
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
            throw new FilterCompileException("Failed to prepare filter compile workspace", exception);
        }
    }

    private void compileSource(Path javaFile, Path classRoot) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new FilterCompileException("Java compiler is unavailable");
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
            throw new FilterCompileException("Filter source compilation failed. file=" + javaFile);
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
            throw new InvalidFilterRequestException("Filter source body is empty");
        }
        if (config == null) {
            throw new InvalidFilterRequestException("Filter config is null");
        }
        if (version < 1) {
            throw new InvalidFilterRequestException("Filter version must be greater than zero");
        }
    }
}