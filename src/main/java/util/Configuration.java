package util;

import soot.Scene;
import java.io.File;
import java.nio.file.Paths;

public class Configuration {

    public static String PPLLibraryPath() {
        String pplDir = System.getenv("PPL_JNI");
        if (pplDir.isEmpty()) {
            return "/usr/local/lib/ppl/libppl_java.jnilib";
        } else {
            return pplDir;
        }
    }

    public static void LoadArtifactsIntoSootPath() {
        LoadArtifactsIntoSootPath("artifacts/");
    }

    public static void LoadArtifactsIntoSootPath(String path) {
        String artifactsClassPath = Paths.get(path).toAbsolutePath().toString();
        Scene.v().setSootClassPath(Scene.v().getSootClassPath() +
                                   File.pathSeparator +
                                   artifactsClassPath);
    }
}
