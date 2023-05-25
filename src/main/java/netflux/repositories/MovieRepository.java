package netflux.repositories;

import netflux.domain.Movie;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

public interface MovieRepository extends ReactiveMongoRepository<Movie, String> {


}
