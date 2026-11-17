package ch.dboeckli.spring.datarest.web;

import ch.dboeckli.spring.datarest.entity.Beer;
import ch.dboeckli.spring.datarest.entity.BeerStyleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BeerForm {

    private UUID id;

    private Long version;

    private String beerName;

    private BeerStyleEnum beerStyle;

    private String upc;

    private Integer quantityOnHand;

    private BigDecimal price;

    private Timestamp createdDate;

    private Timestamp lastModifiedDate;

    public static BeerForm from(Beer beer) {
        return BeerForm.builder()
            .id(beer.getId())
            .version(beer.getVersion())
            .beerName(beer.getBeerName())
            .beerStyle(beer.getBeerStyle())
            .upc(beer.getUpc())
            .quantityOnHand(beer.getQuantityOnHand())
            .price(beer.getPrice())
            .createdDate(beer.getCreatedDate())
            .lastModifiedDate(beer.getLastModifiedDate())
            .build();
    }

}
