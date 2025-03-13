package app.notifications.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String body;

    private String subject;

    private String status;

    private LocalDateTime createdOn;
}
