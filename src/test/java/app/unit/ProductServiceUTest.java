package app.unit;

import app.exceptions.EntityNotFoundException;
import app.exceptions.ProductNameAlreadyExistsException;
import app.order_details.model.OrderDetails;
import app.order_details.service.OrderDetailsService;
import app.products.model.Category;
import app.products.model.Product;
import app.products.repository.ProductsRepository;
import app.products.service.ProductsService;
import app.user.service.UserService;
import app.web.dto.AddAProductRequest;
import app.web.dto.EditProductDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class ProductServiceUTest {

    @Mock
    private  ProductsRepository productsRepository;
    @Mock
    private  UserService userService;
    @Mock
    private  OrderDetailsService orderDetailsService;

    @InjectMocks
    private ProductsService productsService;


    @Test
    void givenAlreadyExistingProductName_whenAddProduct_ExceptionIsThrown() {
        AddAProductRequest addAProductRequest = new AddAProductRequest();
        addAProductRequest.setProductName("test");

        when(productsRepository.findByName(anyString())).thenReturn(Optional.of(new Product()));

        assertThrows(ProductNameAlreadyExistsException.class,()->productsService.addAProduct(addAProductRequest));
    }
    @Test
    void givenValidProduct_whenAddingProduct_thenSuccess() {
        AddAProductRequest addAProductRequest = new AddAProductRequest();
        addAProductRequest.setProductName("test");
        addAProductRequest.setProductImageURL("www.image.com");
        addAProductRequest.setPrice(BigDecimal.ZERO);
        addAProductRequest.setCategory(Category.OTHER);
        addAProductRequest.setQuantityPerUnit(10);
        addAProductRequest.setStockQuantity(100);
        addAProductRequest.setDescription("This is a test");

        Product product = Product.builder()
                .name("test")
                .photo("www.image.com")
                .price(BigDecimal.ZERO)
                .category(Category.OTHER)
                .quantityPerUnit(10)
                .stockQuantity(100)
                .description("This is a test")
                .build();

        when(productsRepository.findByName(anyString())).thenReturn(Optional.empty());

        productsService.addAProduct(addAProductRequest);

        assertThat(product).isNotNull();
        assertThat(product.getName()).isEqualTo(addAProductRequest.getProductName());
        assertThat(product.getPhoto()).isEqualTo(addAProductRequest.getProductImageURL());
        assertThat(product.getPrice()).isEqualTo(addAProductRequest.getPrice());
        assertThat(product.getCategory()).isEqualTo(addAProductRequest.getCategory());
        assertThat(product.getQuantityPerUnit()).isEqualTo(addAProductRequest.getQuantityPerUnit());
        assertThat(product.getStockQuantity()).isEqualTo(addAProductRequest.getStockQuantity());
        assertThat(product.getDescription()).isEqualTo(addAProductRequest.getDescription());

        verify(productsRepository, times(1)).findByName(anyString());
        verify(productsRepository, times(1)).save(any(Product.class));
    }
    @Test
    void givenInvalidProduct_whenGettingById_thenExceptionIsThrown() {
        UUID productId = UUID.randomUUID();
        when(productsRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,()->productsService.getById(productId));

    }
    @Test
    void givenValidProduct_whenGettingById_thenSuccess() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .name("test")
                .build();
        when(productsRepository.findById(productId)).thenReturn(Optional.of(product));

        Product byId = productsService.getById(productId);
        assertThat(byId).isNotNull();
        assertThat(byId.getName()).isEqualTo(product.getName());
    }
    @Test
    void givenValidProduct_whenUpdatingProduct_thenSuccess() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().build();
        EditProductDetails details = EditProductDetails.builder()
                .productName("test complete")
                .price(BigDecimal.ONE)
                .category(Category.DRINKS)
                .quantityPerUnit(11)
                .stockQuantity(101)
                .description("Test passed")
                .available(false)
                .image("www.image2.com")
                .build();


        when(productsRepository.findById(productId)).thenReturn(Optional.of(product));
        productsService.updateProduct(productId,details);

        assertThat(product.getName()).isEqualTo(details.getProductName());
        assertThat(product.getPrice()).isEqualTo(details.getPrice());
        assertThat(product.getCategory()).isEqualTo(details.getCategory());
        assertThat(product.getQuantityPerUnit()).isEqualTo(details.getQuantityPerUnit());
        assertThat(product.getStockQuantity()).isEqualTo(details.getStockQuantity());
        assertThat(product.getDescription()).isEqualTo(details.getDescription());
        assertThat(product.isAvailable()).isEqualTo(details.getAvailable());
        assertThat(product.getPhoto()).isEqualTo(details.getImage());

        verify(productsRepository, times(1)).findById(productId);
        verify(productsRepository,times(1)).save(any(Product.class));
    }

    @Test
    void givenOrderDetails_whenReducingItemQuantity_thenSuccess() {
        Product product = Product.builder()
                .stockQuantity(100)
                .build();

        OrderDetails orderDetails = OrderDetails.builder()
                .product(product)
                .quantity(10)
                .build();
        List<OrderDetails> orderDetailsList = List.of(orderDetails);

        productsService.reduceItemQuantity(orderDetailsList);

        assertThat(product.getStockQuantity()).isEqualTo(90);
        verify(productsRepository,times(orderDetailsList.size())).save(any(Product.class));
    }
    @Test
    void givenEmptyList_whenReducingItemQuantity_thenReturnEmptyList() {
        List<OrderDetails> emptyList = Collections.emptyList();

        productsService.reduceItemQuantity(emptyList);

        verify(productsRepository,times(0)).save(any(Product.class));
    }
}
