package app.api;

import app.products.model.Category;
import app.products.model.Product;
import app.products.service.DealsService;
import app.products.service.ProductsService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.ProductController;
import app.web.dto.AddAProductRequest;
import app.web.dto.EditProductDetails;
import app.web.mapper.DTOMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerApiTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductsService productsService;

    @MockitoBean
    private DealsService dealsService;
    @MockitoBean
    private UserService userService;
    @Mock
    private DTOMapper dtoMapper;


    @Test
    void testAddProduct() throws Exception {
        AddAProductRequest addAProductRequest = new AddAProductRequest();
        addAProductRequest.setProductName("New Product");
        addAProductRequest.setDescription("New Product Description");
        addAProductRequest.setPrice(BigDecimal.valueOf(29.99));
        addAProductRequest.setProductImageURL("image_url.jpg");
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);

        when(userService.getById(any())).thenReturn(User.builder().build());

        MockHttpServletRequestBuilder request = post("/products/add")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("productName", addAProductRequest.getProductName())
                .param("description", addAProductRequest.getDescription())
                .param("price", addAProductRequest.getPrice().toString())
                .param("productImageURL", addAProductRequest.getProductImageURL())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("addAproduct"))
                .andExpect(model().attribute("addAProductRequest", addAProductRequest));
    }

    @Test
    void testGetProductDetails() throws Exception {
        UUID id = UUID.randomUUID();
        Product product = Product.builder()
                .id(id)
                .name("Test Product")
                .description("Test Product Description")
                .price(BigDecimal.valueOf(29.99))
                .category(Category.OTHER)
                .build();

        when(productsService.getById(id)).thenReturn(product);
        when(userService.getById(any())).thenReturn(User.builder().role(UserRole.USER).build());

        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = get("/products/{id}/details", id)
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("productDetails"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", product));
    }

    @Test
    void testGetTodayDeals() throws Exception {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Deal 1")
                .description("Deal 1 Description")
                .price(BigDecimal.valueOf(9.99))
                .onDeal(true)
                .discountAmount(BigDecimal.valueOf(5.00))
                .category(Category.OTHER)
                .build();
        List<Product> dailyDeals = List.of(product);

        when(dealsService.getDailyDeals()).thenReturn(dailyDeals);
        when(userService.getById(any())).thenReturn(User.builder().build());

        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/products/today-deals")
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("todaysDeals"))
                .andExpect(model().attributeExists("dailyDeals"))
                .andExpect(model().attribute("dailyDeals", dailyDeals));
    }

    @Test
    void testEditProductSearch() throws Exception {
        //new Product(UUID.randomUUID(), "Test Product", "desc", BigDecimal.valueOf(20), "img.jpg")
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .description("Deal 1 Description")
                .price(BigDecimal.valueOf(20))
                .category(Category.OTHER)
                .build();
        List<Product> search = List.of(product);

        when(productsService.search(any())).thenReturn(search);
        when(userService.getById(any())).thenReturn(User.builder().build());

        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = get("/products/edit/search")
                .param("query", "Test Product")
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("editProduct"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attribute("search", search));
    }

    @Test
    void testUpdateProduct() throws Exception {
        UUID productId = UUID.randomUUID();

        Product product = Product.builder()
                .id(productId)
                .name("Original Product")
                .description("Original Description")
                .price(BigDecimal.valueOf(25.00))
                .category(Category.OTHER)
                .build();

        EditProductDetails editProductDetails = EditProductDetails.builder()
                .productName("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(39.99))
                .category(Category.DRINKS)
                .quantityPerUnit(10)
                .stockQuantity(100)
                .available(false)
                .image("http://example.com/profile.jpg")
                .build();

        when(userService.getById(any())).thenReturn(User.builder().build());
        when(productsService.getById(any())).thenReturn(product);
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = put("/products/{id}/edit", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("productName", editProductDetails.getProductName())
                .param("description", editProductDetails.getDescription())
                .param("price", editProductDetails.getPrice().toString())
                .param("category", editProductDetails.getCategory().toString())
                .param("quantityPerUnit", String.valueOf(editProductDetails.getQuantityPerUnit()))
                .param("stockQuantity", String.valueOf(editProductDetails.getStockQuantity()))
                .param("available", String.valueOf(editProductDetails.getAvailable()))
                .param("image", editProductDetails.getImage())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(productsService, times(1)).updateProduct(any(UUID.class), any(EditProductDetails.class));
    }


    @Test
    void givenInvalidProductDetails_whenUpdatingProduct_thenReturnsTheViewWithErrors() throws Exception {
        UUID productId = UUID.randomUUID();

        Product product = Product.builder()
                .id(productId)
                .name("Original Product")
                .description("Original Description")
                .price(BigDecimal.valueOf(25.00))
                .category(Category.OTHER)
                .build();

        EditProductDetails editProductDetails = EditProductDetails.builder()
                .productName("")
                .description("Updated Description")
                .price(BigDecimal.valueOf(-10))
                .category(Category.DRINKS)
                .quantityPerUnit(10)
                .stockQuantity(-5)
                .available(false)
                .image("not-a-url")
                .build();

        when(userService.getById(any())).thenReturn(User.builder().build());
        when(productsService.getById(any())).thenReturn(product);
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = put("/products/{id}/edit", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("productName", editProductDetails.getProductName())
                .param("description", editProductDetails.getDescription())
                .param("price", editProductDetails.getPrice().toString())
                .param("category", editProductDetails.getCategory().toString())
                .param("quantityPerUnit", String.valueOf(editProductDetails.getQuantityPerUnit()))
                .param("stockQuantity", String.valueOf(editProductDetails.getStockQuantity()))
                .param("available", String.valueOf(editProductDetails.getAvailable()))
                .param("image", editProductDetails.getImage())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("editProductDetails"))
                .andExpect(model().attributeExists("editProductDetails"))
                .andExpect(model().attributeHasFieldErrors(
                        "editProductDetails", "productName", "price", "stockQuantity", "image"))
                .andExpect(model().attributeExists("product"));

        verify(productsService, never()).updateProduct(any(UUID.class), any(EditProductDetails.class));
    }

    @Test
    void testAddProductPage() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = get("/products/add")
                .with(user(principal))
                .with(csrf());
        when(userService.getById(any())).thenReturn(User.builder().build());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("addAproduct"))
                .andExpect(model().attributeExists("addAProductRequest"));
    }

    @Test
    void testAddProductPageAccessDenied() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/products/add")
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("status", "message"));
    }

    @Test
    void testGetEditProductPage() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = get("/products/edit")
                .with(user(principal))
                .with(csrf());
        when(userService.getById(any())).thenReturn(User.builder().build());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("editProduct"))
                .andExpect(model().attributeExists("adminSearchRequest"));
    }

    @Test
    void testGetEditProductPageAccessDenied() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/products/edit")
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("status", "message"));
    }


    @Test
    void testLoadSpecificProduct() throws Exception {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .name("Sample Product")
                .description("Sample Description")
                .price(BigDecimal.valueOf(29.99))
                .category(Category.SWEETS)
                .build();
        when(userService.getById(any())).thenReturn(User.builder().build());
        when(productsService.getById(productId)).thenReturn(product);
        EditProductDetails details = EditProductDetails.builder()
                .productName(product.getName())
                .price(product.getPrice())
                .quantityPerUnit(product.getQuantityPerUnit())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .description(product.getDescription())
                .available(product.isAvailable())
                .image(product.getPhoto())
                .build();
        try (MockedStatic<DTOMapper> mockedMapper = mockStatic(DTOMapper.class)) {
            mockedMapper.when(() -> DTOMapper.mapProductToProductEditDetails(product))
                    .thenReturn(details);

            AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);

            MockHttpServletRequestBuilder request = get("/products/{id}/edit", productId)
                    .with(user(principal))
                    .with(csrf());

            mockMvc.perform(request)
                    .andExpect(status().isOk())
                    .andExpect(view().name("editProductDetails"))
                    .andExpect(model().attributeExists("product"))
                    .andExpect(model().attributeExists("editProductDetails"));
        }
    }

}
