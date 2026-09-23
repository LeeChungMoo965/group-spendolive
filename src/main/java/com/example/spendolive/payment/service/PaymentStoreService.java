import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentStoreService {
    
    private final PaymentRepository paymentRepository;

    // 아예 다른 클래스이므로 여기서 @Transactional이 완벽하게 작동합니다!
    @Transactional(rollbackFor = Exception.class)
    public void savePaymentAll(SettlementPaymentVO paymentInfo, 
                               EscrowPayoutVO escrowInfo, 
                               PlatformRevenueVO revenueInfo, 
                               int roomId, 
                               String userId) throws Exception { 
                               
        paymentRepository.updatePaymentStatus(paymentInfo);
        paymentRepository.insertEscrow(escrowInfo);
        paymentRepository.insertPlatfoem_Revenue(revenueInfo);
        paymentRepository.updatSettlementroommemberStatus(roomId, userId);
    }
}
