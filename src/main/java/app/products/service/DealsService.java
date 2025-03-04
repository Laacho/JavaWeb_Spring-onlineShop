package app.products.service;

import app.products.model.Product;
import app.products.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class DealsService {

    private final ProductsRepository productsRepository;
    private static final int DEAL_COUNT = 8;

    @Autowired
    public DealsService(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @Scheduled(cron = "@daily")
    @Transactional
    public void generateDailyDeals() {
        productsRepository.removeAllDeals();
        List<Product> all = productsRepository.findAll();
        List<Product> products = pickRandomDeals(all);
        productsRepository.saveAll(products);
    }

    public List<Product> getDailyDeals() {
        return productsRepository.findByIsOnDeal(true);
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
}
