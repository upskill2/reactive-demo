package com.rabbitstockquoteservice.service;

import com.rabbitmq.client.Delivery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.Receiver;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.rabbitstockquoteservice.config.RabbitConfig.QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuoteRunner implements CommandLineRunner {

    private final QuoteMessageSender quoteMessageSender;
    private final QuoteGeneratorService quoteGeneratorService;
    private final Receiver receiver;

    @Override
    public void run (final String... args) throws Exception {
        CountDownLatch latch = new CountDownLatch (25);
        AtomicInteger senderCounter = new AtomicInteger ();

        quoteGeneratorService.fetchQuoteStream (Duration.ofMillis (100))
                .take (25)
                .log ("Got Quote #" + senderCounter.getAndIncrement ())
                .flatMap (quoteMessageSender::sendQuoteMessage)
                .subscribe (result -> {
                            log.info ("Sent Message to Rabbit #{}", senderCounter.getAndIncrement ());
                            latch.countDown ();
                        }, throwable -> {
                            log.error ("Got Error", throwable);
                        }, () -> {
                            log.info ("Send Message to Rabbit Complete");
                        }
                );

        latch.await (1, TimeUnit.SECONDS);

        AtomicInteger receivedCount = new AtomicInteger ();

        receiver.consumeAutoAck (QUEUE)
                .log ("Msg Received")
                .subscribe (msg -> {
                    log.info ("Received Message #{}: {}", receivedCount.incrementAndGet (), new String (msg.getBody ()));
                }, throwable -> {
                    log.error ("Got Error", throwable);
                }, () -> {
                    log.info ("Receive Complete");
                });

    }
}
