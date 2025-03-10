package app.shipment.model;

import app.orders.model.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)//check if it needs to be eager
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    private Carrier carrier;

    @Column(nullable = false)
    private LocalDateTime estimatedDeliveryDate;


    private LocalDateTime actualDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private BigDecimal shipmentPrice;
}
