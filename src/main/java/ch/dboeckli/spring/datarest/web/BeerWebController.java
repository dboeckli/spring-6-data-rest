package ch.dboeckli.spring.datarest.web;

import ch.dboeckli.spring.datarest.entity.Beer;
import ch.dboeckli.spring.datarest.repository.BeerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Controller
@RequestMapping(BeerWebController.WEB_BASE_PATH)
@RequiredArgsConstructor
@Slf4j
public class BeerWebController {
    
    public static final String WEB_BASE_PATH = "/web";
    
    public static final String BEERS_TEMPLATE = "beers"  ;
    public static final String BEER_TEMPLATE = "beer";
    public static final String BEER_FORM_TEMPLATE = "beerForm";
    
    public static final String LIST_BEERS_PAGE = WEB_BASE_PATH + "/" + BEERS_TEMPLATE;
    public static final String BEER_PAGE = WEB_BASE_PATH + "/" + BEER_TEMPLATE;

    public static final String REDIRECT_PREFIX = "redirect:";
    
    private final BeerRepository beerRepository;

    @GetMapping("/" + BEERS_TEMPLATE)
    public String listBeers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "25") int size) {
        Page<Beer> beerPage = beerRepository.findAll(PageRequest.of(page, size));
        model.addAttribute("beers", beerPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", beerPage.getTotalPages());
        model.addAttribute("totalItems", beerPage.getTotalElements());

        int totalPages = beerPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
            model.addAttribute("pageNumbers", pageNumbers);
        }

        // Calculate start and end page numbers for pagination
        int paginationSize = 5; // Number of page links to show
        int startPage = Math.max(0, page - paginationSize / 2);
        int endPage = Math.min(totalPages - 1, startPage + paginationSize - 1);

        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return BEERS_TEMPLATE;
    }

    @GetMapping("/" + BEER_TEMPLATE + "/{id}")
    public String getBeer(@PathVariable UUID id, Model model) {
        Beer beer = beerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("beer", beer);
        return BEER_TEMPLATE;
    }

    @GetMapping("/" + BEER_TEMPLATE + "/new")
    public String newBeerForm(Model model) {
        log.info("Creating new beer in form");
        model.addAttribute("beer", new Beer());
        return BEER_FORM_TEMPLATE;
    }

    @PostMapping("/" + BEER_TEMPLATE + "/edit/")
    public String createBeer(@Valid @ModelAttribute Beer beer, BindingResult bindingResult, Model model) {
        log.info("### Creating new beer: {}", beer);
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors occurred: {}", bindingResult.getAllErrors());
            List<String> fieldsToIgnore = List.of("createdDate", "lastModifiedDate");
            boolean hasOtherErrors = bindingResult.getFieldErrors().stream()
                .anyMatch(error -> !fieldsToIgnore.contains(error.getField()));

            if (hasOtherErrors) {
                log.error("Validation errors occurred: {}", bindingResult.getAllErrors());
                model.addAttribute("beer", beer);
                return BEER_FORM_TEMPLATE;
            } 
        }
        Beer newBeer = new Beer();
        newBeer.setBeerName(beer.getBeerName());
        newBeer.setBeerStyle(beer.getBeerStyle());
        newBeer.setUpc(beer.getUpc());
        newBeer.setQuantityOnHand(beer.getQuantityOnHand());
        newBeer.setPrice(beer.getPrice());
        newBeer.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        newBeer.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
        Beer createdBeer = beerRepository.save(beer);
        log.info("### Created new beer: {}", createdBeer);
        return REDIRECT_PREFIX + LIST_BEERS_PAGE;
    }

    @GetMapping("/" + BEER_TEMPLATE + "/edit/{id}")
    public String editBeerForm(@PathVariable UUID id, Model model) {
        log.info("Updating existing beer in form");
        Beer beer = beerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beer not found"));
        model.addAttribute("beer", beer);
        return BEER_FORM_TEMPLATE;
    }

    @PostMapping("/" + BEER_TEMPLATE + "/edit/{id}")
    public String updateBeer(@PathVariable UUID id, @Valid @ModelAttribute("beer") Beer beer) {
        log.info("### Updating beer: {}", id);
        Beer existingBeer = beerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beer not found"));
        
        // Update the fields of the existing beer
        existingBeer.setBeerName(beer.getBeerName());
        existingBeer.setBeerStyle(beer.getBeerStyle());
        existingBeer.setUpc(beer.getUpc());
        existingBeer.setQuantityOnHand(beer.getQuantityOnHand());
        existingBeer.setPrice(beer.getPrice());
        
        beerRepository.save(existingBeer);
        log.info("### Updating beer: {} to {}", existingBeer, beer);
        return REDIRECT_PREFIX + LIST_BEERS_PAGE;
    }

    @PostMapping(BEER_TEMPLATE + "/delete/{id}")
    public String deleteBeer(@PathVariable UUID id) {
        log.info("Deleting beer with ID: {}", id);
        beerRepository.deleteById(id);
        log.info("Deleted beer with ID: {}", id);
        return REDIRECT_PREFIX + LIST_BEERS_PAGE;
    }
    
}
