package kr.ac.knue.facultyassessment.codegroups;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class CodeGroupManagementController {

    private final CodeGroupManagementService codeGroupManagementService;

    public CodeGroupManagementController(CodeGroupManagementService codeGroupManagementService) {
        this.codeGroupManagementService = codeGroupManagementService;
    }

    @GetMapping("/api/code-groups")
    public ApiResponse<?> listCodeGroups(
        @RequestParam(required = false) String groupId,
        @RequestParam(required = false) String useYn
    ) {
        return ApiResponse.success(codeGroupManagementService.findCodeGroups(new CodeGroupSearchCriteria(groupId, useYn)));
    }

    @PostMapping("/api/code-groups")
    public ApiResponse<Void> saveCodeGroup(@Valid @RequestBody CodeGroupRequest request, HttpServletRequest httpRequest) {
        codeGroupManagementService.saveCodeGroup(request, actor(httpRequest));
        return ApiResponse.success(null);
    }

    private String actor(HttpServletRequest httpRequest) {
        return ((AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        )).userId();
    }
}
