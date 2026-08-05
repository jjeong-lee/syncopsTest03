import { Field, Row } from "../types";

const normalizeRoles = (value: unknown): string[] => {
  if (Array.isArray(value)) {
    return value
      .map((item) =>
        typeof item === "string" ? item : String((item as Row).roleCode ?? ""),
      )
      .filter(Boolean);
  }
  if (typeof value === "string")
    return value
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean);
  return [];
};

type Props = {
  field: Field;
  row: Row;
  disabled: boolean;
  immutableLocked?: boolean;
  error?: string;
  onChange: (key: string, value: unknown) => void;
};

export function FieldControl({
  field,
  row,
  disabled,
  immutableLocked = false,
  error,
  onChange,
}: Props) {
  const value = row[field.key] ?? "";
  const commonId = `field-${field.key}`;
  const fieldDisabled = disabled || field.readonly || immutableLocked;

  return (
    <label
      className="block text-sm font-semibold text-plumBlack"
      htmlFor={commonId}
    >
      <span className="flex items-center justify-between gap-2">
        {field.label}
        {(field.readonly || immutableLocked) && (
          <span className="text-xs font-medium text-oliveMuted">readonly</span>
        )}
      </span>
      {field.type === "textarea" || field.type === "json" ? (
        <textarea
          id={commonId}
          className="pin-input mt-2 min-h-[96px] py-3"
          value={String(value)}
          placeholder={field.placeholder}
          disabled={fieldDisabled}
          onChange={(event) => onChange(field.key, event.target.value)}
        />
      ) : field.type === "select" ? (
        <select
          id={commonId}
          className="pin-input mt-2"
          value={String(value)}
          disabled={fieldDisabled}
          onChange={(event) => onChange(field.key, event.target.value)}
        >
          {(field.options ?? []).map((option) => (
            <option key={option} value={option}>
              {option || "전체"}
            </option>
          ))}
        </select>
      ) : field.type === "roles" ? (
        <div className="mt-2 flex flex-wrap gap-2 rounded-[24px] bg-[#f6f6f3] p-3">
          {(field.options ?? []).map((option) => {
            const checked = normalizeRoles(value).includes(option);
            return (
              <label
                key={option}
                className={`cursor-pointer rounded-2xl px-3 py-2 text-sm font-semibold transition-colors ${checked ? "bg-pinterest text-white" : "bg-white hover:bg-warmSand"}`}
              >
                <input
                  className="sr-only"
                  type="checkbox"
                  checked={checked}
                  disabled={fieldDisabled}
                  onChange={(event) => {
                    const next = new Set(normalizeRoles(value));
                    if (event.target.checked) next.add(option);
                    else next.delete(option);
                    onChange(field.key, Array.from(next));
                  }}
                />
                {option}
              </label>
            );
          })}
        </div>
      ) : (
        <input
          id={commonId}
          className="pin-input mt-2"
          type={
            field.type === "date" || field.type === "number"
              ? field.type
              : "text"
          }
          value={String(value)}
          placeholder={field.placeholder}
          disabled={fieldDisabled}
          onChange={(event) => onChange(field.key, event.target.value)}
        />
      )}
      {field.helper && (
        <span className="mt-1 block text-xs text-oliveMuted">
          {field.helper}
        </span>
      )}
      {error && (
        <span className="mt-1 block text-xs font-semibold text-[#9e0a0a]">
          {error}
        </span>
      )}
    </label>
  );
}
