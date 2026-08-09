package kr.ac.knue.facultyassessment.menupermissions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class MenuPermissionManagementController {

    private final MenuPermissionManagementService menuPermissionManagementService;

    public MenuPermissionManagementController(MenuPermissionManagementService menuPermissionManagementService) {
        this.menuPermissionManagementService = menuPermissionManagementService;
    }

    @GetMapping("/api/menu-permissions")
    public ApiResponse<?> listMenuPermissions(
        @RequestParam(required = false) String subjectType,
        @RequestParam(required = false) String subjectId,
        @RequestParam(required = false) String menuId
    ) {
        return ApiResponse.success(menuPermissionManagementService.findMenuPermissions(
            new MenuPermissionSearchCriteria(subjectType, subjectId, menuId)
        ));
    }

    @PutMapping("/api/menu-permissions")
    public ApiResponse<Void> saveMenuPermissions(@Valid @RequestBody MenuPermissionRequest request, HttpServletRequest httpRequest) {
        AuthenticationPort.AuthenticatedUser actor = (AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        );
        menuPermissionManagementService.saveMenuPermission(request, actor.userId());
        return ApiResponse.success(null);
    }
}
