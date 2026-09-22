package kr.ac.knue.facultyassessment.menuusage;

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
public class MenuUsageController {

    private final MenuUsageService menuUsageService;

    public MenuUsageController(MenuUsageService menuUsageService) {
        this.menuUsageService = menuUsageService;
    }

    @GetMapping("/api/system/menus/usage")
    public ApiResponse<?> listMenuUsageSettings(
        @RequestParam(required = false) String menuId,
        @RequestParam(required = false) String useYn,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(menuUsageService.findMenuUsageSettings(
            new MenuUsageSearchCriteria(menuId, useYn, page, size)
        ));
    }

    @PostMapping("/api/system/menus/usage")
    public ApiResponse<MenuUsageSetting> saveMenuUsageSettings(
        @Valid @RequestBody MenuUsageSettingRequest request,
        HttpServletRequest httpRequest
    ) {
        return ApiResponse.success(menuUsageService.saveMenuUsageSetting(request, actor(httpRequest)));
    }

    private String actor(HttpServletRequest httpRequest) {
        return ((AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        )).userId();
    }
}
