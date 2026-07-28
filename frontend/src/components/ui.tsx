import {
  ButtonHTMLAttributes,
  InputHTMLAttributes,
  ReactNode,
  TextareaHTMLAttributes,
} from "react";

type Variant = "default" | "outline" | "secondary" | "ghost" | "destructive";

const buttonVariants: Record<Variant, string> = {
  default: "bg-primary text-primary-foreground shadow-sm hover:bg-primary/90",
  outline:
    "border border-input bg-background shadow-sm hover:bg-accent hover:text-accent-foreground",
  secondary:
    "bg-secondary text-secondary-foreground shadow-sm hover:bg-secondary/80",
  ghost: "hover:bg-accent hover:text-accent-foreground",
  destructive:
    "bg-destructive text-destructive-foreground shadow-sm hover:bg-destructive/90",
};

export function Button({
  className = "",
  variant = "default",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant }) {
  return (
    <button
      className={`inline-flex h-9 items-center justify-center gap-2 whitespace-nowrap rounded-md px-4 py-2 text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 ${buttonVariants[variant]} ${className}`}
      {...props}
    />
  );
}

export function Card({
  className = "",
  children,
}: {
  className?: string;
  children: ReactNode;
}) {
  return (
    <section
      className={`rounded-xl border bg-card text-card-foreground shadow-sm ${className}`}
    >
      {children}
    </section>
  );
}

export function CardHeader({
  className = "",
  children,
}: {
  className?: string;
  children: ReactNode;
}) {
  return (
    <div className={`grid gap-1.5 px-6 pt-6 ${className}`}>{children}</div>
  );
}

export function CardTitle({
  className = "",
  children,
}: {
  className?: string;
  children: ReactNode;
}) {
  return (
    <h2 className={`font-semibold leading-none tracking-tight ${className}`}>
      {children}
    </h2>
  );
}

export function CardDescription({
  className = "",
  children,
}: {
  className?: string;
  children: ReactNode;
}) {
  return (
    <p className={`text-sm text-muted-foreground ${className}`}>{children}</p>
  );
}

export function CardContent({
  className = "",
  children,
}: {
  className?: string;
  children: ReactNode;
}) {
  return <div className={`px-6 py-6 ${className}`}>{children}</div>;
}

export function Input({
  className = "",
  ...props
}: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={`flex h-9 w-full min-w-0 rounded-md border border-input bg-background px-3 py-1 text-base shadow-sm transition-colors duration-200 placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 md:text-sm ${className}`}
      {...props}
    />
  );
}

export function Textarea({
  className = "",
  ...props
}: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      className={`min-h-24 w-full rounded-md border border-input bg-background px-3 py-2 text-sm shadow-sm transition-colors duration-200 placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${className}`}
      {...props}
    />
  );
}

export function Badge({
  className = "",
  children,
}: {
  className?: string;
  children: ReactNode;
}) {
  return (
    <span
      className={`inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium ${className}`}
    >
      {children}
    </span>
  );
}
