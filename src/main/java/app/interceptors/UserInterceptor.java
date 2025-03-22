package app.interceptors;

import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Set;

@Component
public class UserInterceptor implements HandlerInterceptor {
    private final UserService userService;
    private final Set<String> excludedUrls=Set.of("/","/login","/register");

    @Autowired
    public UserInterceptor(UserService userService) {
        this.userService = userService;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
        String endpoint = request.getRequestURI();
        if(!excludedUrls.contains(endpoint)) {
        //    User user = userService.getById(auth.getUserId());
          //  modelAndView.addObject("user", user);
        }
    }
}
