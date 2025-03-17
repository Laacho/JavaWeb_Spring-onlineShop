package app.web;

import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice {
    private final UserService userService;

    @Autowired
    public ExceptionAdvice(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(Exception.class)
    public String getNotifications() {
        return "error";
    }

}
