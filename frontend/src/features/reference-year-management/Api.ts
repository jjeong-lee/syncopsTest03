import { apiRequest } from "../../shared/api/client";

export type ReferenceYearSetting = {
  currentEvaluationYear: number;
  defaultQueryYear: number;
  targetYear: number;
  baselineCopyYn: "예" | "아니오";
  initializationYn: "예" | "아니오";
};

export type ReferenceYearSettingsSearch = {
  targetYear: string;
  size: "20" | "50" | "100";
};

export async function listReferenceYearSettings(
  search: ReferenceYearSettingsSearch,
) {
  const query = new URLSearchParams({ page: "0", size: search.size });
  if (search.targetYear) query.set("targetYear", search.targetYear);
  return apiRequest<ReferenceYearSetting[]>(
    `/api/system/settings/reference-years?${query.toString()}` as `/api/${string}`,
  );
}

export async function saveReferenceYearSettings(setting: ReferenceYearSetting) {
  return apiRequest<ReferenceYearSetting>(
    "/api/system/settings/reference-years",
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(setting),
    },
  );
}
