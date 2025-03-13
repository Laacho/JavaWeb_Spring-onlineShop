package app.notifications.client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateNotificationRequest {
    private String body;

    private String subject;

    private UUID userId;

    private String status;
}
