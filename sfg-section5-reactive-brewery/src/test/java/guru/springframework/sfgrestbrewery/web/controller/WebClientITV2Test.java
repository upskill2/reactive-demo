package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
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
    public static final String BEER_UPC_PATH_V2 = "/api/v2/beerUpc/";

    WebClient webClient;

    @BeforeEach
    void setUp () {
        webClient = WebClient.builder ()
                .baseUrl (BASE_URL)
                .clientConnector (new ReactorClientHttpConnector (HttpClient.create ().wiretap (true)))
                .build ();
    }

    @Test
    void testUpdateBeer () throws InterruptedException {

        final String beerName = "JTs Beer";
        final Integer beerId = 1;

        CountDownLatch countDownLatch = new CountDownLatch (2);


        //update beer
        webClient.put ().uri (BeerRouterConfig.API_V2_POST_BEER + "/" + beerId)
                .accept (MediaType.APPLICATION_JSON)
                .body (BodyInserters
                        .fromValue (BeerDto.builder ()
                                .beerName (beerName)
                                .upc ("0631234200036")
                                .beerStyle ("PALE_ALE")
                                .price (new BigDecimal ("12.99"))
                                .build ()))
                .retrieve ().toBodilessEntity ()
                .subscribe (responseEntity -> {
                    assertThat (responseEntity.getStatusCode ().is2xxSuccessful ());
                    countDownLatch.countDown ();
                });


        //wait for update thread to happen
        countDownLatch.await (500, TimeUnit.MILLISECONDS);

        webClient.get ().uri (BeerRouterConfig.API_V2_POST_BEER + "/" + beerId)
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ()
                .bodyToMono (BeerDto.class)
                .subscribe (beer -> {
                    assertThat (beer).isNotNull ();
                    assertThat (beer.getBeerName ()).isNotNull ();
                    assertThat (beer.getBeerName ()).isEqualTo (beerName);
                    countDownLatch.countDown ();
                });

        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isZero ();
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
    void testGetBeerByUPC () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);
        String upc = "0631234200036";


        Mono<BeerDto> beerDtoMono = webClient.get ().uri (BEER_UPC_PATH_V2 + upc)
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
    void testGetBeerByUPCNotFound () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);
        String upc = "not_found";


        Mono<BeerDto> beerDtoMono = webClient.get ().uri (BEER_UPC_PATH_V2 + upc)
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ().bodyToMono (BeerDto.class);

        beerDtoMono.subscribe (beerDto -> {
        }, throwable -> {
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

    @Test
    void testSaveBeer () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);

        BeerDto beerDto = BeerDto.builder ()
                .beerName ("New Beer")
                .beerStyle ("ALE")
                .build ();

        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post ().uri (BEER_PATH_V2)
                .body (BodyInserters.fromValue (beerDto))
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ()
                .toBodilessEntity ();

        beerResponseMono.publishOn (Schedulers.parallel ())
                .doOnError (throwable -> {
                    log.error ("Error Occurred");
                    //   countDownLatch.countDown ();
                })
                .subscribe (responseEntity -> {
                    assertThat (responseEntity).isNotNull ();
                    assertThat (responseEntity.getStatusCode ().is2xxSuccessful ());
                    countDownLatch.countDown ();
                });

        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isEqualTo (0);
    }

    @Test
    void testSaveBeerBadRequest () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (2);

        BeerDto beerDto = BeerDto.builder ()
                .price (new BigDecimal ("12.99"))
                .build ();


        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post ().uri (BEER_PATH_V2)
                .contentType (MediaType.APPLICATION_JSON)
                .body (BodyInserters.fromValue (beerDto))
                .retrieve ().toBodilessEntity ();


        beerResponseMono.publishOn (Schedulers.parallel ())
                .subscribe (responseEntity -> {
                }, throwable -> {
                    countDownLatch.countDown ();
                });


        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isOne ();

    }

}
