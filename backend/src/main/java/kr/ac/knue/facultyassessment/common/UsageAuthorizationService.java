package kr.ac.knue.facultyassessment.common;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class UsageAuthorizationService {

    private final UsageAuthorizationMapper usageAuthorizationMapper;
    private final UsagePeriodPolicy usagePeriodPolicy;

    public UsageAuthorizationService(
        UsageAuthorizationMapper usageAuthorizationMapper,
        UsagePeriodPolicy usagePeriodPolicy
    ) {
        this.usageAuthorizationMapper = usageAuthorizationMapper;
        this.usagePeriodPolicy = usagePeriodPolicy;
    }

    public boolean canAccessMenu(String menuId) {
        if (menuId == null) {
            return true;
        }
        UsageAuthorizationMapper.MenuUsage usage = usageAuthorizationMapper.findMenuUsage(menuId);
        return usage != null && usagePeriodPolicy.isMenuCurrentlyAvailable(
            usage.useYn(),
            usage.exposureStartAt(),
            usage.exposureEndAt()
        );
    }
}
