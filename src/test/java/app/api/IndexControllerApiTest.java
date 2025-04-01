package app.api;

import app.interceptors.UserInterceptor;
import app.products.model.Product;
import app.products.service.ProductsService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.IndexController;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ProductsService productsService;

    @InjectMocks
    private UserInterceptor userInterceptor;



    @Test
    void getRequestToIndexEndpoint_shouldReturnIndexPage() throws Exception {
        MockHttpServletRequestBuilder request= get("/");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginPage() throws Exception {
        MockHttpServletRequestBuilder request= get("/register");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }
    @Test
    void getRequestToLoginEndpoint_shouldReturnLoginView() throws Exception {
        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewAndErrorMessageAttribute() throws Exception {
        MockHttpServletRequestBuilder request = get("/login")
                .param("error", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists( "error"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testGetHomePage_NoProductSearch() throws Exception {

        List<Product> allProducts = List.of(Product.builder().name("Product3").build());
        List<Product> recommendedProducts = List.of(Product.builder().name("RecommendedProduct1").build());

        AuthenticationMetadata auth = new AuthenticationMetadata(UUID.randomUUID(), "testUser", "password", UserRole.USER, true);
        when(productsService.findAllProductsDependingOnRole(auth.getRole())).thenReturn(allProducts);
        when(productsService.getRecommendedProductsForUser(auth.getUserId())).thenReturn(recommendedProducts);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(auth);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        User mockUser = new User();
        when(userService.getById(any())).thenReturn(mockUser);

        MockHttpServletRequestBuilder request = get("/home")
                .with(user(auth));
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("isSearching"))
                .andExpect(model().attribute("isSearching", false))
                .andExpect(model().attributeExists("allProducts"))
                .andExpect(model().attribute("allProducts", allProducts))
                .andExpect(model().attributeExists("recommendedProducts"))
                .andExpect(model().attribute("recommendedProducts", recommendedProducts));
    }
}
