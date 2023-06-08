package com.beerclient.services;

import com.beerclient.configs.WebClientProperties;
import domain.Beer;
import domain.BeerPagedList;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerClientServiceImpl implements BeerClientService {

    private final WebClient webClient;

    @Override
    public Mono<BeerPagedList> getAllBeers (Integer pageNumber, Integer pageSize, String beerName, String beerStyle, PageRequest pageRequest,
                                            Boolean showInventoryOnHand) {
        return webClient.get ()
                .uri (uriBuilder -> uriBuilder.path (WebClientProperties.BEER_V1_PATH)
                        .queryParam ("pageNumber", Optional.ofNullable (pageNumber))
                        .queryParam ("pageSize", Optional.ofNullable (pageSize))
                        .queryParam ("beerName", Optional.ofNullable (beerName))
                        .queryParam ("beerStyle", Optional.ofNullable (beerStyle))
                        .queryParam ("showInventoryOnHand", Optional.ofNullable (showInventoryOnHand))
                        .build ())
                .retrieve ()
                .bodyToMono (BeerPagedList.class);
    }

    @Override
    public Mono<Beer> getBeerById (final UUID beerId) {
        return null;
    }

    @Override
    public Mono<Beer> getBeerByUpc (final String upc) {
        return null;
    }

    @Override
    public Mono<Beer> createBeer (final Beer beerDto) {
        return null;
    }

    @Override
    public Mono<Beer> updateBeer (final Beer beerDto) {
        return null;
    }

    @Override
    public Mono<Void> deleteBeerById (final UUID beerId) {
        return null;
    }
}
