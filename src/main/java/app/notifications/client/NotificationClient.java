package app.notifications.client;

import app.notifications.client.dto.CreateNotificationRequest;
import app.notifications.client.dto.NotificationResponse;
import app.notifications.client.dto.SendNotificationForAll;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@FeignClient(name = "notification-svc",url = "http://localhost:8082/api/v1/notifications")
public interface NotificationClient {

    @PostMapping
    ResponseEntity<NotificationResponse> publishNotification(@RequestBody CreateNotificationRequest request);

    @PostMapping("/all")
    ResponseEntity<Void> publishNotificationForAll(@RequestBody SendNotificationForAll sendNotificationForAll);

    @GetMapping("/history")
    ResponseEntity<List<NotificationResponse>> getNotificationHistory(@RequestParam(name = "userId") UUID userId);

    @DeleteMapping("/delete")
    ResponseEntity<Void> deleteNotification(@RequestParam(name = "userId") UUID userId);
}
