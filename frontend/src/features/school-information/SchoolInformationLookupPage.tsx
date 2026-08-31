import { useState } from "react";

import { ApiRequestError, apiRequest } from "../../shared/api/client";

type SchoolInformation = {
  educationOfficeName: string;
  schoolName: string;
  schoolTypeName: string;
  locationName: string;
  foundationName: string;
  roadAddress: string;
  telephoneNumber: string;
};

type SearchForm = { schoolName: string; educationOfficeCode: string };

const emptySearch: SearchForm = { schoolName: "", educationOfficeCode: "" };

function schoolInformationQueryPath(search: SearchForm): `/api/${string}` {
  const parameters = new URLSearchParams();
  if (search.schoolName) parameters.set("schoolName", search.schoolName);
  if (search.educationOfficeCode) {
    parameters.set("educationOfficeCode", search.educationOfficeCode);
  }
  const query = parameters.toString();
  return `/api/external-integrations/schools${query ? `?${query}` : ""}`;
}

export function SchoolInformationLookupPage() {
  const [search, setSearch] = useState<SearchForm>(emptySearch);
  const [schools, setSchools] = useState<SchoolInformation[]>([]);
  const [state, setState] = useState<
    "idle" | "loading" | "success" | "empty" | "error"
  >("idle");
  const [message, setMessage] = useState("");

  const loadSchools = async () => {
    setSchools([]);
    setMessage("");
    setState("loading");
    try {
      const response = await apiRequest<SchoolInformation[]>(
        schoolInformationQueryPath(search),
      );
      setSchools(response.data);
      setState(response.data.length === 0 ? "empty" : "success");
    } catch (error) {
      setState("error");
      setMessage(
        error instanceof ApiRequestError
          ? error.message
          : "학교정보 조회 요청에 실패했습니다.",
      );
    }
  };

  return (
    <section
      className="school-information-lookup"
      aria-labelledby="school-information-lookup-title"
      data-testid="school-information-lookup-page"
    >
      <p className="breadcrumb">
        시스템 관리 &gt; 외부 연동 &gt; 학교정보 조회
      </p>
      <h1 id="school-information-lookup-title">학교정보 조회</h1>
      <section
        className="school-information-search"
        aria-label="학교정보 조회 조건"
      >
        <label>
          학교명
          <input
            aria-label="학교명"
            data-testid="school-name-input"
            value={search.schoolName}
            onChange={(event) =>
              setSearch({ ...search, schoolName: event.target.value })
            }
          />
        </label>
        <label>
          시도교육청 코드
          <input
            aria-label="시도교육청 코드"
            data-testid="education-office-code-input"
            value={search.educationOfficeCode}
            onChange={(event) =>
              setSearch({ ...search, educationOfficeCode: event.target.value })
            }
          />
        </label>
        <div className="form-actions">
          <button
            type="button"
            className="text-action"
            data-testid="school-information-reset-button"
            onClick={() => setSearch(emptySearch)}
          >
            초기화
          </button>
          <button
            type="button"
            className="primary-action"
            data-testid="school-information-search-button"
            disabled={state === "loading"}
            onClick={() => void loadSchools()}
          >
            조회
          </button>
        </div>
      </section>

      {state === "loading" && (
        <p className="loading-message" aria-live="polite">
          학교정보를 조회 중입니다.
        </p>
      )}
      {state === "empty" && (
        <p className="empty-message">조회 결과가 없습니다</p>
      )}
      {state === "error" && (
        <p className="error-message" role="alert">
          {message}{" "}
          <button
            type="button"
            className="text-action"
            data-testid="school-information-retry-button"
            onClick={() => void loadSchools()}
          >
            다시 시도
          </button>
        </p>
      )}
      {state === "success" && (
        <section
          className="school-information-results"
          aria-label="학교정보 조회 결과"
        >
          <div className="section-title">
            <h2>조회 결과</h2>
            <p className="result-count">조회 결과 {schools.length}건</p>
          </div>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  <th>교육청명</th>
                  <th>학교명</th>
                  <th>학교종류</th>
                  <th>소재지</th>
                  <th>설립구분</th>
                  <th>도로명주소</th>
                  <th>전화번호</th>
                </tr>
              </thead>
              <tbody>
                {schools.map((school) => (
                  <tr
                    key={`${school.educationOfficeName}-${school.schoolName}`}
                    data-testid="school-information-row"
                  >
                    <td>{school.educationOfficeName}</td>
                    <td>{school.schoolName}</td>
                    <td>{school.schoolTypeName}</td>
                    <td>{school.locationName}</td>
                    <td>{school.foundationName}</td>
                    <td>{school.roadAddress}</td>
                    <td>{school.telephoneNumber}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </section>
  );
}
