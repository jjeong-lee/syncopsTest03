export type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: {
    code: string;
    message: string;
    fieldErrors?: Record<string, string>;
  };
};

export type PageResult<T> = {
  items: T[];
  page: { page: number; size: number; totalElements: number };
};

async function request<T>(
  path: string,
  init: RequestInit = {},
): Promise<ApiResponse<T>> {
  const response = await fetch(path, {
    credentials: "include",
    headers:
      init.body instanceof FormData
        ? init.headers
        : { "Content-Type": "application/json", ...(init.headers || {}) },
    ...init,
  });

  const json = (await response.json().catch(() => ({
    success: false,
    error: {
      code: String(response.status),
      message: "응답을 해석할 수 없습니다.",
    },
  }))) as ApiResponse<T>;

  if (!response.ok && json.success !== false) {
    return {
      success: false,
      error: { code: String(response.status), message: response.statusText },
    };
  }

  return json;
}

export const api = {
  login: (loginId: string, password: string) =>
    request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ loginId, password }),
    }),
  logout: () => request("/api/auth/logout", { method: "POST" }),
  me: () => request("/api/auth/me"),
  health: () => request("/api/health"),
  search: <T>(path: string, keyword: string, size: number, page = 0) => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    if (keyword.trim()) params.set("keyword", keyword.trim());
    return request<PageResult<T>>(`${path}?${params.toString()}`);
  },
  mutate: <T>(
    path: string,
    method: "POST" | "PATCH" | "PUT" | "DELETE",
    payload: Record<string, unknown>,
    id?: string | null,
  ) =>
    request<T>(
      id && method !== "PUT" ? `${path}/${encodeURIComponent(id)}` : path,
      {
        method,
        body: JSON.stringify(payload),
      },
    ),
  uploadAttachment: async (path: string, form: FormData) => {
    const response = await fetch(`${path}/attachments`, {
      method: "POST",
      credentials: "include",
      body: form,
    });
    return response.json() as Promise<ApiResponse<unknown>>;
  },
};
