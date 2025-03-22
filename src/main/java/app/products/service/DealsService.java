package app.products.service;

import app.notifications.service.NotificationService;
import app.products.model.Product;
import app.web.dto.SendNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class DealsService {

    private final ProductsService productsService;
    private final NotificationService notificationService;
    private static final int DEAL_COUNT = 8;

    @Autowired
    public DealsService(ProductsService productsService, NotificationService notificationService) {
        this.productsService = productsService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "@daily")
    @Transactional
    public void generateDailyDeals() {
        productsService.removeAllDeals();
        List<Product> all = productsService.findAll();
        List<Product> products = pickRandomDeals(all);
        productsService.saveAll(products);
        sendNotificationToALL(products);
    }

    public List<Product> getDailyDeals() {
       return productsService.findAllProductsOnDeal();
    }


    private List<Product> pickRandomDeals(List<Product> all) {
        Random random = new Random();
        Collections.shuffle(all, random);

        List<Product> collect = all.stream()
                .limit(DEAL_COUNT)
                .toList();
        for (Product product : collect) {
            double discountPercentage = generateRandomDiscount();
            applyDiscountToProduct(product, discountPercentage);
        }

        return collect;
    }
    private void applyDiscountToProduct(Product product, double discountPercentage) {
        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountAmount  = originalPrice.multiply(new BigDecimal(discountPercentage / 100));
        BigDecimal discountedPrice = originalPrice.subtract(discountAmount);
        product.setPrice(discountedPrice);
        product.setDiscountAmount(discountAmount);
        product.setOnDeal(true);
    }

    private double generateRandomDiscount() {
        Random random = new Random();
        return 10 + (40 * random.nextDouble());
    }
    private void sendNotificationToALL(List<Product> products) {
        SendNotificationRequest sendNotificationRequest = buildRequest(products);
        try {
            ResponseEntity<Void> voidResponseEntity = notificationService.publishNotificationToAllUsers(sendNotificationRequest);
            if (!voidResponseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed].Couldn't send notification to all users");
            }
        }catch (Exception e) {
            log.error("[Feign call to notification-svc failed].Couldn't send notification to all users");
        }
    }

    private SendNotificationRequest buildRequest(List<Product> products) {
        SendNotificationRequest sendNotificationRequest=new SendNotificationRequest();
        sendNotificationRequest.setSubject("New deals available");
        StringBuilder sb=new StringBuilder();
        sb.append("""
                Discount Alert!
                The following products are on discount today:
                
                """);
        for (Product product : products) {
            sb.append("\n");
            sb.append(product.getName()).append(" with Category: ");
            sb.append(product.getCategory());
        }
        sb.append("Head over to the Today's Deals tab and check them out!");
        sendNotificationRequest.setBody(sb.toString());
        return sendNotificationRequest;
    }
}
