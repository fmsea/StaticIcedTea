package util;

public class Configuration {

    public static String PPLLibraryPath() {
        String pplDir = System.getenv("PPL_JNI");
        if (pplDir.isEmpty()) {
            return "/usr/local/lib/ppl/libppl_java.jnilib";
        } else {
            return pplDir;
        }
    }
}
