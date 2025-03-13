package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "Subject cannot be blank")
    private String subject;
    @NotBlank(message = "Body cannot be blank")
    private String body;

    private String username;
}
