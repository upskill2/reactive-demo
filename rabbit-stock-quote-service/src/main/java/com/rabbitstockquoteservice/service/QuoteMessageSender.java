package com.rabbitstockquoteservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitstockquoteservice.model.Quote;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.OutboundMessageResult;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

import java.util.concurrent.atomic.AtomicInteger;

import static com.rabbitstockquoteservice.config.RabbitConfig.QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuoteMessageSender {

    private final Sender sender;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Mono<Void> sendQuoteMessage (Quote quote) {
        byte[] jsonBytes = objectMapper.writeValueAsBytes (quote);

        Flux<OutboundMessageResult> confirmations = sender.sendWithPublishConfirms (
                Flux.just (new OutboundMessage ("", QUEUE, jsonBytes)));

        sender.declareQueue (QueueSpecification.queue (QUEUE))
                .thenMany (confirmations)
                .doOnError (err-> {
                    log.error ("Error: " + err.getMessage ());
                })
                .subscribe (r -> {
                    if (r.isAck ()) {
                        log.info ("Message Sent Successfully");
                    } else {
                        log.error ("Message Not Sent");
                    }
                });

        return Mono.empty ();
    }
}
