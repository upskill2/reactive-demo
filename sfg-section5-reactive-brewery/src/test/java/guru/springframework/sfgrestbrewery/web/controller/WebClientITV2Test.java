package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by jt on 3/7/21.
 */

@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class WebClientITV2Test {

    public static final String BASE_URL = "http://localhost:8080";
    public static final String BEER_PATH_V2 = "/api/v2/beer/";

    WebClient webClient;

    @BeforeEach
    void setUp () {
        webClient = WebClient.builder ()
                .baseUrl (BASE_URL)
                .clientConnector (new ReactorClientHttpConnector (HttpClient.create ().wiretap (true)))
                .build ();
    }

     @Test
    void testGetBeerById () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);

        Mono<BeerDto> beerDtoMono = webClient.get ().uri (BEER_PATH_V2 + 1)
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ().bodyToMono (BeerDto.class);

        beerDtoMono.subscribe (beerDto -> {
            assertThat (beerDto).isNotNull ();
            assertThat (beerDto.getBeerName ()).isNotBlank ();
            countDownLatch.countDown ();
        });

        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isEqualTo (0);
    }

    @Test
    void testGetBeerByIdNotFound () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);

        Mono<BeerDto> beerDtoMono = webClient.get ().uri (BEER_PATH_V2 + 999)
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ().bodyToMono (BeerDto.class);

        beerDtoMono.subscribe (beerDto -> {
        }, throwable -> {
            assertThat (throwable instanceof WebClientResponseException).isTrue ();
            WebClientResponseException exception = (WebClientResponseException) throwable;
            assertThat (exception.getStatusCode ()).isEqualTo (HttpStatus.NOT_FOUND);
            countDownLatch.countDown ();
        });

        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isEqualTo (0);
    }


}
