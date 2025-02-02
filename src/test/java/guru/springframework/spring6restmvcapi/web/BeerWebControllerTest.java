package guru.springframework.spring6restmvcapi.web;

import guru.springframework.spring6restmvcapi.entity.Beer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
class BeerWebControllerTest {

    @Autowired
    BeerWebController controller;

    @Test
    @Order(0)
    void testListBeers() {
        Model model = new ExtendedModelMap();

        String viewName = controller.listBeers(model, 0, 25);
        // Log all attributes in the model
        log.info("### Model attributes:");
        model.asMap().forEach((key, value) -> log.info(key + ": " + value));

        assertAll(
            () -> assertEquals("beers", viewName),
            () -> assertEquals(25, ((List<Beer>)model.getAttribute("beers")).size()),
            () -> assertEquals(2, model.getAttribute("totalPages")),
            () -> assertEquals(0, model.getAttribute("currentPage")),
            () -> assertEquals(30L, model.getAttribute("totalItems")),
            () -> assertEquals(new ArrayList<>(List.of(1, 2)), model.getAttribute("pageNumbers")),
            () -> assertEquals(0, model.getAttribute("startPage")),
            () -> assertEquals(1, model.getAttribute("endPage"))
        );
    }

    @Test
    @Order(1)
    void testGetBeer() {
        // First, get a list of beers to obtain a valid ID
        Model listModel = new ExtendedModelMap();
        controller.listBeers(listModel, 0, 25);
        List<Beer> beers = (List<Beer>) listModel.getAttribute("beers");

        // Get the ID of the first beer
        UUID beerId = beers.getFirst().getId();

        // Now test the getBeer method
        Model model = new ExtendedModelMap();
        String viewName = controller.getBeer(beerId, model);

        // Log all attributes in the model
        log.info("### Model attributes for getBeer:");
        model.asMap().forEach((key, value) -> log.info(key + ": " + value));

        assertAll(
            () -> assertEquals("beer", viewName),
            () -> assertNotNull(model.getAttribute("beer")),
            () -> assertInstanceOf(Beer.class, model.getAttribute("beer")),
            () -> assertEquals(beerId, ((Beer) model.getAttribute("beer")).getId())
        );
    }


}
