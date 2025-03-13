package app.notifications.client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SendNotificationForAll {
    private String body;

    private String subject;

    private List<UUID> userId;

    private String status;
}
