export type ApiError = {
  code: string;
  message: string;
  fieldErrors?: Record<string, string>;
};
export type ApiResponse<T> = { success: boolean; data: T; error?: ApiError };
export type Entity = Record<
  string,
  string | number | boolean | null | undefined
>;
export type CurrentUser = Entity & { roles?: string[]; menus?: Entity[] };
