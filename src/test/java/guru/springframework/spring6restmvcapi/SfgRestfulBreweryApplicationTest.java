package guru.springframework.spring6restmvcapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@Slf4j
public class SfgRestfulBreweryApplicationTest {

    @Test
    public void contextLoads() {
        log.info("Testing Spring 6 Application...");
    }

}
