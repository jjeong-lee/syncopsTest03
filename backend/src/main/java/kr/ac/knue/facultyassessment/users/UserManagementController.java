package kr.ac.knue.facultyassessment.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/api/users")
    public ApiResponse<?> listUsers(
        @RequestParam(required = false) String personnelNo,
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String organization,
        @RequestParam(required = false) String position,
        @RequestParam(required = false) String employmentStatus,
        @RequestParam(required = false) String roleCode,
        @RequestParam(required = false) String useYn
    ) {
        return ApiResponse.success(userManagementService.findUsers(
            new UserSearchCriteria(personnelNo, name, organization, position, employmentStatus, roleCode, useYn)
        ));
    }

    @PatchMapping("/api/users/{userId}/settings")
    public ApiResponse<Void> updateUserSettings(
        @PathVariable String userId,
        @Valid @RequestBody UserSettingsRequest request,
        HttpServletRequest httpRequest
    ) {
        AuthenticationPort.AuthenticatedUser actor = (AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        );
        userManagementService.updateSettings(userId, request, actor.userId());
        return ApiResponse.success(null);
    }
}
