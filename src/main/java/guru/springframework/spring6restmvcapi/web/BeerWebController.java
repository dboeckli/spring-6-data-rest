package guru.springframework.spring6restmvcapi.web;

import guru.springframework.spring6restmvcapi.entity.Beer;
import guru.springframework.spring6restmvcapi.repository.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Controller
@RequestMapping(BeerWebController.WEB_BASE_PATH)
@RequiredArgsConstructor
public class BeerWebController {
    
    public static final String WEB_BASE_PATH = "/web";
    public static final String BEERS = "beers"  ;
    public static final String LIST_BEERS_PATH = WEB_BASE_PATH + "/" + BEERS;
    private static final String REDIRECT_PREFIX = "/redirect";
    
    private final BeerRepository beerRepository;

    @GetMapping("/" + BEERS)
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

        return BEERS;
    }

    @GetMapping("/beer/{id}")
    public String getBeer(@PathVariable UUID id, Model model) {
        Beer beer = beerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("beer", beer);
        return "beer";
    }

    @GetMapping("/beer/new")
    public String newBeerForm(Model model) {
        model.addAttribute("beer", new Beer());
        return "beerForm";
    }

    @PostMapping("/beer/new")
    public String createBeer(@ModelAttribute Beer beer) {
        beerRepository.save(beer);
        return REDIRECT_PREFIX + LIST_BEERS_PATH;
    }

    @GetMapping("/beer/{id}/edit")
    public String editBeerForm(@PathVariable UUID id, Model model) {
        Beer beer = beerRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Beer not found"));
        model.addAttribute("beer", beer);
        return "beerForm";
    }

    @PostMapping("/beer/{id}/edit")
    public String updateBeer(@PathVariable UUID id, @ModelAttribute Beer beer) {
        beer.setId(id);
        beerRepository.save(beer);
        return REDIRECT_PREFIX + LIST_BEERS_PATH;
    }

    @DeleteMapping("/beer/{id}/delete")
    public String deleteBeer(@PathVariable UUID id) {
        beerRepository.deleteById(id);
        return REDIRECT_PREFIX + LIST_BEERS_PATH;
    }
    
}
