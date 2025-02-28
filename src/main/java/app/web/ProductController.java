package app.web;

import app.products.model.Product;
import app.products.service.DealsService;
import app.products.service.ProductsService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.AddAProductRequest;
import app.web.dto.AdminSearchRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductsService productsService;
    private final UserService userService;
    private final DealsService dealsService;

    @Autowired
    public ProductController(ProductsService productsService, UserService userService, DealsService dealsService) {
        this.productsService = productsService;
        this.userService = userService;
        this.dealsService = dealsService;
    }


    @GetMapping("/add")
//    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView addProduct(@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user=userService.getById(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("addAproduct");
        modelAndView.addObject("addAProductRequest", new AddAProductRequest());
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView addProduct(@Valid AddAProductRequest addAProductRequest, BindingResult bindingResult,@AuthenticationPrincipal AuthenticationMetadata auth)  {
        User user = userService.getById(auth.getUserId());
        if(bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("addAproduct");
            modelAndView.addObject("user", user);
            return modelAndView;
        }
        productsService.addAproduct(addAProductRequest);
        ModelAndView modelAndView = new ModelAndView("redirect:/home");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("/{id}/details")
    public ModelAndView getProductDetails(@PathVariable UUID id,@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());
        Product product = productsService.getById(id);
        ModelAndView modelAndView = new ModelAndView("productDetails");
        modelAndView.addObject("user",user);
        modelAndView.addObject("product", product);
        return modelAndView;
    }

    @GetMapping("/edit")
    public ModelAndView getEditProductPage(@AuthenticationPrincipal AuthenticationMetadata auth){
        User user = userService.getById(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("editProduct");
        modelAndView.addObject("user", user);
        modelAndView.addObject("adminSearchRequest",new AdminSearchRequest());
        return modelAndView;
    }
    @GetMapping("/edit/search")
    public ModelAndView editProductSearch(@Valid AdminSearchRequest adminSearchRequest,@AuthenticationPrincipal AuthenticationMetadata auth){
        User user = userService.getById(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("editProduct");
        modelAndView.addObject("user", user);

        List<Product> search = productsService.search(adminSearchRequest);
        modelAndView.addObject("search", search);
        return modelAndView;
    }

    @GetMapping("/today-deals")
    public ModelAndView getTodayDeals(@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView("todaysDeals");
        modelAndView.addObject("user", user);
       List<Product> dailyDeals= dealsService.getDailyDeals();
        modelAndView.addObject("dailyDeals", dailyDeals);
        return modelAndView;
    }
}
