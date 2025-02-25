package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.voucher.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/vouchers")
public class VoucherController {

    public final VoucherService voucherService;
    public final UserService userService;

    @Autowired
    public VoucherController(VoucherService voucherService, UserService userService) {
        this.voucherService = voucherService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getAllVouchers(@AuthenticationPrincipal AuthenticationMetadata auth) {
        User user = userService.getById(auth.getUserId());

        ModelAndView modelAndView = new ModelAndView("vouchers");
        modelAndView.addObject("user", user);
        return modelAndView;

    }
}
