package kr.ac.knue.facultyassessment.userroles;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class UserRoleManagementController {

    private final UserRoleManagementService userRoleManagementService;

    public UserRoleManagementController(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    @GetMapping("/api/users/{userId}/roles")
    public ApiResponse<?> listUserRoles(@PathVariable String userId) {
        return ApiResponse.success(userRoleManagementService.findActiveUserRoles(userId));
    }

    @PostMapping("/api/users/{userId}/roles")
    public ApiResponse<Void> saveUserRole(
        @PathVariable String userId,
        @Valid @RequestBody UserRoleRequest request,
        HttpServletRequest httpRequest
    ) {
        AuthenticationPort.AuthenticatedUser actor = (AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        );
        userRoleManagementService.saveUserRole(userId, request, actor.userId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/api/users/{userId}/roles/{userRoleId}")
    public ApiResponse<Void> revokeUserRole(
        @PathVariable String userId,
        @PathVariable String userRoleId,
        @Valid @RequestBody UserRoleRevokeRequest request,
        HttpServletRequest httpRequest
    ) {
        AuthenticationPort.AuthenticatedUser actor = (AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        );
        userRoleManagementService.revokeUserRole(userId, userRoleId, request, actor.userId());
        return ApiResponse.success(null);
    }
}
