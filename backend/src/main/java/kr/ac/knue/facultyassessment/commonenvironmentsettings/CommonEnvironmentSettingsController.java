package kr.ac.knue.facultyassessment.commonenvironmentsettings;

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
public class CommonEnvironmentSettingsController {
    private final CommonEnvironmentSettingsService service;

    public CommonEnvironmentSettingsController(CommonEnvironmentSettingsService service) {
        this.service = service;
    }

    @GetMapping("/api/system/settings/common-environment")
    public ApiResponse<?> listCommonEnvironmentSettings(@RequestParam(required = false) String settingKey, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.findCommonEnvironmentSettings(new CommonEnvironmentSettingSearchCriteria(settingKey, page, size)));
    }

    @PostMapping("/api/system/settings/common-environment")
    public ApiResponse<CommonEnvironmentSetting> saveCommonEnvironmentSettings(@Valid @RequestBody CommonEnvironmentSettingRequest request, HttpServletRequest httpRequest) {
        AuthenticationPort.AuthenticatedUser user = (AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE);
        return ApiResponse.success(service.saveCommonEnvironmentSetting(request, user.userId()));
    }
}
