package app.shipment.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum Carrier {
    EKONT("Ekont", BigDecimal.valueOf(5.00)),
    FEDEX("FedEx", BigDecimal.valueOf(15.00)),
    SPEEDY("Speedy", BigDecimal.valueOf(6.00)),
    BGPOSHTI("BGPOSHTI", BigDecimal.valueOf(10.00)),
    BGBOX("BGBOX", BigDecimal.valueOf(2.00));



    private final String name;
    private final BigDecimal price;

    Carrier(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    public BigDecimal getByName(String name){
        for (Carrier value : Carrier.values()) {
            if(value.name.equals(name)){
                return value.price;
            }
        }
        return BigDecimal.ZERO;
    }
}
