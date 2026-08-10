package kr.ac.knue.facultyassessment.roles;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @GetMapping("/api/roles")
    public ApiResponse<?> listRoles() {
        return ApiResponse.success(roleManagementService.findRoles());
    }

    @PostMapping("/api/roles")
    public ApiResponse<Void> saveRole(@Valid @RequestBody RoleRequest request, HttpServletRequest httpRequest) {
        AuthenticationPort.AuthenticatedUser actor = (AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        );
        roleManagementService.saveRole(request, actor.userId());
        return ApiResponse.success(null);
    }
}
