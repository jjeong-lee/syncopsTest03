package kr.ac.knue.facultyassessment.menus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.facultyassessment.auth.AuthenticationPort;
import kr.ac.knue.facultyassessment.auth.SessionAuthorizationFilter;
import kr.ac.knue.facultyassessment.common.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.foundation.enabled", havingValue = "true", matchIfMissing = true)
public class MenuManagementController {

    private final MenuManagementService menuManagementService;

    public MenuManagementController(MenuManagementService menuManagementService) {
        this.menuManagementService = menuManagementService;
    }

    @GetMapping("/api/menus")
    public ApiResponse<?> listMenus(
        @RequestParam(required = false) String parentMenuId,
        @RequestParam(required = false) String useYn
    ) {
        return ApiResponse.success(menuManagementService.findMenus(new MenuSearchCriteria(parentMenuId, useYn)));
    }

    @PostMapping("/api/menus")
    public ApiResponse<Void> saveMenu(@Valid @RequestBody MenuRequest request, HttpServletRequest httpRequest) {
        menuManagementService.saveMenuStructure(request, actor(httpRequest));
        return ApiResponse.success(null);
    }

    @PutMapping("/api/menus/order")
    public ApiResponse<Void> reorderMenu(@Valid @RequestBody MenuOrderRequest request, HttpServletRequest httpRequest) {
        menuManagementService.reorderMenu(request, actor(httpRequest));
        return ApiResponse.success(null);
    }

    private String actor(HttpServletRequest httpRequest) {
        return ((AuthenticationPort.AuthenticatedUser) httpRequest.getAttribute(
            SessionAuthorizationFilter.AUTHENTICATED_USER_ATTRIBUTE
        )).userId();
    }
}
