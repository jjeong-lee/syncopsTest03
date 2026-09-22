import { apiRequest } from "../../shared/api/client";

export type CommonEnvironmentSetting = {
  settingKey: string;
  settingValue: string;
};
export type CommonEnvironmentSettingsSearch = {
  settingKey: string;
  size: "20" | "50" | "100";
};

export async function listCommonEnvironmentSettings(
  search: CommonEnvironmentSettingsSearch,
) {
  const query = new URLSearchParams({ page: "0", size: search.size });
  if (search.settingKey) query.set("settingKey", search.settingKey);
  return apiRequest<CommonEnvironmentSetting[]>(
    `/api/system/settings/common-environment?${query.toString()}` as `/api/${string}`,
  );
}

export async function saveCommonEnvironmentSettings(
  setting: CommonEnvironmentSetting,
) {
  return apiRequest<CommonEnvironmentSetting>(
    "/api/system/settings/common-environment",
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(setting),
    },
  );
}
