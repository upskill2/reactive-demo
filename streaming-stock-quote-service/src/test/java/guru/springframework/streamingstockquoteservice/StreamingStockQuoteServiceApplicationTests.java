package guru.springframework.streamingstockquoteservice;

import guru.springframework.streamingstockquoteservice.model.Quote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StreamingStockQuoteServiceApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void fetchQuotes () {
        webTestClient.get ()
                .uri ("/quotes?size=20")
                .accept (MediaType.APPLICATION_JSON)
                .exchange ()
                .expectStatus ().isOk ()
                .expectHeader ().contentType (MediaType.APPLICATION_JSON)
                .expectBodyList (Quote.class)
                .hasSize (20)
                .consumeWith (allQuotes -> {
                    assertThat (allQuotes.getResponseBody ())
                            .allSatisfy (quote -> assertThat (quote.getPrice ()).isPositive ());
                    Quote quote = allQuotes.getResponseBody ().get (0);
                    assert quote.getPrice ().compareTo (BigDecimal.ZERO) > 0;
                    assertThat (allQuotes.getResponseBody ())
                            .hasSize (20)
                            .allSatisfy (quote1 -> assertThat (quote1.getPrice ()).isPositive ());
                });
    }

    @Test
    void testStreamQuotes () throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch (10);

        webTestClient.get ()
                .uri ("/quotes")
                .accept (MediaType.APPLICATION_NDJSON)
                .exchange ()
                .returnResult (Quote.class)
                .getResponseBody ()
                .take (10)
                .subscribe (quote -> {
                    assertThat (quote.getPrice ()).isPositive ();
                    countDownLatch.countDown ();
                });

        countDownLatch.await ();
    }


    @Test
    void contextLoads () {
    }

}
