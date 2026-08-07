export type ManagedRecord = {
  id: string;
  area?: string;
  title?: string;
  status?: string;
  useYn?: string;
  payload?: Record<string, unknown>;
  createdAt?: string;
  updatedAt?: string;
  [key: string]: unknown;
};

export type SessionUser = {
  loginId: string;
  userName: string;
  roleCodes: string[];
};

export type ApiResponse<T> = {
  success: boolean;
  data: T;
  error?: { code: string; message: string; details?: Record<string, string> };
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
  });
  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !body.success) {
    const error = new Error(body.error?.message ?? "요청 처리에 실패했습니다.");
    (error as Error & { code?: string; details?: unknown }).code =
      body.error?.code;
    (error as Error & { details?: unknown }).details = body.error?.details;
    throw error;
  }
  return body.data;
}

export function readRecordValue(
  record: ManagedRecord | null | undefined,
  key: string,
): string {
  if (!record) return "";
  const direct = record[key];
  const payload =
    record.payload && typeof record.payload === "object"
      ? record.payload[key]
      : undefined;
  const value = direct ?? payload;
  if (value === null || value === undefined) return "";
  if (typeof value === "object") return JSON.stringify(value);
  return String(value);
}

export function mergePayload(record: ManagedRecord): ManagedRecord {
  const reserved = new Set([
    "id",
    "area",
    "title",
    "status",
    "useYn",
    "payload",
    "createdAt",
    "updatedAt",
  ]);
  const payload = { ...(record.payload ?? {}) };
  Object.entries(record).forEach(([key, value]) => {
    if (!reserved.has(key) && value !== undefined) payload[key] = value;
  });
  return {
    id: record.id,
    title: record.title ?? record.id,
    status: record.status,
    useYn: record.useYn,
    payload,
    ...Object.fromEntries(
      Object.entries(record).filter(([key]) => !reserved.has(key)),
    ),
  };
}

export function buildListUrl(
  path: string,
  keyword: string,
  size: number,
  page = 0,
  filters: Record<string, string> = {},
): string {
  const params = new URLSearchParams({
    keyword,
    size: String(size),
    page: String(page),
  });
  Object.entries(filters).forEach(([key, value]) => {
    if (value) params.set(key, value);
  });
  return `${path}?${params.toString()}`;
}

export const api = {
  login: (loginId: string, password: string) =>
    request<SessionUser>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ loginId, password }),
    }),
  me: () => request<SessionUser>("/api/auth/me"),
  logout: () =>
    request<{ status: string }>("/api/auth/logout", {
      method: "POST",
      body: JSON.stringify({}),
    }),
  list: (
    path: string,
    keyword: string,
    size: number,
    page = 0,
    filters: Record<string, string> = {},
  ) =>
    request<ManagedRecord[]>(buildListUrl(path, keyword, size, page, filters)),
  save: (path: string, record: ManagedRecord) =>
    request<ManagedRecord>(path, {
      method: "POST",
      body: JSON.stringify(mergePayload(record)),
    }),
};
