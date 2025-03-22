package app.voucher.service;

import app.user.model.User;
import app.user.service.UserService;
import app.voucher.model.Voucher;
import app.voucher.repository.VoucherRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VoucherService {
    private final UserService userService;
    private final VoucherRepository voucherRepository;

    @Autowired
    public VoucherService(UserService userService, VoucherRepository voucherRepository) {
        this.userService = userService;
        this.voucherRepository = voucherRepository;
    }


    public Voucher findByCode(String code) {
        return voucherRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Voucher not found"));
    }
    public Voucher getById(UUID id) {
        return voucherRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Voucher not found"));
    }


    public void checkForGivingVoucher(UUID userId) {
        long ordersForUser = userService.getOrdersForUser(userId);
        //every 5 orders to be given a voucher
        //could be changed
        if(ordersForUser % 5== 0 && ordersForUser > 0) {
            Voucher voucher = initVoucherForUser(userId);
            //add logic for notification
            //todo
        }
    }

    private Voucher initVoucherForUser(UUID userId) {
        User user = userService.getById(userId);
        String code= generateCode();
        Voucher voucher = Voucher.builder()
                .code(code)
                .minOrderPrice(BigDecimal.valueOf(50.00))
                .discountAmount(BigDecimal.valueOf(10.00))
                .deadline(LocalDateTime.now().plusDays(30))
                .user(user)
                .build();
        voucherRepository.save(voucher);
        return voucher;
    }

    private String generateCode() {
        return  RandomStringUtils.random(7,true,true);
    }
}
