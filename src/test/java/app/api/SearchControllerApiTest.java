package app.api;

import app.products.model.Product;
import app.products.service.ProductsService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.SearchController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
public class SearchControllerApiTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductsService productsService;

    @MockitoBean
    private UserService userService;

    @Test
    void getSearchResults_WithValidProductName_ShouldRedirectToHomeWithProducts() throws Exception {
        String productName = "Laptop";
        List<Product> mockProducts = List.of(new Product(), new Product());

        when(productsService.searchByName(productName)).thenReturn(mockProducts);
        when(userService.getById(any(UUID.class))).thenReturn(User.builder().build());

        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = get("/search")
                .param("productName", productName)
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/home?isSearching=*"))
                .andExpect(flash().attribute("searchedProducts", mockProducts))
                .andExpect(model().attribute("isSearching", "true"))
                .andExpect(model().attribute("productName", productName));

        verify(productsService, times(1)).searchByName(productName);
    }

    @Test
    void testGetSearchResults_WithEmptyProductName_ShouldRedirectToHomeWithEmptyList() throws Exception {
        String productName = "";
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = get("/search")
                .param("productName", productName)
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/home?isSearching=*"))
                .andExpect(flash().attribute("searchedProducts", List.of()))
                .andExpect(model().attribute("isSearching", "false"))
                .andExpect(model().attribute("productName", productName));

        verify(productsService, times(1)).searchByName(productName);
    }


}
