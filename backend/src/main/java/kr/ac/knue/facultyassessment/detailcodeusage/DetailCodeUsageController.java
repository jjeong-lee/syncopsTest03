package kr.ac.knue.facultyassessment.detailcodeusage;

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
public class DetailCodeUsageController {

    private final DetailCodeUsageService detailCodeUsageService;

    public DetailCodeUsageController(DetailCodeUsageService detailCodeUsageService) {
        this.detailCodeUsageService = detailCodeUsageService;
    }

    @GetMapping("/api/system/common-codes/usage")
    public ApiResponse<?> listDetailCodeUsageSettings(
        @RequestParam(required = false) String groupId,
        @RequestParam(required = false) String codeValue,
        @RequestParam(required = false) String useYn,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(detailCodeUsageService.findDetailCodeUsageSettings(
            new DetailCodeUsageSearchCriteria(groupId, codeValue, useYn, page, size)
        ));
    }

    @PostMapping("/api/system/common-codes/usage")
    public ApiResponse<DetailCodeUsageSetting> saveDetailCodeUsageSettings(
        @Valid @RequestBody DetailCodeUsageSettingRequest request,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(detailCodeUsageService.saveDetailCodeUsageSetting(
            request,
            actor(httpRequest)
        ));
    }

    private String actor(HttpServletRequest httpRequest) {
        return ((AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        )).userId();
    }
}
