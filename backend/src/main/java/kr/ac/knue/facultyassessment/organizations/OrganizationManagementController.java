package kr.ac.knue.facultyassessment.organizations;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class OrganizationManagementController {

    private final OrganizationManagementService organizationManagementService;

    public OrganizationManagementController(OrganizationManagementService organizationManagementService) {
        this.organizationManagementService = organizationManagementService;
    }

    @GetMapping("/api/organizations")
    public ApiResponse<?> listOrganizations(@RequestParam(required = false) String organizationCode) {
        return ApiResponse.success(organizationManagementService.findOrganizations(new OrganizationSearchCriteria(organizationCode)));
    }

    @PutMapping("/api/organizations/{organizationId}/relationship")
    public ApiResponse<Void> saveOrganizationRelationship(
        @PathVariable String organizationId,
        @Valid @RequestBody OrganizationRelationshipRequest request,
        HttpServletRequest httpRequest
    ) {
        AuthenticationPort.AuthenticatedUser actor = (AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        );
        organizationManagementService.saveRelationship(organizationId, request, actor.userId());
        return ApiResponse.success(null);
    }
}
