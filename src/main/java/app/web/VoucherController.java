package app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/vouchers")
public class VoucherController {


    @GetMapping
    public String getAllVouchers() {
        return "vouchers";
    }
}
