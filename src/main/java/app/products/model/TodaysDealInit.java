package app.products.model;

import app.products.service.DealsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class TodaysDealInit implements CommandLineRunner {
    private final DealsService dealsService;

    @Autowired
    public TodaysDealInit(DealsService dealsService) {
        this.dealsService = dealsService;
    }

    @Override
    public void run(String... args) throws Exception {
        if(dealsService.getDailyDeals().isEmpty()) {
            dealsService.generateDailyDeals();
        }
    }
}
