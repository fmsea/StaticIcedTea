package dev.fmsea.driver.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import dev.fmsea.processing.InferDomains;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "infer-domains",
         mixinStandardHelpOptions = true,
         description = "Analyze diakon output and infer the weakly-relational domain type for each variable.")
public class InferDomainsCommand implements Callable<Integer> {
    @Parameters(index="0",
                description="Name of file to parse")
    protected Path inputFile;

    @Override
    public Integer call() throws Exception {
        try (FileReader reader = new FileReader(inputFile.toFile());
             BufferedReader bufRd = new BufferedReader(reader);
             BufferedWriter bufWr = new BufferedWriter(new OutputStreamWriter(System.out));) {
            InferDomains.inferDomains(bufRd, bufWr);
        } catch (IOException ex) {
        }
        return 0;
    }
}
