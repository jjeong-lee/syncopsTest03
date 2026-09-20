package kr.ac.knue.facultyassessment.assignments;

import java.time.LocalDate;
import kr.ac.knue.facultyassessment.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class EffectiveDateValidator {

    public void validate(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && startDate.isAfter(endDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EFFECTIVE_PERIOD", "유효 시작일은 종료일보다 늦을 수 없습니다.", "effectiveEndDate");
        }
    }
}
