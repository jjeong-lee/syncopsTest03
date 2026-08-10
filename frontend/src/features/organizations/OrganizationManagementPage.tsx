import { useState } from "react";
import { ApiRequestError, apiRequest } from "../../shared/api/client";

export type OrganizationNode = {
  organizationId: string;
  organizationCode: string;
  organizationName: string;
};

export type OrganizationSummary = {
  organizationId: string;
  organizationCode: string;
  organizationName: string;
  organizationType: string;
  parentOrganizationId: string | null;
  parentOrganizationCode: string | null;
  parentOrganizationName: string | null;
  effectiveStartDate: string | null;
  effectiveEndDate: string | null;
  children: OrganizationNode[];
};

type RelationshipForm = {
  parentOrganizationId: string;
  effectiveStartDate: string;
  effectiveEndDate: string;
};

const emptyRelationshipForm: RelationshipForm = {
  parentOrganizationId: "",
  effectiveStartDate: "",
  effectiveEndDate: "",
};

export function OrganizationManagementPage() {
  const [organizationCode, setOrganizationCode] = useState("");
  const [organizations, setOrganizations] = useState<OrganizationSummary[]>([]);
  const [selectedOrganization, setSelectedOrganization] =
    useState<OrganizationSummary | null>(null);
  const [relationship, setRelationship] = useState<RelationshipForm>(
    emptyRelationshipForm,
  );
  const [state, setState] = useState<
    "idle" | "loading" | "empty" | "error" | "permission" | "success"
  >("idle");
  const [message, setMessage] = useState("");

  const applySelectedOrganization = (
    organization: OrganizationSummary | null,
  ) => {
    setSelectedOrganization(organization);
    setRelationship(
      organization
        ? {
            parentOrganizationId: organization.parentOrganizationId ?? "",
            effectiveStartDate: organization.effectiveStartDate ?? "",
            effectiveEndDate: organization.effectiveEndDate ?? "",
          }
        : emptyRelationshipForm,
    );
  };

  const search = async (preserveSelection = false) => {
    setState("loading");
    if (!preserveSelection) {
      applySelectedOrganization(null);
      setMessage("");
    }
    const query = organizationCode
      ? `?${new URLSearchParams({ organizationCode })}`
      : "";
    try {
      const response = await apiRequest<OrganizationSummary[]>(
        `/api/organizations${query}` as `/api/${string}`,
      );
      setOrganizations(response.data);
      if (preserveSelection && selectedOrganization) {
        applySelectedOrganization(
          response.data.find(
            (organization) =>
              organization.organizationId ===
              selectedOrganization.organizationId,
          ) ?? null,
        );
      }
      setState(response.data.length === 0 ? "empty" : "success");
    } catch (error) {
      setState(
        error instanceof ApiRequestError && error.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        error instanceof ApiRequestError && error.status === 403
          ? "권한이 없습니다."
          : "조직 목록을 조회하지 못했습니다.",
      );
    }
  };

  const saveRelationship = async () => {
    if (!selectedOrganization) return;
    setState("loading");
    setMessage("");
    try {
      await apiRequest<null>(
        `/api/organizations/${selectedOrganization.organizationId}/relationship`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(relationship),
        },
      );
      setMessage("저장한 조직 관계를 다시 조회했습니다.");
      await search(true);
    } catch (error) {
      setState(
        error instanceof ApiRequestError && error.status === 403
          ? "permission"
          : "error",
      );
      setMessage(
        error instanceof ApiRequestError && error.status === 403
          ? "권한이 없습니다."
          : "조직 관계를 저장하지 못했습니다.",
      );
    }
  };

  if (state === "permission") {
    return (
      <section
        className="organization-management organization-state"
        aria-live="polite"
      >
        <h1>조직 관리</h1>
        <p>권한이 없습니다.</p>
      </section>
    );
  }

  return (
    <section
      className="organization-management"
      aria-labelledby="organization-management-title"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 사용자·조직 관리 &gt; 조직 관리
      </p>
      <h1 id="organization-management-title">조직 관리</h1>
      <form
        className="organization-search-form"
        onSubmit={(event) => {
          event.preventDefault();
          void search();
        }}
      >
        <label htmlFor="organization-code">
          조직코드
          <input
            id="organization-code"
            aria-label="조직코드"
            value={organizationCode}
            onChange={(event) => setOrganizationCode(event.target.value)}
          />
        </label>
        <button type="submit" className="primary-action">
          조회
        </button>
      </form>

      {message && state === "success" && (
        <p className="success-message" role="status">
          {message}
        </p>
      )}
      <section className="organization-results" aria-live="polite">
        <div className="section-title">
          <h2>조회 결과</h2>
          {state === "success" && <span>{organizations.length}개</span>}
        </div>
        {state === "loading" && (
          <p className="loading-message">
            조직 목록과 계층을 불러오는 중입니다.
          </p>
        )}
        {state === "empty" && (
          <p className="empty-message">조직코드 조건에 맞는 조직이 없습니다.</p>
        )}
        {state === "error" && (
          <p className="error-message">
            {message}{" "}
            <button
              type="button"
              className="text-action"
              onClick={() => void search()}
            >
              다시 시도
            </button>
          </p>
        )}
        {state === "success" && (
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>조직코드</th>
                  <th>조직명</th>
                  <th>조직 유형</th>
                  <th>상위 조직</th>
                  <th>하위 조직</th>
                </tr>
              </thead>
              <tbody>
                {organizations.map((organization) => (
                  <tr
                    key={organization.organizationId}
                    className={
                      selectedOrganization?.organizationId ===
                      organization.organizationId
                        ? "selected-row"
                        : ""
                    }
                  >
                    <td>{organization.organizationCode}</td>
                    <td>
                      <button
                        type="button"
                        className="row-link"
                        aria-label={`${organization.organizationName} 조직 선택`}
                        onClick={() => applySelectedOrganization(organization)}
                      >
                        {organization.organizationName}
                      </button>
                    </td>
                    <td>{organization.organizationType}</td>
                    <td>{organization.parentOrganizationName ?? "-"}</td>
                    <td>
                      {organization.children
                        .map((child) => child.organizationName)
                        .join(", ") || "-"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {selectedOrganization && (
        <>
          <section className="organization-hierarchy">
            <h2>선택 조직의 계층</h2>
            <div className="hierarchy-line">
              <span>
                {selectedOrganization.parentOrganizationName ??
                  "상위 조직 없음"}
              </span>
              <strong>{selectedOrganization.organizationName}</strong>
              <span>
                {selectedOrganization.children
                  .map((child) => child.organizationName)
                  .join(", ") || "하위 조직 없음"}
              </span>
            </div>
          </section>
          <section className="relationship-form">
            <h2>조직 관계·적용기간 편집</h2>
            <label>
              상위조직
              <select
                aria-label="상위조직"
                value={relationship.parentOrganizationId}
                onChange={(event) =>
                  setRelationship({
                    ...relationship,
                    parentOrganizationId: event.target.value,
                  })
                }
              >
                <option value="">선택</option>
                {organizations
                  .filter(
                    (organization) =>
                      organization.organizationId !==
                      selectedOrganization.organizationId,
                  )
                  .map((organization) => (
                    <option
                      key={organization.organizationId}
                      value={organization.organizationId}
                    >
                      {organization.organizationName}
                    </option>
                  ))}
              </select>
            </label>
            <label>
              적용 시작일
              <input
                aria-label="적용 시작일"
                type="date"
                value={relationship.effectiveStartDate}
                onChange={(event) =>
                  setRelationship({
                    ...relationship,
                    effectiveStartDate: event.target.value,
                  })
                }
              />
            </label>
            <label>
              적용 종료일
              <input
                aria-label="적용 종료일"
                type="date"
                value={relationship.effectiveEndDate}
                onChange={(event) =>
                  setRelationship({
                    ...relationship,
                    effectiveEndDate: event.target.value,
                  })
                }
              />
            </label>
            {state === "error" && <p className="error-message">{message}</p>}
            <div className="form-actions">
              <button
                type="button"
                className="text-action"
                onClick={() => applySelectedOrganization(selectedOrganization)}
              >
                취소
              </button>
              <button
                type="button"
                className="primary-action"
                disabled={state === "loading"}
                onClick={() => void saveRelationship()}
              >
                관계·적용기간 저장
              </button>
            </div>
          </section>
        </>
      )}
    </section>
  );
}
