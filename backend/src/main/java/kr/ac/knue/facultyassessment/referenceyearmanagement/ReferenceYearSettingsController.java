package kr.ac.knue.facultyassessment.referenceyearmanagement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class ReferenceYearSettingsController {

    private final ReferenceYearSettingsService referenceYearSettingsService;

    public ReferenceYearSettingsController(ReferenceYearSettingsService referenceYearSettingsService) {
        this.referenceYearSettingsService = referenceYearSettingsService;
    }

    @GetMapping("/api/system/settings/reference-years")
    public ApiResponse<?> listReferenceYearSettings(
        @RequestParam(required = false) Integer targetYear,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(referenceYearSettingsService.findReferenceYearSettings(
            new ReferenceYearSettingSearchCriteria(targetYear, page, size)
        ));
    }

    @PostMapping("/api/system/settings/reference-years")
    public ApiResponse<ReferenceYearSetting> saveReferenceYearSettings(
        @Valid @RequestBody ReferenceYearSettingRequest request,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(referenceYearSettingsService.saveReferenceYearSetting(request, actor(httpRequest)));
    }

    private String actor(HttpServletRequest httpRequest) {
        return ((AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        )).userId();
    }
}
