package driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.DefaultOctagonState;
import abstractinterp.scalar.state.OctagonDifferenceBoundedMatrixBuilder;
import abstractinterp.scalar.state.OctagonState;
import common.Locals;
import soot.Local;

public class GenerateOctagonsRunner implements Runnable {

    private final Path outputPath;
    private final int numOctagons;

    public GenerateOctagonsRunner(Path outputPath, int numOctagons) {
        this.outputPath = outputPath;
        this.numOctagons = numOctagons;
    }

    public void run() {
        Random r = new Random(1234567890);
        Set<OctagonState> states = IntStream.range(0, this.numOctagons).boxed()
            .map(i -> generateOctagon(r.nextInt(100) + 1))
            .collect(Collectors.toSet());

        File dir = this.outputPath.toFile();
        dir.mkdirs();
        for (OctagonState state : states) {
            File output = Path.of(this.outputPath.toString(),
                String.format("octagon.%d.smt", state.hashCode())).toFile();

            try (FileWriter fw = new FileWriter(output);
                 BufferedWriter buf = new BufferedWriter(fw)) {

                buf.write(state.getLocals()
                    .stream()
                    .sorted((a, b) -> a.toString().compareTo(b.toString()))
                    .map(l -> l.toString())
                    .collect(Collectors.joining("\t")));
                buf.write("\n");
                buf.write(state.toSmt());
                buf.write("\n");
                buf.flush();
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }

    private OctagonState generateOctagon(int N) {
        Random r = new Random(123467890);
        Set<Local> locals = IntStream.range(0, N)
            .boxed()
            .map(i -> String.format("x%d", i))
            .map(x -> Locals.get(x))
            .collect(Collectors.toSet());
        int oN = 2 * N;
        OctagonDifferenceBoundedMatrixBuilder builder = new OctagonDifferenceBoundedMatrixBuilder(oN, true);
        for (int i = 0; i < oN; i++) {
            for (int j = 0; j < (i + 1); j++) {
                if (i == j) {
                    builder.setConstraint(i, j, Constraint.of(0));
                } else if (r.nextDouble() > 0.3) {
                    Constraint c = Constraint.of(r.nextInt(65535));
                    builder.setConstraint(i, j, c);
                    builder.setConstraint(j ^ 1, i ^ 1, c.copy());
                }
            }
        }
        return new DefaultOctagonState(locals, builder.build());
    }
}
