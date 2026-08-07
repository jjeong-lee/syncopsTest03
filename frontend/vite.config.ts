import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  preview: { allowedHosts: true },
  test: { environment: "jsdom", setupFiles: "./tests/setup.ts" },
});
