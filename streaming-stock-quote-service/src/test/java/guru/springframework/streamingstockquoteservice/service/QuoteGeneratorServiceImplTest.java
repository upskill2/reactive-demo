package guru.springframework.streamingstockquoteservice.service;

import guru.springframework.streamingstockquoteservice.model.Quote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class QuoteGeneratorServiceImplTest {

    QuoteGeneratorService service;

    @BeforeEach
    void setUp () {
        service = new QuoteGeneratorServiceImpl ();

    }

    @Test
    void fetchQuoteStream () throws InterruptedException {

        Flux<Quote> quoteFlux = service.fetchQuoteStream (Duration.ofMillis (100L));
        Consumer<Quote> quoteConsumer = System.out::println;
        Consumer<Throwable> throwableConsumer = e -> System.out.println (e.getMessage ());

        CountDownLatch countDownLatch = new CountDownLatch (1);
        Runnable done = countDownLatch::countDown;

        quoteFlux.take (30)
                .subscribe (quoteConsumer, throwableConsumer, done);

        countDownLatch.await ();

    }
}