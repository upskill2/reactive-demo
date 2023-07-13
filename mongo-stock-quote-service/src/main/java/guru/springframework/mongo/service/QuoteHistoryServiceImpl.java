package guru.springframework.mongo.service;

import guru.springframework.mongo.domain.QuoteHistory;
import guru.springframework.mongo.model.Quote;
import guru.springframework.mongo.repository.QuoteHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QuoteHistoryServiceImpl implements QuoteHistoryService {

    private final QuoteHistoryRepository repository;
    @Override
    public Mono<QuoteHistory> saveQuoteToMongo (final Quote quote) {
        return repository.save (QuoteHistory.builder ()
                .price (quote.getPrice ())
                .ticker (quote.getTicker ())
                .instant (quote.getInstant ())
                .build ());
    }
}
