package app.voucher.service;

import app.voucher.model.Voucher;
import app.voucher.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VoucherService {
    private final VoucherRepository voucherRepository;

    @Autowired
    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }


    public Voucher findByCode(String code) {
        return voucherRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Voucher not found"));
    }
    public Voucher getById(UUID id) {
        return voucherRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Voucher not found"));
    }
}
