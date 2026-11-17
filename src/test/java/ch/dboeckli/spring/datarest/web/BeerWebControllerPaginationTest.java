package ch.dboeckli.spring.datarest.web;

import ch.dboeckli.spring.datarest.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BeerWebControllerPaginationTest {

    @Test
    void listBeersHandlesEmptyRepository() {
        BeerRepository beerRepository = mock(BeerRepository.class);
        when(beerRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 25), 0));
        BeerWebController controller = new BeerWebController(beerRepository);
        Model model = new ExtendedModelMap();

        String viewName = controller.listBeers(model, 0, 25);

        assertThat(viewName).isEqualTo(BeerWebController.BEERS_TEMPLATE);
        assertThat(model.getAttribute("totalPages")).isEqualTo(0);
        assertThat(model.getAttribute("pageNumbers")).isNull();
        assertThat(model.getAttribute("startPage")).isEqualTo(0);
        assertThat(model.getAttribute("endPage")).isEqualTo(-1);
    }

}
