import domain.Person;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PersonRepositoryImpl implements PersonRepository {

    Person a0 = new Person (1, "A", "B");
    Person a1 = new Person (2, "C", "D");
    Person a2 = new Person (3, "E", "D");
    Person a3 = new Person (4, "G", "I");

    @Override
    public Mono<Person> findById (Integer id) {
        Flux<Person> personFlux = findAll ();
        return personFlux.filter (p->p.getId ()==id).next ();
    }

    @Override
    public Flux<Person> findAll () {
        return Flux.just (a0, a1, a2, a3);
    }
}
