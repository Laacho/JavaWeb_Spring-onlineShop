package app.products.model;

import app.products.service.DealsService;
import app.products.service.ProductsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@Slf4j
public class TodaysDealInit implements CommandLineRunner {
    private final DealsService dealsService;

    @Autowired
    public TodaysDealInit(DealsService dealsService) {
        this.dealsService = dealsService;
    }

    @Override
    public void run(String... args) throws Exception {
       try {
           if (dealsService.getDailyDeals().isEmpty()) {
               dealsService.generateDailyDeals();
           }
       }catch (Exception e){
           log.error("Something went wrong with today's deal init: " + e.getMessage());
       }

    }
}
