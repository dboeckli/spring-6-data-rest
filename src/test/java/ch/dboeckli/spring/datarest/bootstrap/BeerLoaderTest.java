package ch.dboeckli.spring.datarest.bootstrap;

import ch.dboeckli.spring.datarest.repository.BeerRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeerLoaderTest {

    @Test
    void doesNotLoadBeersWhenDataAlreadyExists() {
        BeerRepository beerRepository = mock(BeerRepository.class);
        when(beerRepository.count()).thenReturn(5L);
        BeerLoader beerLoader = new BeerLoader(beerRepository);

        beerLoader.run();

        verify(beerRepository, never()).save(any());
    }

}
