package kr.ac.knue.facultyassessment.common;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class UsagePeriodPolicy {

    private final Clock clock;

    public UsagePeriodPolicy() {
        this(Clock.systemUTC());
    }

    UsagePeriodPolicy(Clock clock) {
        this.clock = clock;
    }

    public void validateMenuExposurePeriod(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_EXPOSURE_START", "노출 시작일시는 필수입니다.", "exposureStartAt");
        }
        if (endAt != null && startAt.isAfter(endAt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EXPOSURE_PERIOD", "노출 시작일시는 종료일시보다 늦을 수 없습니다.", "exposureEndAt");
        }
    }

    public void validateDetailCodeEffectivePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_EFFECTIVE_START", "적용 시작일은 필수입니다.", "effectiveStartDate");
        }
        if (endDate != null && startDate.isAfter(endDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EFFECTIVE_PERIOD", "적용 시작일은 종료일보다 늦을 수 없습니다.", "effectiveEndDate");
        }
    }

    public boolean isMenuCurrentlyAvailable(String useYn, OffsetDateTime startAt, OffsetDateTime endAt) {
        return isMenuAvailableAt(useYn, startAt, endAt, OffsetDateTime.now(clock));
    }

    public boolean isDetailCodeCurrentlyAvailable(String useYn, LocalDate startDate, LocalDate endDate) {
        return isDetailCodeAvailableOn(useYn, startDate, endDate, LocalDate.now(clock));
    }

    public boolean isHistoricalMenuRecord(String useYn, OffsetDateTime startAt, OffsetDateTime endAt) {
        return isPeriodRecord(startAt);
    }

    public boolean isHistoricalDetailCodeRecord(String useYn, LocalDate startDate, LocalDate endDate) {
        return isPeriodRecord(startDate);
    }

    private boolean isMenuAvailableAt(String useYn, OffsetDateTime startAt, OffsetDateTime endAt, OffsetDateTime referenceTime) {
        return "Y".equals(useYn)
            && isPeriodRecord(startAt)
            && !referenceTime.isBefore(startAt)
            && (endAt == null || !referenceTime.isAfter(endAt));
    }

    private boolean isDetailCodeAvailableOn(String useYn, LocalDate startDate, LocalDate endDate, LocalDate referenceDate) {
        return "Y".equals(useYn)
            && isPeriodRecord(startDate)
            && !referenceDate.isBefore(startDate)
            && (endDate == null || !referenceDate.isAfter(endDate));
    }

    private boolean isPeriodRecord(Object start) {
        return start != null;
    }
}
