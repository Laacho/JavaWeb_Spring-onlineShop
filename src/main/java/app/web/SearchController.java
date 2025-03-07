package app.web;


import app.products.model.Product;
import app.products.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {
    private final ProductsService productsService;
    @Autowired
    public SearchController( ProductsService productsService) {
        this.productsService = productsService;
    }

    @GetMapping
    public ModelAndView getSearchResults(@RequestParam("productName") String productName, RedirectAttributes redirectAttributes) {
        boolean isSearching = productName != null && !productName.isEmpty();
       List<Product> searchedProducts= productsService.searchByName(productName);
       redirectAttributes.addFlashAttribute("searchedProducts", searchedProducts);

        ModelAndView modelAndView = new ModelAndView("redirect:/home");
        modelAndView.addObject("isSearching", isSearching);
        modelAndView.addObject("productName", productName);
        return modelAndView;
    }
}
