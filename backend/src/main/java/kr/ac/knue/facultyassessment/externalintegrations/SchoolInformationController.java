package kr.ac.knue.facultyassessment.externalintegrations;

import jakarta.validation.constraints.Min;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class SchoolInformationController {

    private final SchoolInformationService schoolInformationService;

    public SchoolInformationController(SchoolInformationService schoolInformationService) {
        this.schoolInformationService = schoolInformationService;
    }

    @GetMapping("/api/external-integrations/schools")
    public ApiResponse<?> listSchoolInformation(
        @RequestParam(required = false) String schoolName,
        @RequestParam(required = false) String educationOfficeCode,
        @RequestParam(defaultValue = "1") @Min(1) int pIndex,
        @RequestParam(defaultValue = "100") @Min(1) int pSize
    ) {
        return ApiResponse.success(schoolInformationService.findSchools(schoolName, educationOfficeCode, pIndex, pSize));
    }
}
