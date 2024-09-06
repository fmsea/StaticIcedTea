package dev.fmsea.util;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Compiler {

    private static final Logger LOG = LoggerFactory.getLogger(Compiler.class);

    public static Path compileSource(String name, String sourceBody) throws Exception {
        OutputStream out = new ByteArrayOutputStream();
        OutputStream err = new ByteArrayOutputStream();
        Path dir = Files.createTempDirectory("dfa-smt-driver-tests");
        Path source = Paths.get(dir.toAbsolutePath().toString(), name + ".java");
        Path target = Paths.get(dir.toAbsolutePath().toString(), name + ".class");
        try (Writer w = new FileWriter(source.toString())) {
            w.write(sourceBody);
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null,
                     out,
                     err,
                     "-classpath",
                     dir.toString(),
                     "-source",
                     "8",
                     "-target",
                     "8",
                     source.toFile().getAbsolutePath());
        LOG.debug("compiler output: {}", out);
        LOG.debug("compiler error:  {}", err);
        return source.getParent().resolve(name + ".class");
    }
}
