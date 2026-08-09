export interface ApiResponse<T> {
  success: true;
  data: T;
  meta: Record<string, unknown>;
}

type ApiErrorBody = {
  error?: {
    code?: string;
    field?: string;
    message?: string;
  };
};

export class ApiRequestError extends Error {
  constructor(
    public readonly status: number,
    public readonly field?: string,
    message?: string,
  ) {
    super(message || `API 요청이 실패했습니다. (${status})`);
  }
}

export async function apiRequest<T>(
  path: `/api/${string}`,
  init?: RequestInit,
): Promise<ApiResponse<T>> {
  const response = await fetch(path, {
    ...init,
    credentials: "include",
    headers: {
      Accept: "application/json",
      ...init?.headers,
    },
  });

  if (!response.ok) {
    const body = (await response
      .json()
      .catch(() => null)) as ApiErrorBody | null;
    throw new ApiRequestError(
      response.status,
      body?.error?.field,
      body?.error?.message,
    );
  }

  return response.json() as Promise<ApiResponse<T>>;
}
