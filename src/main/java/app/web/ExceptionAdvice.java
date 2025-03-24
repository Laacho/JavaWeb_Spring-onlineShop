package app.web;

import app.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class ExceptionAdvice {


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            AccessDeniedException.class, // Когато се опитва да достъпи ендпойнт, до който не му е позволено/нямам достъп
            NoResourceFoundException.class, // Когато се опитва да достъпи невалиден ендпойнт
            MethodArgumentTypeMismatchException.class,
            MissingRequestValueException.class
    })
    public ModelAndView handleException() {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status",404);
        modelAndView.addObject("message", "Not Found");
        return modelAndView;
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NotificationFeignClientNotWorkingException.class)
    public ModelAndView handleNotificationFeignClientNotWorkingException(NotificationFeignClientNotWorkingException e) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("message", e.getMessage());
        modelAndView.addObject("message",404);
        return modelAndView;
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProductNameAlreadyExistsException.class)
    public String handleProductNameAlreadyExists(RedirectAttributes redirectAttributes, ProductNameAlreadyExistsException productNameAlreadyExistsException) {
        String message = productNameAlreadyExistsException.getMessage();
        redirectAttributes.addFlashAttribute("productAlreadyExists", message);
        return "redirect:/products/add";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFoundException() {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status",404);
        modelAndView.addObject("message", "Could not find what you were looking for!");
        return modelAndView;
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidUsernameOrAddressException.class)
    public String handleDuplicateEntryForUsernameOrAddress(RedirectAttributes redirectAttributes, InvalidUsernameOrAddressException invalidUsernameOrAddressException) {
        String message = invalidUsernameOrAddressException.getMessage();
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:/register";
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(OrderPriceMismatchException.class)
    public ModelAndView handlePriceMismatchException(OrderPriceMismatchException orderPriceMismatchException) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status",400);
        modelAndView.addObject("message", orderPriceMismatchException.getMessage());
        return modelAndView;
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(VoucherException.class)
    public ModelAndView handleVoucherException(VoucherException voucherException) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status",400);
        modelAndView.addObject("message", voucherException.getMessage());
        return modelAndView;
    }
    @ExceptionHandler(Exception.class)
    public ModelAndView defaultErrorHandler(){
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("status",400);
        modelAndView.addObject("message", "Something went wrong");
        return modelAndView;
    }

}
