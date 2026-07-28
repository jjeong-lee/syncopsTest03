package kr.ac.knue.fpe.contract;

import jakarta.servlet.http.Cookie;
import kr.ac.knue.fpe.admin.AdminController;
import kr.ac.knue.fpe.admin.AdminMapper;
import kr.ac.knue.fpe.admin.AdminService;
import kr.ac.knue.fpe.auth.AuthController;
import kr.ac.knue.fpe.auth.AuthService;
import kr.ac.knue.fpe.auth.SessionUser;
import kr.ac.knue.fpe.common.HealthController;
import kr.ac.knue.fpe.common.api.ApiException;
import kr.ac.knue.fpe.common.api.GlobalExceptionHandler;
import kr.ac.knue.fpe.common.api.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpenApiOperationMockMvcContractTest {
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Mock AdminMapper adminMapper;
    @Mock AdminService adminService;
    @Mock AuthService authService;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(
                new HealthController(),
                new AuthController(authService),
                new AdminController(adminMapper, adminService)
            )
            .setControllerAdvice(new GlobalExceptionHandler())
            .defaultRequest(get("/").requestAttr("request_id", "REQ-CONTRACT"))
            .build();

        when(adminService.params(anyMap())).thenAnswer(invocation -> {
            Map<String, String> raw = invocation.getArgument(0);
            Map<String, Object> params = new HashMap<>(raw);
            params.putIfAbsent("page", 0);
            params.putIfAbsent("size", 20);
            params.putIfAbsent("offset", 0);
            return params;
        });
        when(adminService.page(org.mockito.ArgumentMatchers.<List<Map<String, Object>>>any(), anyMap()))
            .thenAnswer(invocation -> {
                List<Map<String, Object>> items = invocation.getArgument(0);
                return PageResult.of(items, 0, 20);
            });

        when(adminMapper.listUsers(anyMap())).thenReturn(List.of(row("userId", "USER-1", "displayName", "관리자", "systemUseYn", "Y")));
        when(adminMapper.listOrganizations(anyMap())).thenReturn(List.of(row("organizationCode", "KNUE", "organizationName", "한국교원대학교")));
        when(adminMapper.organizationTreeRows()).thenReturn(new ArrayList<>(List.of(row("organizationCode", "KNUE", "parentOrganizationCode", null, "organizationName", "한국교원대학교"))));
        when(adminMapper.listRoles(anyMap())).thenReturn(List.of(row("roleCode", "R09", "roleName", "시스템관리자", "useYn", "Y")));
        when(adminMapper.listUserRoles(anyMap())).thenReturn(List.of(row("userRoleId", "UR-1", "userId", "USER-1", "roleCode", "R09", "assignmentStatus", "ACTIVE")));
        when(adminMapper.listMenus(anyMap())).thenReturn(List.of(row("menuId", "MENU-1", "menuName", "사용자 관리", "useYn", "Y")));
        when(adminMapper.menuTreeRows()).thenReturn(new ArrayList<>(List.of(row("menuId", "MENU-1", "parentMenuId", null, "menuName", "사용자 관리"))));
        when(adminMapper.listMenuPermissions(anyMap())).thenReturn(List.of(row("menuPermissionId", "MP-1", "targetType", "ROLE", "targetId", "R09", "accessAllowedYn", "Y")));
        when(adminMapper.listCodeGroups(anyMap())).thenReturn(List.of(row("groupId", "ROLE_SOURCE", "groupName", "역할 출처", "useYn", "Y")));
        when(adminMapper.listDetailCodes(anyMap())).thenReturn(List.of(row("detailCodeId", "DC-1", "groupId", "ROLE_SOURCE", "codeValue", "MANUAL", "useYn", "Y")));

        when(authService.login(eq("admin"), eq("admin"), anyString()))
            .thenReturn(new AuthService.LoginResult("SESSION-1", new SessionUser(UUID.fromString("00000000-0000-0000-0000-000000000009"), "admin", "관리자", List.of("R09"))));
        when(adminService.updateUserUsage(eq("USER-1"), anyMap(), anyString())).thenReturn(row("userId", "USER-1", "systemUseYn", "N", "accountStatus", "DISABLED"));
        when(adminService.updateOrganizationRelation(eq("KNUE"), anyMap(), anyString())).thenReturn(row("organizationCode", "KNUE", "parentOrganizationCode", "ROOT"));
        when(adminService.createRole(anyMap(), anyString())).thenReturn(row("roleCode", "R10", "roleName", "성과관리자", "useYn", "Y"));
        when(adminService.updateRole(eq("R09"), anyMap(), anyString())).thenReturn(row("roleCode", "R09", "roleName", "시스템관리자", "useYn", "Y"));
        when(adminService.assignUserRole(anyMap(), anyString())).thenReturn(row("userRoleId", "UR-2", "roleCode", "R09", "assignmentStatus", "ACTIVE"));
        when(adminService.revokeUserRole(eq("UR-1"), anyMap(), anyString())).thenReturn(row("userRoleId", "UR-1", "assignmentStatus", "REVOKED"));
        when(adminService.createMenu(anyMap(), anyString())).thenReturn(row("menuId", "MENU-2", "menuName", "역할 관리", "useYn", "Y"));
        when(adminService.updateMenu(eq("MENU-1"), anyMap(), anyString())).thenReturn(row("menuId", "MENU-1", "menuName", "사용자 관리", "useYn", "N"));
        when(adminService.createCodeGroup(anyMap(), anyString())).thenReturn(row("groupId", "NEW_GROUP", "groupName", "신규 그룹", "useYn", "Y"));
        when(adminService.updateCodeGroup(eq("ROLE_SOURCE"), anyMap(), anyString())).thenReturn(row("groupId", "ROLE_SOURCE", "groupName", "역할 출처", "useYn", "N"));
        when(adminService.createDetailCode(anyMap(), anyString())).thenReturn(row("detailCodeId", "DC-2", "groupId", "ROLE_SOURCE", "codeValue", "POSITION_BASED", "useYn", "Y"));
        when(adminService.updateDetailCode(eq("DC-1"), anyMap(), anyString())).thenReturn(row("detailCodeId", "DC-1", "groupId", "ROLE_SOURCE", "codeValue", "MANUAL", "useYn", "N"));
    }

    @Test
    void openapi_fixture_is_loaded_from_classpath_contract_resource() throws Exception {
        String yaml = new String(new ClassPathResource("contracts/openapi.yaml").getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(yaml).contains("operationId: getHealth", "operationId: saveMenuPermissions", "HttpOnly SameSite=Lax");
    }

    @Test
    void public_health_and_auth_operations_return_enveloped_contracts() throws Exception {
        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"));

        mvc.perform(post("/api/auth/login").contentType(JSON).content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().value("JSESSIONID", "SESSION-1"))
            .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
            .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
            .andExpect(jsonPath("$.data.loginId").value("admin"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"));

        mvc.perform(post("/api/auth/logout").cookie(new Cookie("JSESSIONID", "SESSION-1")))
            .andExpect(status().isOk())
            .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
            .andExpect(jsonPath("$.success").value(true));

        mvc.perform(get("/api/auth/me").requestAttr("sessionUser", new SessionUser(UUID.fromString("00000000-0000-0000-0000-000000000009"), "admin", "관리자", List.of("R09"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.loginId").value("admin"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"));
    }

    @Test
    void login_validation_negative_case_returns_bad_request_body_contract() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(JSON).content("{\"loginId\":\"\",\"password\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void read_operations_support_portal_filtering_and_page_contracts() throws Exception {
        mvc.perform(get("/api/users").param("roleCode", "R09").param("systemUseYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].userId").value("USER-1"))
            .andExpect(jsonPath("$.data.page.size").value(20));

        mvc.perform(get("/api/organizations").param("organizationCode", "KNUE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].organizationCode").value("KNUE"));

        mvc.perform(get("/api/organizations/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].organizationCode").value("KNUE"));

        mvc.perform(get("/api/roles").param("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].roleCode").value("R09"));

        mvc.perform(get("/api/user-roles").param("roleCode", "R09"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].assignmentStatus").value("ACTIVE"));

        mvc.perform(get("/api/menus").param("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].menuId").value("MENU-1"));

        mvc.perform(get("/api/menus/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").value("MENU-1"));

        mvc.perform(get("/api/menu-permissions").param("targetType", "ROLE").param("targetId", "R09"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].targetId").value("R09"));

        mvc.perform(get("/api/code-groups").param("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].groupId").value("ROLE_SOURCE"));

        mvc.perform(get("/api/detail-codes").param("groupId", "ROLE_SOURCE").param("useYn", "Y"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].codeValue").value("MANUAL"));
    }

    @Test
    void write_operations_return_body_contracts_and_delegate_side_effects() throws Exception {
        mvc.perform(patch("/api/users/USER-1/usage").contentType(JSON).content("{\"systemUseYn\":\"N\",\"accountStatus\":\"DISABLED\",\"reason\":\"계정 중지\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value("USER-1"))
            .andExpect(jsonPath("$.data.systemUseYn").value("N"));

        mvc.perform(put("/api/organizations/KNUE/relation").contentType(JSON).content("{\"parentOrganizationCode\":\"ROOT\",\"effectiveStartDate\":\"2026-01-01\",\"effectiveEndDate\":\"2026-12-31\",\"reason\":\"관계 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.organizationCode").value("KNUE"));

        mvc.perform(post("/api/roles").contentType(JSON).content("{\"roleCode\":\"R10\",\"roleName\":\"성과관리자\",\"rolePurpose\":\"평가 관리\",\"defaultDataScope\":\"ALL\",\"useYn\":\"Y\",\"reason\":\"등록\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.roleCode").value("R10"));

        mvc.perform(patch("/api/roles/R09").contentType(JSON).content("{\"roleName\":\"시스템관리자\",\"rolePurpose\":\"시스템 관리\",\"defaultDataScope\":\"ALL\",\"useYn\":\"Y\",\"reason\":\"수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCode").value("R09"));

        mvc.perform(post("/api/user-roles").contentType(JSON).content("{\"userId\":\"USER-1\",\"roleCode\":\"R09\",\"roleSource\":\"MANUAL\",\"validFrom\":\"2026-01-01\",\"reason\":\"부여\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.assignmentStatus").value("ACTIVE"));

        mvc.perform(patch("/api/user-roles/UR-1/revoke").contentType(JSON).content("{\"reason\":\"회수\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.assignmentStatus").value("REVOKED"));

        mvc.perform(post("/api/menus").contentType(JSON).content("{\"menuLevel\":\"L2\",\"displayOrder\":2,\"menuName\":\"역할 관리\",\"useYn\":\"Y\",\"reason\":\"등록\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.menuId").value("MENU-2"));

        mvc.perform(patch("/api/menus/MENU-1").contentType(JSON).content("{\"menuLevel\":\"L2\",\"displayOrder\":1,\"menuName\":\"사용자 관리\",\"useYn\":\"N\",\"reason\":\"중지\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.useYn").value("N"));

        mvc.perform(patch("/api/menus/reorder").contentType(JSON).content("{\"orders\":[{\"menuId\":\"MENU-1\",\"displayOrder\":1}],\"reason\":\"정렬\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        verify(adminService).reorderMenus(org.mockito.ArgumentMatchers.<Map<String, Object>>any(), anyString());

        mvc.perform(put("/api/menu-permissions").contentType(JSON).content("{\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"permissions\":[{\"menuId\":\"MENU-1\",\"accessAllowedYn\":\"Y\"}],\"reason\":\"권한 저장\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
        verify(adminService).savePermissions(org.mockito.ArgumentMatchers.<Map<String, Object>>any(), anyString());

        mvc.perform(post("/api/code-groups").contentType(JSON).content("{\"groupId\":\"NEW_GROUP\",\"groupName\":\"신규 그룹\",\"useYn\":\"Y\",\"reason\":\"등록\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.groupId").value("NEW_GROUP"));

        mvc.perform(patch("/api/code-groups/ROLE_SOURCE").contentType(JSON).content("{\"groupName\":\"역할 출처\",\"useYn\":\"N\",\"reason\":\"중지\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.useYn").value("N"));

        mvc.perform(post("/api/detail-codes").contentType(JSON).content("{\"groupId\":\"ROLE_SOURCE\",\"codeValue\":\"POSITION_BASED\",\"codeName\":\"보직기반\",\"sortOrder\":2,\"useYn\":\"Y\",\"validFrom\":\"2026-01-01\",\"reason\":\"등록\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.codeValue").value("POSITION_BASED"));

        mvc.perform(patch("/api/detail-codes/DC-1").contentType(JSON).content("{\"groupId\":\"ROLE_SOURCE\",\"codeValue\":\"MANUAL\",\"codeName\":\"수동\",\"sortOrder\":1,\"useYn\":\"N\",\"validFrom\":\"2026-01-01\",\"reason\":\"중지\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.useYn").value("N"));
    }

    @Test
    void write_negative_cases_return_validation_not_found_and_conflict_contracts() throws Exception {
        doThrow(new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "대상을 찾을 수 없습니다."))
            .when(adminService).updateRole(eq("UNKNOWN"), anyMap(), anyString());
        doThrow(new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "종료일은 시작일보다 빠를 수 없습니다."))
            .when(adminService).updateOrganizationRelation(eq("KNUE-BAD"), anyMap(), anyString());
        doThrow(new ApiException(HttpStatus.CONFLICT, "CONFLICT", "회수할 활성 역할이 없습니다."))
            .when(adminService).revokeUserRole(eq("UR-REVOKED"), anyMap(), anyString());

        mvc.perform(patch("/api/roles/UNKNOWN").contentType(JSON).content("{\"roleName\":\"없음\",\"rolePurpose\":\"없음\",\"defaultDataScope\":\"OWN\",\"useYn\":\"Y\",\"reason\":\"수정\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));

        mvc.perform(put("/api/organizations/KNUE-BAD/relation").contentType(JSON).content("{\"parentOrganizationCode\":\"ROOT\",\"effectiveStartDate\":\"2026-12-31\",\"effectiveEndDate\":\"2026-01-01\",\"reason\":\"기간 오류\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mvc.perform(patch("/api/user-roles/UR-REVOKED/revoke").contentType(JSON).content("{\"reason\":\"중복 회수\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error.code").value("CONFLICT"));
    }

    private static Map<String, Object> row(Object... values) {
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            row.put(String.valueOf(values[i]), values[i + 1]);
        }
        return row;
    }
}
