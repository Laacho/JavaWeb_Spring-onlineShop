package app.web;


import app.products.model.Product;
import app.products.service.ProductsService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.HomePageSearchRequest;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class IndexController {

    private final UserService userService;
    private final ProductsService productsService;

    @Autowired
    public IndexController(UserService userService, ProductsService productsService) {
        this.userService = userService;
        this.productsService = productsService;
    }


    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }


    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error",required = false) String error) {
        ModelAndView modelAndView = new ModelAndView("login");
        if(error != null) {
            modelAndView.addObject("error", "Invalid username or password.");
        }
        return modelAndView;
    }



    @GetMapping("/register")
    public ModelAndView getRegisterPage() {
        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerRequest", new RegisterRequest());
        return modelAndView;
    }

    @PostMapping("/register")
    public String registerUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        userService.register(registerRequest);
        return "redirect:/login";
    }


    @GetMapping("/home")
    public ModelAndView getHomePage(@RequestParam (value = "productName", required = false) String productName,
                                    @AuthenticationPrincipal AuthenticationMetadata auth,
                                    @ModelAttribute("searchedProducts") List<Product> searchedProducts) {
        User user = userService.getById(auth.getUserId());

        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("user",user);
        modelAndView.addObject("searchRequest",new HomePageSearchRequest());

        boolean isSearching = productName != null && !productName.isEmpty();
        modelAndView.addObject("isSearching", isSearching);
        if (searchedProducts != null) {
            modelAndView.addObject("searchedProducts", searchedProducts);
        }
        List<Product> allProducts= productsService.findAllProductsDependingOnRole(auth.getRole());
        modelAndView.addObject("allProducts", allProducts);


        List<Product> recommendedProducts=productsService.getRecommendedProductsForUser(auth.getUserId());
        modelAndView.addObject("recommendedProducts", recommendedProducts);

        return modelAndView;
    }
}
