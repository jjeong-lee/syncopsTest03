import { apiRequest } from "../../shared/api/client";

export type MenuSummary = {
  menuId: string;
  menuName: string;
};

export type MenuUsageSetting = {
  menuId: string;
  useYn: "Y" | "N";
  exposureStartAt: string;
  exposureEndAt: string | null;
};

export type MenuUsageSearch = {
  menuId: string;
  useYn: string;
  size: "20" | "50" | "100";
};

export async function listMenus() {
  return apiRequest<MenuSummary[]>("/api/menus");
}

export async function listMenuUsageSettings(search: MenuUsageSearch) {
  const query = new URLSearchParams({ page: "0", size: search.size });
  if (search.menuId) query.set("menuId", search.menuId);
  if (search.useYn) query.set("useYn", search.useYn);
  return apiRequest<MenuUsageSetting[]>(
    `/api/system/menus/usage?${query.toString()}` as `/api/${string}`,
  );
}

export async function saveMenuUsageSettings(setting: MenuUsageSetting) {
  return apiRequest<MenuUsageSetting>("/api/system/menus/usage", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(setting),
  });
}
