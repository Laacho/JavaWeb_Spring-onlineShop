package app.web;

import app.user.service.UserService;
import app.voucher.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String getAllVouchers() {
        return "vouchers";
    }
}
