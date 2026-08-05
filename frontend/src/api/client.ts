export type ApiPage<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
};

export type ApiFieldErrors = Record<string, string>;

export class ApiClientError extends Error {
  status: number;
  code?: string;
  fields: ApiFieldErrors;

  constructor(
    message: string,
    status: number,
    code?: string,
    fields: ApiFieldErrors = {},
  ) {
    super(message);
    this.name = "ApiClientError";
    this.status = status;
    this.code = code;
    this.fields = fields;
  }
}

type Envelope<T> = {
  success: boolean;
  data: T;
  error?: {
    code?: string;
    message: string;
    fields?: ApiFieldErrors;
    fieldErrors?: ApiFieldErrors;
  };
};

export function toQuery(params: Record<string, unknown> = {}) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || String(value).trim() === "")
      return;
    query.set(key, String(value));
  });
  const text = query.toString();
  return text ? `?${text}` : "";
}

export async function api<T>(
  path: `/api/${string}`,
  init: RequestInit = {},
): Promise<T> {
  const response = await fetch(path, {
    ...init,
    credentials: "include",
    headers: { "Content-Type": "application/json", ...(init.headers ?? {}) },
  });
  const envelope = (await response.json()) as Envelope<T>;
  if (!response.ok || !envelope.success) {
    const fields = envelope.error?.fields ?? envelope.error?.fieldErrors ?? {};
    throw new ApiClientError(
      envelope.error?.message ?? "API 요청에 실패했습니다.",
      response.status,
      envelope.error?.code,
      fields,
    );
  }
  return envelope.data;
}
