package guru.springframework.spring6restmvcapi;

import guru.springframework.spring6restmvcapi.repository.BeerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(useMainMethod = SpringBootTest.UseMainMethod.ALWAYS)
@Slf4j
class SfgRestfulBreweryApplicationTest {
    
    @Autowired
    BeerRepository beerRepository;

    @Test
    void contextLoads() {
        log.info("Testing Spring 6 Application...");
        assertEquals(30, beerRepository.count()); 
    }

}
