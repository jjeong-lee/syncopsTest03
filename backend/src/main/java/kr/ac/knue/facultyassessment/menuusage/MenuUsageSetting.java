package kr.ac.knue.facultyassessment.menuusage;

import java.time.OffsetDateTime;

public record MenuUsageSetting(
    String menuId,
    String useYn,
    OffsetDateTime exposureStartAt,
    OffsetDateTime exposureEndAt
) {
}
