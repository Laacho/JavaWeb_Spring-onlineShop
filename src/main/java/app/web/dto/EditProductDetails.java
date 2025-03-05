package app.web.dto;

import app.products.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
@Builder
public class EditProductDetails {

    @NotBlank(message = "Product name cannot be empty!")
    @Size(min = 5,max = 100,message = "Product name must be between 5 and 100 characters!")
    private String productName;

    @NotNull(message = "Price cannot be empty")
    @Positive(message = "Must a value greater or equal to 0")
    private BigDecimal price;

    @NotNull(message = "Must be a valid category")
    private Category category;

    @NotNull(message = "Quantity cannot be null!")
    @Positive(message = "The quantity must be greater or equal to 0")
    private int quantityPerUnit;

    @NotNull(message = "Quantity cannot be null!")
    @Positive(message = "The quantity must be greater or equal to 0")
    private int stockQuantity;

    @NotBlank(message = "Description cannot be empty!")
    @Size(min = 1,max=255,message = "Description must be between 1 and 255 characters!")
    private String description;


    private Boolean available;

    @URL(message = "Must be a valid url!")
    @NotBlank(message = "Cannot be empty url")
    private String image;
}
