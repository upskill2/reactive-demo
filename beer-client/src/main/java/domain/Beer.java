package domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class Beer {

    private String beerName;
    private String upc;

    private Integer quantityOnHand;
    private BigDecimal price;

}
