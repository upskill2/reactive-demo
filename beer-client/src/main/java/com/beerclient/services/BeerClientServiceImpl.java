package com.beerclient.services;

import com.beerclient.configs.WebClientProperties;
import domain.Beer;
import domain.BeerPagedList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
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
    public Mono<Beer> getBeerById (final UUID beerId, final boolean showInventoryOnHand) {

        return webClient.get ()
                .uri (uriBuilder -> uriBuilder.path (WebClientProperties.BEER_V1_PATH_GET_BY_ID)
                        .queryParam ("showInventoryOnHand", Optional.ofNullable (showInventoryOnHand))
                        .build (beerId))
                .retrieve ()
                .bodyToMono (Beer.class);
    }

    @Override
    public Mono<Beer> getBeerByUpc (final String upc) {
        return webClient.get ()
                .uri (uriBuilder -> uriBuilder.path (WebClientProperties.BEER_V1_UPC_PATH)
                        .build (upc))
                .retrieve ()
                .bodyToMono (Beer.class);
    }

    @Override
    public Mono<ResponseEntity<Void>> createBeer (final Beer beerDto) {
        return webClient.post ()
                .uri (uriBuilder -> uriBuilder.path (WebClientProperties.BEER_V1_PATH).build ())
                .body (BodyInserters.fromValue (beerDto))
                .retrieve ()
                .toBodilessEntity ();
    }

    @Override
    public Mono<ResponseEntity<Void>> updateBeer (final UUID beerId, final Beer beerDto) {


        return webClient.put ().uri (uriBuilder -> uriBuilder.path (WebClientProperties.BEER_V1_PATH_GET_BY_ID)
                        .build (beerId))
                .body (BodyInserters.fromValue (beerDto))
                .retrieve ()
                .toBodilessEntity ();
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteBeerById (final UUID beerId) {
        return webClient.delete
                ().uri (uriBuilder -> uriBuilder.path (WebClientProperties.BEER_V1_PATH_GET_BY_ID)
                        .build (beerId))
                .retrieve ()
                .toBodilessEntity ();
    }
}
