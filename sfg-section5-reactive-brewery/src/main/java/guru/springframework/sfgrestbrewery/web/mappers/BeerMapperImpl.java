package guru.springframework.sfgrestbrewery.web.mappers;

import guru.springframework.sfgrestbrewery.domain.Beer;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BeerMapperImpl implements BeerMapper {
    @Override
    public BeerDto beerToBeerDto (final Beer beer) {
        if (beer == null) return null;

        return BeerDto.builder ()
                .id (beer.getId ())
                .beerName (beer.getBeerName ())
                .beerStyle (beer.getBeerStyle () != null ? beer.getBeerStyle ().name () : null)
                .upc (beer.getUpc ())
                .price (beer.getPrice ())
                .quantityOnHand (beer.getQuantityOnHand ())
                .createdDate (beer.getCreatedDate ())
                .lastUpdatedDate (beer.getLastModifiedDate ())
                .build ();

    }

    @Override
    public BeerDto beerToBeerDtoWithInventory (final Beer beer) {
        if (beer == null) return null;

        return BeerDto.builder ()
                .id (beer.getId ())
                .beerName (beer.getBeerName ())
                .beerStyle (beer.getBeerStyle () != null ? beer.getBeerStyle ().name () : null)
                .upc (beer.getUpc ())
                .price (beer.getPrice ())
                .quantityOnHand (beer.getQuantityOnHand ())
                .createdDate (beer.getCreatedDate ())
                .lastUpdatedDate (beer.getLastModifiedDate ())
                .build ();
    }

    @Override
    public Beer beerDtoToBeer (final BeerDto dto) {


        return Beer.builder ()
                .id (dto.getId ())
                .beerName (dto.getBeerName ())
                .beerStyle (dto.getBeerStyle () != null ? BeerStyleEnum.valueOf (dto.getBeerStyle ()) : null)
                .upc (dto.getUpc ())
                .price (dto.getPrice ())
                .quantityOnHand (dto.getQuantityOnHand ())
                .createdDate (dto.getCreatedDate ())
                .lastModifiedDate (dto.getLastUpdatedDate ())
                .build ();
    }
}
