import { describe, expect, it } from "vitest";
import {
  buildMutationPayload,
  buildSearchKeyword,
  resolveMutationMethod,
} from "../src/pages/AdminScreenPage";
import { screens } from "../src/featureCatalog";

describe("AdminScreenPage UI contract helpers", () => {
  it("uses explicit contract filter values as the API keyword without empty fields", () => {
    expect(
      buildSearchKeyword("", {
        employeeNo: "K1234",
        userName: "",
        departmentName: "공과대학",
      }),
    ).toBe("K1234 공과대학");

    expect(
      buildSearchKeyword("  직접 검색어  ", {
        employeeNo: "K1234",
      }),
    ).toBe("직접 검색어");
  });

  it("sends only editable modal fields and the required reason for user changes", () => {
    const userScreen = screens.find(
      (screen) => screen.route === "/admin/cmn/fr/001",
    );

    const payload = buildMutationPayload(
      userScreen!,
      {
        recordId: "USER-1",
        employeeNo: "K1234",
        userName: "원천 사용자명",
        systemUseYn: "N",
        businessRole: "R02",
        reason: "권한 회수",
        status: "DELETED",
        updatedAt: "2026-08-06T00:00:00Z",
      },
      "edit",
    );

    expect(payload).toEqual({
      systemUseYn: "N",
      businessRole: "R02",
      reason: "권한 회수",
    });
  });

  it("does not expose mutable payload fields for read-only result screens", () => {
    const resultScreen = screens.find(
      (screen) => screen.route === "/admin/cmn/fr/081",
    );

    const payload = buildMutationPayload(
      resultScreen!,
      {
        batchExecutionId: "EXEC-1",
        failureCount: 1,
        reason: "재처리",
      },
      "edit",
    );

    expect(payload).toEqual({});
    expect(resolveMutationMethod(resultScreen!, "edit")).toBe("GET");
  });

  it("uses PATCH for editing screens whose create contract is POST", () => {
    const roleScreen = screens.find(
      (screen) => screen.route === "/admin/cmn/fr/005",
    );

    expect(resolveMutationMethod(roleScreen!, "create")).toBe("POST");
    expect(resolveMutationMethod(roleScreen!, "edit")).toBe("PATCH");
  });
});
