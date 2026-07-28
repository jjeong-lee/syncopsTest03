import { Button } from "./ui";

export function ConfirmDialog({
  title,
  description,
  confirmText = "확인",
  onConfirm,
}: {
  title: string;
  description: string;
  confirmText?: string;
  onConfirm?: () => void;
}) {
  return (
    <div
      className="rounded-lg border bg-muted/40 p-4"
      role="group"
      aria-label={title}
    >
      <div className="space-y-1">
        <h3 className="text-sm font-semibold">{title}</h3>
        <p className="text-sm text-muted-foreground">{description}</p>
      </div>
      <div className="mt-3 flex justify-end gap-2">
        <Button variant="outline" type="button">
          취소
        </Button>
        <Button type="button" onClick={onConfirm}>
          {confirmText}
        </Button>
      </div>
    </div>
  );
}
