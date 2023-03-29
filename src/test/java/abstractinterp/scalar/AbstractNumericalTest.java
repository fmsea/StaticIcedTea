package abstractinterp.scalar;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class AbstractNumericalTest {

    protected String generateReport(Analysis analysis) {
        try (StringWriter writer = new StringWriter()) {
            analysis.writeReport(writer);
            writer.flush();
            return writer.toString();
        } catch (IOException ex) {
            System.err.println(ex.toString());
            return "";
        }
    }

    protected String readResourcesFile(String fileName) {
        try {
            String resourceFileName = "abstractinterp/scalar/" + fileName;
            ClassLoader loader = getClass().getClassLoader();
            Path resourceFile = Paths.get(loader.getResource(resourceFileName).getFile());
            return Files.readString(resourceFile).trim();
        } catch (IOException ex) {
            System.err.println(ex.toString());
            throw new RuntimeException(ex);
        }
    }
}
