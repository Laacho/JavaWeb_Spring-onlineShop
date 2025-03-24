package app.notifications.service;

import app.exceptions.NotificationFeignClientNotWorkingException;
import app.notifications.client.NotificationClient;
import app.notifications.client.dto.CreateNotificationRequest;
import app.notifications.client.dto.NotificationResponse;
import app.notifications.client.dto.SendNotificationForAll;
import app.user.model.User;
import app.user.service.UserService;
import app.voucher.model.Voucher;
import app.web.dto.SendNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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

        try {
            ResponseEntity<NotificationResponse> notificationResponseResponseEntity = notificationClient.publishNotification(request);
            if (!notificationResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed] Couldn't post notification for user with id [%s]".formatted(userId));
            }
        } catch (Exception e) {
            throw new NotificationFeignClientNotWorkingException("Error sending notification!");
        }
    }


    public void publishNotification(SendNotificationRequest sendNotificationRequest) {
        if (!sendNotificationRequest.getUsername().isBlank()) {
            //username is not blank so send to specific user
            publishNotificationForSpecificUser(sendNotificationRequest);
        } else {
            //username is blank so send to everyone
            publishNotificationToAllUsers(sendNotificationRequest);
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
        try {
            ResponseEntity<Void> voidResponseEntity = notificationClient.publishNotificationForAll(request);
            if (!voidResponseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to send notification for all users");
            }
            return voidResponseEntity;
        } catch (Exception e) {
            throw new NotificationFeignClientNotWorkingException("Error sending notification for all users");
        }

    }

    public List<NotificationResponse> getAllUserNotifications(UUID id) {
        try {
            ResponseEntity<List<NotificationResponse>> notificationHistory = notificationClient.getNotificationHistory(id);
            if (!notificationHistory.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed] Couldn't get notification for user with id [%s]".formatted(id));
            }
            return Objects.requireNonNull(notificationHistory.getBody())
                    .stream()
                    .limit(MAX_NUMBER_OF_NOTIFICATIONS_TO_BE_DISPLAYED)
                    .toList();
        } catch (Exception e) {
            throw new NotificationFeignClientNotWorkingException("Error getting notification for user with id [%s]".formatted(id));
        }

    }

    public void deleteNotifications(UUID userId) {
        try {
            ResponseEntity<Void> voidResponseEntity = notificationClient.deleteNotification(userId);
            if (!voidResponseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed] Couldn't delete notification for user with id [%s]".formatted(userId));
            }
        } catch (Exception e) {
            log.error("Error deleting the notification for user with id [%s]".formatted(userId));
        }
    }
    @EventListener
    private void handleVoucherCreationEvent(Voucher voucher) {
        String subject="You have a new voucher!";
        String body= "%s has reached 5 orders. You can use this voucher code on your next purchase: %s!".formatted(voucher.getUser().getUsername(), voucher.getCode());
        SendNotificationRequest request = SendNotificationRequest.builder()
                .subject(subject)
                .body(body)
                .username(voucher.getUser().getUsername())
                .build();
        publishNotification(request);
    }
    private void publishNotificationForSpecificUser(SendNotificationRequest sendNotificationRequest) {
        User user = userService.getByUsername(sendNotificationRequest.getUsername());

        CreateNotificationRequest succeeded = CreateNotificationRequest.builder()
                .body(sendNotificationRequest.getBody())
                .status(sendNotificationRequest.getSubject())
                .userId(user.getId())
                .status("SUCCEEDED")
                .build();
        try {
            ResponseEntity<NotificationResponse> notificationResponseResponseEntity = notificationClient.publishNotification(succeeded);
            if (!notificationResponseResponseEntity.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed] Couldn't post notification for user with id [%s]".formatted(user.getId()));

            }
        } catch (Exception e) {
            throw new NotificationFeignClientNotWorkingException("Error sending notification for user with id [%s]".formatted(user.getId()));
        }
    }
}
