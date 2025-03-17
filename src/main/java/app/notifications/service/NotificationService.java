package app.notifications.service;

import app.notifications.client.NotificationClient;
import app.notifications.client.dto.CreateNotificationRequest;
import app.notifications.client.dto.NotificationResponse;
import app.notifications.client.dto.SendNotificationForAll;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.SendNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {
    private final NotificationClient notificationClient;
    private final UserService userService;
    private static final int MAX_NUMBER_OF_NOTIFICATIONS_TO_BE_DISPLAYED = 15;

    public NotificationService(NotificationClient notificationClient, UserService userService) {
        this.notificationClient = notificationClient;
        this.userService = userService;
    }


    public void sendNotificationWithTrackingNumber(String trackingNumber, UUID userId) {
        String body = "Your order has been sent to " + trackingNumber + ". You can use this number to track it.";
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .subject("You have successfully placed an order!")
                .body(body)
                .userId(userId)
                .status("SUCCEEDED")
                .build();


        ResponseEntity<NotificationResponse> notificationResponseResponseEntity = notificationClient.publishNotification(request);
        if (!notificationResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to notification-svc failed] Couldn't post notification for user with id [%s]".formatted(userId));
        }
    }


    public void publishNotification(SendNotificationRequest sendNotificationRequest) {
        if (!sendNotificationRequest.getUsername().isBlank()) {
            //username is not blank so send to specific user
            publishNotificationForSpecificUser(sendNotificationRequest);
        } else {
            //username is blank so send to everyone
            ResponseEntity<Void> notificationResponseResponseEntity = publishNotificationToAllUsers(sendNotificationRequest);
            if (!notificationResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed].Couldn't send notification to all users");
            }
        }
    }

    private void publishNotificationForSpecificUser(SendNotificationRequest sendNotificationRequest) {
        User user = userService.getByUsername(sendNotificationRequest.getUsername());

        CreateNotificationRequest succeeded = CreateNotificationRequest.builder()
                .body(sendNotificationRequest.getBody())
                .status(sendNotificationRequest.getSubject())
                .userId(user.getId())
                .status("SUCCEEDED")
                .build();
        ResponseEntity<NotificationResponse> notificationResponseResponseEntity = notificationClient.publishNotification(succeeded);
        if (!notificationResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to notification-svc failed] Couldn't post notification for user with id [%s]".formatted(user.getId()));

        }
    }

    public ResponseEntity<Void> publishNotificationToAllUsers(SendNotificationRequest sendNotificationRequest) {
        List<UUID> userId = userService.getAllUsers().stream().map(User::getId).toList();
        SendNotificationForAll request = SendNotificationForAll.builder()
                .body(sendNotificationRequest.getBody())
                .subject(sendNotificationRequest.getSubject())
                .status("SUCCEEDED")
                .userId(userId)
                .build();
        return notificationClient.publishNotificationForAll(request);

    }

    public List<NotificationResponse> getAllUserNotifications(UUID id) {
        ResponseEntity<List<NotificationResponse>> notificationHistory = notificationClient.getNotificationHistory(id);
        if (!notificationHistory.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to notification-svc failed] Couldn't get notification for user with id [%s]".formatted(id));
        }
        return Objects.requireNonNull(notificationHistory.getBody())
                .stream()
                .limit(MAX_NUMBER_OF_NOTIFICATIONS_TO_BE_DISPLAYED)
                .toList();
    }

    public void deleteNotifications(UUID userId) {
        ResponseEntity<Void> voidResponseEntity = notificationClient.deleteNotification(userId);
        if (!voidResponseEntity.getStatusCode().is2xxSuccessful()) {
            log.error("[Feign call to notification-svc failed] Couldn't delete notification for user with id [%s]".formatted(userId));
        }
    }
}
