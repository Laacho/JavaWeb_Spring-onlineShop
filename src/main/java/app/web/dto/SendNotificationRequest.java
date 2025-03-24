package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendNotificationRequest {

    @NotBlank(message = "Subject cannot be blank")
    private String subject;
    @NotBlank(message = "Body cannot be blank")
    private String body;

    private String username;
}
