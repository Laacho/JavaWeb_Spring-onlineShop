package app.web;

import app.products.model.Product;
import app.products.service.DealsService;
import app.products.service.ProductsService;
import app.web.dto.AddAProductRequest;
import app.web.dto.AdminSearchRequest;
import app.web.dto.EditProductDetails;
import app.web.mapper.DTOMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductsService productsService;
    private final DealsService dealsService;

    @Autowired
    public ProductController(ProductsService productsService,DealsService dealsService) {
        this.productsService = productsService;
        this.dealsService = dealsService;
    }


    @GetMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView addProduct() {
        ModelAndView modelAndView = new ModelAndView("addAproduct");
        modelAndView.addObject("addAProductRequest", new AddAProductRequest());
        return modelAndView;
    }

    @PostMapping("/add")
    public String addProduct(@Valid AddAProductRequest addAProductRequest, BindingResult bindingResult)  {
        if(bindingResult.hasErrors()) {
            return "addAproduct";
        }
        productsService.addAProduct(addAProductRequest);
        return "redirect:/home";
    }

    @GetMapping("/{id}/details")
    public ModelAndView getProductDetails(@PathVariable UUID id,@RequestParam(required = false,defaultValue = "false") boolean added) {
        Product product = productsService.getById(id);
        ModelAndView modelAndView = new ModelAndView("productDetails");
        if(added) {
            modelAndView.addObject("added", added);
        }
        modelAndView.addObject("product", product);
        return modelAndView;
    }

    @GetMapping("/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getEditProductPage(){
        ModelAndView modelAndView = new ModelAndView("editProduct");
        modelAndView.addObject("adminSearchRequest",new AdminSearchRequest());
        return modelAndView;
    }
    @GetMapping("/edit/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView editProductSearch(@Valid AdminSearchRequest adminSearchRequest){
        ModelAndView modelAndView = new ModelAndView("editProduct");
        List<Product> search = productsService.search(adminSearchRequest);
        modelAndView.addObject("search", search);
        return modelAndView;
    }
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView loadSpecificProducts(@PathVariable UUID id){
        Product product= productsService.getById(id);
        ModelAndView modelAndView = new ModelAndView("editProductDetails");
        modelAndView.addObject("product", product);
        modelAndView.addObject("editProductDetails", DTOMapper.mapProductToProductEditDetails(product));
        return modelAndView;
    }

    @PutMapping("/{id}/edit")
    public ModelAndView updateProduct(@PathVariable UUID id,@Valid EditProductDetails editProductDetails,BindingResult bindingResult){
        if(bindingResult.hasErrors()) {
            ModelAndView modelAndView=new ModelAndView("editProductDetails");
            modelAndView.addObject("editProductDetails", editProductDetails);
            Product product = productsService.getById(id);
            modelAndView.addObject("product", product);
            return modelAndView;
        }
        productsService.updateProduct(id,editProductDetails);

        return new ModelAndView("redirect:/home");
    }
    @GetMapping("/today-deals")
    public ModelAndView getTodayDeals() {
        ModelAndView modelAndView = new ModelAndView("todaysDeals");
        List<Product> dailyDeals= dealsService.getDailyDeals();
        modelAndView.addObject("dailyDeals", dailyDeals);
        return modelAndView;
    }
}
