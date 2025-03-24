package app.util;

import app.products.model.Product;
import app.user.model.User;
import lombok.experimental.UtilityClass;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class ProductUtility {
    public static void sortProductsByUser(User user){
        Map<Product, Integer> collect = user.getShoppingCart().getProducts()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getName())) // Sort by timestamp
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
        user.getShoppingCart().setProducts(collect);
    }

    public static List<Product> sortRecommendedProductsForUser(List<Product> products){
        return products.stream()
                .sorted(Comparator.comparing(Product::getName)
                        .thenComparing(Product::getPrice))
                .toList();
    }
}
