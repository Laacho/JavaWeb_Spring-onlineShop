package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplyVoucherRequest {
    @NotBlank(message = "Voucher code cannot be empty!")
    private String voucherCode;

}
