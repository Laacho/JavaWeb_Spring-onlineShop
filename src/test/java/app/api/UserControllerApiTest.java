package app.api;

import app.interceptors.UserInterceptor;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.UserController;
import app.web.dto.EditProfileRequest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @InjectMocks
    private UserInterceptor userInterceptor;

    @Test
    void getRequestToProfileEndpoint_shouldReturnProfilePage() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/user/{id}/profile", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());
        when(userService.getById(any())).thenReturn(User.builder().build());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("editProfileRequest"));
    }

    @Test
    void getRequestToProfileEndpointWithNotLoggedInUser_shouldReturnProfilePage() throws Exception {
        MockHttpServletRequestBuilder request = get("/user/{id}/profile", UUID.randomUUID());
        when(userService.getById(any())).thenReturn(User.builder().build());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    void getRequestToPatchUpdateProfile_shouldRedirectToHomePage() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        EditProfileRequest requestEditProfile = EditProfileRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .profilePicture("http://example.com/profile.jpg")
                .address("Ulica Glavna")
                .email("john.doe@example.com")
                .build();

        MockHttpServletRequestBuilder request = patch("/user/{id}/profile", UUID.randomUUID())
                .with(user(principal))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("firstName", requestEditProfile.getFirstName())
                .param("lastName", requestEditProfile.getLastName())
                .param("email", requestEditProfile.getEmail())
                .param("address", requestEditProfile.getAddress())
                .param("profilePicture", requestEditProfile.getProfilePicture());

        when(userService.getById(any())).thenReturn(User.builder().build());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
        verify(userService, times(1)).editProfile(any(), any());
    }

    @Test
    void patchRequestToPatchUpdateProfileWithInvalidRequest_shouldReturnProfilePageWithErrors() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);

        MockHttpServletRequestBuilder request = patch("/user/{id}/profile", UUID.randomUUID())
                .with(user(principal))
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "invalid-email") // Invalid email
                .param("address", "123 Street, City")
                .param("profilePicture", "http://example.com/profile.jpg");


        when(userService.getById(any())).thenReturn(User.builder().build());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrors("editProfileRequest", "email"));

        verify(userService, never()).editProfile(any(), any());
    }

    @Test
    void getRequestToAllUsersEndPoint_shouldReturnAllUsersPage() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = get("/user/all")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(User.builder().build());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("allUsers"))
                .andExpect(model().attributeExists("allUsers"));
        verify(userService, times(1)).getAllUsers();

    }

    @Test
    void getRequestToAllUsersEndPointWithNotAnAdmin_shouldReturnErrorPage() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/user/all")
                .with(user(principal))
                .with(csrf());

        when(userService.getById(any())).thenReturn(User.builder().build());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("status", "message"));
        verify(userService, never()).getAllUsers();

    }

    @Test
    void switchUserStatus_shouldRedirectToUserAll() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/user/{id}/status", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/all"));

        verify(userService, times(1)).switchStatus(any(UUID.class));
    }

    @Test
    void switchUserRole_shouldRedirectToUserAll() throws Exception {
        AuthenticationMetadata principal = new AuthenticationMetadata(UUID.randomUUID(), "User123", "123123", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/user/{id}/role", UUID.randomUUID())
                .with(user(principal))
                .with(csrf());
        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/all"));

        verify(userService, times(1)).switchRole(any(UUID.class));
    }


}
