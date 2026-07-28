declare module "node:fs" {
  export function readFileSync(
    path: URL | string,
    encoding: BufferEncoding,
  ): string;
}

type BufferEncoding = "utf8";
