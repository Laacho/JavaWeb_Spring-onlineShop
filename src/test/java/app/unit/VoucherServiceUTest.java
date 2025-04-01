package app.unit;

import app.user.service.UserService;
import app.voucher.model.Voucher;
import app.voucher.repository.VoucherRepository;
import app.voucher.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceUTest {

    @Mock
    private UserService userService;
    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private VoucherService voucherService;

    @Test
    void testGetById_Success() {
        UUID voucherId = UUID.randomUUID();
        Voucher expectedVoucher = new Voucher();
        expectedVoucher.setId(voucherId);

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(expectedVoucher));

        Voucher actualVoucher = voucherService.getById(voucherId);

        assertNotNull(actualVoucher);
        assertEquals(voucherId, actualVoucher.getId());
        verify(voucherRepository, times(1)).findById(voucherId);
    }

    @Test
    void testGetById_NotFound() {
        UUID voucherId = UUID.randomUUID();

        when(voucherRepository.findById(voucherId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> voucherService.getById(voucherId));

        assertEquals("Voucher not found", exception.getMessage());
        verify(voucherRepository, times(1)).findById(voucherId);
    }

    @Test
    void testCheckForGivingVoucher_ShouldGiveVoucher() {
        UUID userId = UUID.randomUUID();
        long orders = 10;

        when(userService.getOrdersForUser(userId)).thenReturn(orders);

        voucherService.checkForGivingVoucher(userId);

        verify(eventPublisher, times(1)).publishEvent(any(Voucher.class));
    }

    @Test
    void testCheckForGivingVoucher_ShouldNotGiveVoucher() {
        UUID userId = UUID.randomUUID();
        long orders = 7;

        when(userService.getOrdersForUser(userId)).thenReturn(orders);

        voucherService.checkForGivingVoucher(userId);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testCheckForGivingVoucher_ShouldNotGiveVoucher_ZeroOrders() {
        UUID userId = UUID.randomUUID();
        long orders = 0;

        when(userService.getOrdersForUser(userId)).thenReturn(orders);

        voucherService.checkForGivingVoucher(userId);

        verify(eventPublisher, never()).publishEvent(any());
    }
}
