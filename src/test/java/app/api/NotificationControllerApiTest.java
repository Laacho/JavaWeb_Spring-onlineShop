package app.api;

import app.notifications.client.dto.NotificationResponse;
import app.notifications.service.NotificationService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.NotificationController;
import app.web.dto.SendNotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private NotificationService notificationService;

    @Test
    void testGetNotifications_AsAdmin_ShouldReturnViewWithDTO() throws Exception {
        UUID userId = UUID.randomUUID();
        User adminUser = User.builder()
                .id(userId)
                .role(UserRole.ADMIN)
                .build();
        when(userService.getById(userId)).thenReturn(adminUser);
        AuthenticationMetadata principal = new AuthenticationMetadata(adminUser.getId(), "User123", "123123", UserRole.ADMIN, true);

        MockHttpServletRequestBuilder request = get("/notifications")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("notification"))
                .andExpect(model().attributeExists("sendNotificationRequest"));

        verify(userService, times(2)).getById(userId);
    }

    @Test
    void testGetNotifications_AsUser_ShouldReturnViewWithNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.USER)
                .build();

        List<NotificationResponse> notifications = List.of(
                NotificationResponse.builder().subject("Subject 1").body("Body 1").build(),
                NotificationResponse.builder().subject("Subject 2").body("Body 2").build()
        );

        when(userService.getById(userId)).thenReturn(user);
        when(notificationService.getAllUserNotifications(userId)).thenReturn(notifications);
        AuthenticationMetadata principal = new AuthenticationMetadata(user.getId(), "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = get("/notifications")
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("notification"))
                .andExpect(model().attributeExists("allUserNotifications"));

        verify(userService, times(2)).getById(userId);
        verify(notificationService, times(1)).getAllUserNotifications(userId);
    }

    @Test
    void testPublishNotification_ValidRequest_ShouldRedirectToHome() throws Exception {
        SendNotificationRequest sendNotificationRequest = SendNotificationRequest.builder()
                .username("user123")
                .subject("Test Subject")
                .body("Test Body")
                .build();
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = post("/notifications/publish")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", sendNotificationRequest.getUsername())
                .param("subject", sendNotificationRequest.getSubject())
                .param("body", sendNotificationRequest.getBody())
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));

        verify(notificationService, times(1)).publishNotification(any(SendNotificationRequest.class));
    }

    @Test
    void testPublishNotification_InvalidRequest_ShouldReturnNotificationView() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = post("/notifications/publish")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "")
                .param("subject", "")
                .param("body", "")
                .with(user(principal))
                .with(csrf());
        when(userService.getById(any())).thenReturn(User.builder().role(UserRole.USER).build());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("notification"));

        verify(notificationService, never()).publishNotification(any());
    }

    @Test
    void testChangeUserNotifications_ShouldRedirectToNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = put("/notifications")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(userService, times(1)).changeNotifications(userId);
    }

    @Test
    void testDeleteNotifications_ShouldRedirectToNotifications() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticationMetadata principal = new AuthenticationMetadata(userId, "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = delete("/notifications")
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService, times(1)).deleteNotifications(userId);
    }
}
