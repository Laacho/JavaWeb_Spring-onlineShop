package app.unit;

import app.notifications.client.NotificationClient;
import app.notifications.client.dto.CreateNotificationRequest;
import app.notifications.client.dto.NotificationResponse;
import app.notifications.client.dto.SendNotificationForAll;
import app.notifications.service.NotificationService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.SendNotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUTest {

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testSendNotificationWithTrackingNumber_ShouldSendNotification() {
        UUID userId = UUID.randomUUID();
        String trackingNumber = "123ABC";
        String body = "Your order has been sent to " + trackingNumber + ". You can use this number to track it.";
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .subject("You have successfully placed an order!")
                .body(body)
                .userId(userId)
                .status("SUCCEEDED")
                .build();

        NotificationResponse notificationResponse = NotificationResponse.builder().build();
        when(notificationClient.publishNotification(request)).thenReturn(ResponseEntity.ok(notificationResponse));

        notificationService.sendNotificationWithTrackingNumber(trackingNumber, userId);

        verify(notificationClient, times(1)).publishNotification(request);
    }


    @Test
    void testPublishNotificationToAllUsers_ShouldSendNotification() {
        SendNotificationRequest sendNotificationRequest = SendNotificationRequest.builder()
                .subject("Broadcast Subject")
                .body("Broadcast Body")
                .build();

        List<User> users = List.of(User.builder().id(UUID.randomUUID()).build(), User.builder().id(UUID.randomUUID()).build());
        when(userService.getAllUsers()).thenReturn(users);

        SendNotificationForAll sendNotificationForAll = SendNotificationForAll.builder()
                .body("Broadcast Body")
                .subject("Broadcast Subject")
                .status("SUCCEEDED")
                .userId(List.of(users.get(0).getId(), users.get(1).getId()))
                .build();

        ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
        when(notificationClient.publishNotificationForAll(sendNotificationForAll)).thenReturn(responseEntity);

        notificationService.publishNotificationToAllUsers(sendNotificationRequest);

        verify(notificationClient, times(1)).publishNotificationForAll(sendNotificationForAll);
    }

    @Test
    void testGetAllUserNotifications_ShouldReturnNotifications() {
        UUID userId = UUID.randomUUID();
        NotificationResponse notificationResponse1 = NotificationResponse.builder().build();
        NotificationResponse notificationResponse2 = NotificationResponse.builder().build();
        when(notificationClient.getNotificationHistory(userId))
                .thenReturn(ResponseEntity.ok(List.of(notificationResponse1, notificationResponse2)));

        List<NotificationResponse> notifications = notificationService.getAllUserNotifications(userId);

        verify(notificationClient, times(1)).getNotificationHistory(userId);
        assert notifications.size() == 2;
    }

    @Test
    void testDeleteNotifications_ShouldCallDelete() {
        UUID userId = UUID.randomUUID();
        ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
        when(notificationClient.deleteNotification(userId)).thenReturn(responseEntity);

        notificationService.deleteNotifications(userId);

        verify(notificationClient, times(1)).deleteNotification(userId);
    }


}
