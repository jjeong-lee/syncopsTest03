package kr.ac.knue.facultyassessment.detailcodes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class DetailCodeManagementController {

    private final DetailCodeManagementService detailCodeManagementService;

    public DetailCodeManagementController(DetailCodeManagementService detailCodeManagementService) {
        this.detailCodeManagementService = detailCodeManagementService;
    }

    @GetMapping("/api/code-groups/{groupId}/detail-codes")
    public ApiResponse<?> listDetailCodes(
        @PathVariable String groupId,
        @RequestParam(required = false) String useYn
    ) {
        return ApiResponse.success(detailCodeManagementService.findDetailCodes(new DetailCodeSearchCriteria(groupId, useYn)));
    }

    @PostMapping("/api/code-groups/{groupId}/detail-codes")
    public ApiResponse<Void> saveDetailCode(
        @PathVariable String groupId,
        @Valid @RequestBody DetailCodeRequest request,
        HttpServletRequest httpRequest
    ) {
        detailCodeManagementService.saveDetailCode(groupId, request, actor(httpRequest));
        return ApiResponse.success(null);
    }

    private String actor(HttpServletRequest httpRequest) {
        return ((AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        )).userId();
    }
}
