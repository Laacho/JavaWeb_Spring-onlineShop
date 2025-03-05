package app.web.dto;

import app.products.model.Category;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
@Getter
@Setter
public class AddAProductRequest {

    @NotBlank(message = "Product name cannot be empty!")
    @Size(min = 5,max = 100,message = "Product name must be between 5 and 100 characters!")
    private String productName;

    @URL(message = "Must be a valid url!")
    @NotBlank(message = "Cannot be empty url")
    private String productImageURL;

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
    @Size(min = 10,max=255,message = "Description must be between 10 and 255 characters!")
    private String description;
}
