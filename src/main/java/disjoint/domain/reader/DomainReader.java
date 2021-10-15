package disjoint.domain.reader;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.microsoft.z3.Z3Exception;

import disjoint.domain.Domain;

public class DomainReader {

    List<Domain> readDomains;

    public DomainReader(String fileName) {
        try (Reader reader = new FileReader(fileName)) {
            initializeDomains(reader);
        } catch (FileNotFoundException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    public DomainReader(Reader reader) {
        initializeDomains(reader);
    }

    private void initializeDomains(Reader reader) {
        // holds the domain descriptions read from the file
        List<String> domains = new ArrayList<String>();
        // for each line in the file create
        // the corresponding domain
        Scanner scanner = new Scanner(reader);
        while (scanner.hasNextLine()) {
            domains.add(scanner.nextLine());
        }
        scanner.close();

        // iterate over the domain description and
        // instantiate domain for each of them
        readDomains = new ArrayList<Domain>();
        for (String domain : domains) {
            readDomains.add(instantateDomain(domain));
        }

    }

    private Domain instantateDomain(String domain) {
        Domain ret = null;
        try {
            CharStream domainInput = CharStreams.fromString(domain);
            DisjointDomainLexer domainLexer = new DisjointDomainLexer(domainInput);
            CommonTokenStream domainTokens = new CommonTokenStream(domainLexer);
            DisjointDomainParser domainParser = new DisjointDomainParser(domainTokens);
            ParseTree domainTree = domainParser.intervals();
            DomainInstantiator createDomain = new DomainInstantiator();
            createDomain.visit(domainTree);
            ret = createDomain.domain;
        } catch (Z3Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    public List<Domain> getReadDomains() {
        return readDomains;
    }

}
