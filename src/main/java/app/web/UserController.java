package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import app.web.mapper.DTOMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/profile")
    public ModelAndView getProfile(@PathVariable UUID id) {
        User user = userService.getById(id);
        ModelAndView modelAndView = new ModelAndView("profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("editProfileRequest", DTOMapper.mapUserToUserEditRequest(user));
        return modelAndView;
    }
    @PatchMapping("/{id}/profile")
    public ModelAndView updateProfile(@PathVariable UUID id, @Valid EditProfileRequest editProfileRequest, BindingResult bindingResult) {
        User user = userService.getById(id);
        if(bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("profile");
            modelAndView.addObject("editProfileRequest", editProfileRequest);
            modelAndView.addObject("user", user);
            return modelAndView;
        }
        userService.editProfile(id,editProfileRequest);

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/all")
    public ModelAndView getAllUsers(@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());

        ModelAndView modelAndView = new ModelAndView("allUsers");
        modelAndView.addObject("user", user);

        List<User> allUsers = userService.getAllUsers();
        modelAndView.addObject("allUsers", allUsers);

        return modelAndView;
    }

    @PutMapping("/{id}/status")
    public String switchUserStatus(@PathVariable UUID id) {
        userService.switchStatus(id);
        return "redirect:/user/all";
    }

    @PutMapping("/{id}/role")
    public String switchUserRole(@PathVariable UUID id) {
        userService.switchRole(id);
        return "redirect:/user/all";
    }
}
