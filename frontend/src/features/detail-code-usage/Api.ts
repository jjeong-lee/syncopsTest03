import { apiRequest } from "../../shared/api/client";

export type DetailCodeUsageSetting = {
  groupId: string;
  codeValue: string;
  useYn: "Y" | "N";
  effectiveStartDate: string;
  effectiveEndDate: string | null;
};

export type DetailCodeUsageSearch = {
  groupId: string;
  codeValue: string;
  useYn: string;
  size: "20" | "50" | "100";
};

export async function listDetailCodeUsageSettings(
  search: DetailCodeUsageSearch,
) {
  const query = new URLSearchParams({ page: "0", size: search.size });
  if (search.groupId) query.set("groupId", search.groupId);
  if (search.codeValue) query.set("codeValue", search.codeValue);
  if (search.useYn) query.set("useYn", search.useYn);
  return apiRequest<DetailCodeUsageSetting[]>(
    `/api/system/common-codes/usage?${query.toString()}` as `/api/${string}`,
  );
}

export async function saveDetailCodeUsageSettings(
  setting: DetailCodeUsageSetting,
) {
  return apiRequest<DetailCodeUsageSetting>("/api/system/common-codes/usage", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(setting),
  });
}
