package app.web;

import app.notifications.client.dto.NotificationResponse;
import app.notifications.service.NotificationService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.SendNotificationRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ModelAndView getNotifications(@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("notification");
        modelAndView.addObject("user", user);
        if(user.getRole().name().equals("ADMIN")) {
        //load post notification dto
            //needs to be a dto with subject body and a username
            modelAndView.addObject("sendNotificationRequest",new SendNotificationRequest());
        }
        else{
            //he is user so
            //get zaqvka kum history limit 10 da kajem
            //i gi mapim na front end
            List<NotificationResponse> allUserNotifications = notificationService.getAllUserNotifications(user.getId());
            modelAndView.addObject("allUserNotifications", allUserNotifications);
        }
        return modelAndView;
    }

    @PostMapping("/publish")
    public ModelAndView publishNotification(@Valid SendNotificationRequest sendNotificationRequest, BindingResult bindingResult,@AuthenticationPrincipal AuthenticationMetadata auth) {
        ModelAndView modelAndView = new ModelAndView();
        User user = userService.getById(auth.getUserId());
        modelAndView.addObject("user", user);
        if(bindingResult.hasErrors()) {
            modelAndView.setViewName("notification");
            return modelAndView;
        }
        notificationService.publishNotification(sendNotificationRequest);
        modelAndView.setViewName("redirect:/home");
        return modelAndView;
    }
    @PutMapping
    public String changeUserNotifications(@AuthenticationPrincipal AuthenticationMetadata auth) {
        userService.changeNotifications(auth.getUserId());
        return "redirect:/notifications";
    }

    @DeleteMapping
    public String delete(@AuthenticationPrincipal AuthenticationMetadata auth) {
        notificationService.deleteNotifications(auth.getUserId());
        return "redirect:/notifications";
    }
}
