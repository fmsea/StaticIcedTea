package disjoint.domain.reader;

import java.io.StringReader;
import java.io.Reader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import disjoint.domain.Domain;

public class DomainReaderTest {

    @Test
    void testInstantiateDomain() {
        Reader r = new StringReader("(inf,-2] -1 0 1 [2,inf)\n" +
                                    "(inf,-5] (-5,0) 0 (0,5) [5,inf)\n");
        DomainReader domainReader = new DomainReader(r);
        List<Domain> domains = domainReader.getReadDomains();
        assertAll("domain reader instantiates two domains",
                  () -> assertNotNull(domains),
                  () -> assertEquals(2, domains.size()));
    }
}
