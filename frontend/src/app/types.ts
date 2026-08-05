export type Menu = {
  menuId: string;
  parentMenuId?: string | null;
  menuName: string;
  routePath?: string;
  permissionLevel?: "NONE" | "READ" | "WRITE" | string;
};

export type Session = {
  userId: string;
  loginId: string;
  displayName: string;
  roleCodes: string[];
  menus: Menu[];
};

export type FieldType =
  | "text"
  | "date"
  | "number"
  | "select"
  | "textarea"
  | "roles"
  | "json";

export type Field = {
  key: string;
  label: string;
  readonly?: boolean;
  immutable?: boolean;
  type?: FieldType;
  options?: string[];
  placeholder?: string;
  helper?: string;
};

export type Filter = {
  key: string;
  label: string;
  type?: "text" | "select" | "date";
  options?: string[];
};

export type ScreenConfig = {
  id: string;
  title: string;
  route: string;
  menuPath: string;
  goal: string;
  archetype:
    | "search-list-detail"
    | "tree-editor"
    | "effective-period"
    | "permission-matrix"
    | "content-editor";
  endpoint: `/api/${string}`;
  treeEndpoint?: `/api/${string}`;
  detailPath?: (row: Record<string, unknown>) => `/api/${string}` | null;
  createPath?: `/api/${string}`;
  updatePath?: (row: Record<string, unknown>) => `/api/${string}` | null;
  extraAction?:
    | "userRoles"
    | "revokeUserRole"
    | "menuPermissions"
    | "menuReorder"
    | "codeGroupDetailLink";
  idKey: string;
  columns: { key: string; label: string }[];
  filters: Filter[];
  fields: Field[];
  readonlyKeys?: string[];
  newRecord?: Record<string, unknown>;
};

export type Row = Record<string, unknown>;
