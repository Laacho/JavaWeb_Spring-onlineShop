package app.web;

import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.EditProfileRequest;
import app.web.mapper.DTOMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
        if(bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("profile");
            modelAndView.addObject("editProfileRequest", editProfileRequest);
            return modelAndView;
        }
        userService.editProfile(id,editProfileRequest);

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsers() {

        ModelAndView modelAndView = new ModelAndView("allUsers");
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
