package kr.ac.knue.facultyassessment.settings;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class CommonSettingsController {

    private final CommonSettingsService commonSettingsService;

    public CommonSettingsController(CommonSettingsService commonSettingsService) {
        this.commonSettingsService = commonSettingsService;
    }

    @GetMapping("/api/settings/common")
    public ApiResponse<CommonSettings> listCommonSettings() {
        return ApiResponse.success(commonSettingsService.findCommonSettings());
    }

    @PostMapping("/api/settings/common")
    public ApiResponse<Void> saveCommonSettings(
        @Valid @RequestBody CommonSettingsRequest request,
        HttpServletRequest httpRequest
    ) {
        commonSettingsService.saveCommonSettings(request, actor(httpRequest));
        return ApiResponse.success(null);
    }

    @GetMapping("/api/settings/reference-years")
    public ApiResponse<ReferenceYearSettings> listReferenceYearSettings() {
        return ApiResponse.success(commonSettingsService.findReferenceYearSettings());
    }

    @PostMapping("/api/settings/reference-years")
    public ApiResponse<Void> saveReferenceYearSettings(
        @Valid @RequestBody ReferenceYearSettingsRequest request,
        HttpServletRequest httpRequest
    ) {
        commonSettingsService.saveReferenceYearSettings(request, actor(httpRequest));
        return ApiResponse.success(null);
    }

    private String actor(HttpServletRequest request) {
        return ((AuthenticationPort.AuthenticatedUser) request.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        )).userId();
    }
}
