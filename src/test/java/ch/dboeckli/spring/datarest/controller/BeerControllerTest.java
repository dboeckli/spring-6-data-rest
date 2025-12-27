package ch.dboeckli.spring.datarest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BeerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllBeers() throws Exception {
        // Perform GET request and validate response
        mockMvc.perform(get("/api/v4/beer")
                .param("sort", "beerName,asc")  
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$._embedded.beers", hasSize(20)))
            .andExpect(jsonPath("$._embedded.beers[0].beerName", is("Adjunct Trail")))
            .andExpect(jsonPath("$._embedded.beers[0].beerStyle", is("STOUT")))
            .andExpect(jsonPath("$._embedded.beers[0].upc", is("8380495518610")));
    }
}
