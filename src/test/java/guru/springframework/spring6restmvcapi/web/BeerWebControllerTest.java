package guru.springframework.spring6restmvcapi.web;

import guru.springframework.spring6restmvcapi.entity.Beer;
import guru.springframework.spring6restmvcapi.entity.BeerStyleEnum;
import guru.springframework.spring6restmvcapi.repository.BeerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.MapBindingResult;

import java.math.BigDecimal;
import java.util.*;

import static guru.springframework.spring6restmvcapi.web.BeerWebController.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
class BeerWebControllerTest {

    @Autowired
    BeerWebController controller;

    @Autowired
    BeerRepository beerRepository;

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

    @Test
    @Order(2)
    void testEditBeer() {
        // First, get a list of beers to obtain a valid ID
        Model listModel = new ExtendedModelMap();
        controller.listBeers(listModel, 0, 25);
        List<Beer> beers = (List<Beer>) listModel.getAttribute("beers");

        // Get the ID of the first beer
        UUID beerId = beers.getFirst().getId();

        // Get the current beer details
        Model currentModel = new ExtendedModelMap();
        controller.getBeer(beerId, currentModel);
        Beer currentBeer = (Beer) currentModel.getAttribute("beer");
        String originalBeerName = currentBeer.getBeerName();

        // Now test the editBeer method
        Model editModel = new ExtendedModelMap();
        String editViewName = controller.editBeerForm(beerId, editModel);

        // Verify that we're on the edit page
        assertEquals(BEER_FORM_TEMPLATE, editViewName);

        // Get the beer from the edit model
        Beer beerToEdit = (Beer) editModel.getAttribute("beer");
        assertNotNull(beerToEdit);

        // Change the beer name
        String newBeerName = "Updated " + originalBeerName;
        beerToEdit.setBeerName(newBeerName);

        // Simulate form submission to update the beer
        String beerFormViewName = controller.editBeerForm(beerId, editModel);
        assertEquals(BEER_FORM_TEMPLATE, beerFormViewName);

        String resultViewName = controller.updateBeer(beerId, beerToEdit);
        assertEquals(REDIRECT_PREFIX + LIST_BEERS_PAGE, resultViewName);

        Beer updatedBeer = beerRepository.findById(beerId)
            .orElseThrow(() -> new AssertionError("Beer not found with ID: " + beerId));
        log.info("Beer name changed from '{}' to '{}'", originalBeerName, updatedBeer.getBeerName());

        assertAll(
            () -> assertNotNull(updatedBeer),
            () -> assertNotEquals(originalBeerName, updatedBeer.getBeerName()),
            () -> assertEquals(newBeerName, updatedBeer.getBeerName()),
            () -> assertEquals(beerId, updatedBeer.getId())
        );
    }

    @Test
    @Order(3)
    void testCreateBeer() {
        Model beerFormModel = new ExtendedModelMap();
        String beerFormViewName = controller.newBeerForm(beerFormModel);
        assertEquals(BEER_FORM_TEMPLATE, beerFormViewName);

        Beer newBeer = Beer.builder()
            .beerName("new Beer")
            .beerStyle(BeerStyleEnum.IPA)
            .upc("1234567890123")
            .price(BigDecimal.valueOf(9.99))
            .quantityOnHand(5)
            .build();

        String listBeerViewName = controller.createBeer(newBeer,  new MapBindingResult(new HashMap<>(), "beer"), beerFormModel);
        assertEquals(REDIRECT_PREFIX + LIST_BEERS_PAGE, listBeerViewName);

        Page<Beer> beers = beerRepository.findAllByBeerName("new Beer", Pageable.unpaged());
        assertEquals(1, beers.getTotalElements());
    }

    @Test
    @Order(99)
    void testDeleteBeer() {
        // First, get a list of beers to obtain a valid ID
        Model listModel = new ExtendedModelMap();
        controller.listBeers(listModel, 0, 25);
        List<Beer> beers = (List<Beer>) listModel.getAttribute("beers");

        // Get the ID of the first beer
        UUID beerIdToDelete = beers.getFirst().getId();

        String listBeerViewName = controller.deleteBeer(beerIdToDelete);
        assertEquals(REDIRECT_PREFIX + LIST_BEERS_PAGE, listBeerViewName);

        Optional<Beer> beer = beerRepository.findById(beerIdToDelete);
        assertFalse(beer.isPresent());
    }
}
