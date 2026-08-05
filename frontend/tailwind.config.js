/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}", "./tests/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: [
          "DM Sans",
          "system-ui",
          "-apple-system",
          "Segoe UI",
          "sans-serif",
        ],
      },
      colors: {
        pinterest: "#e60023",
        pinterestHover: "#cc001f",
        warmSand: "#e5e5e0",
        warmSandHover: "#dadad3",
        plumBlack: "#211922",
        oliveMuted: "#62625b",
      },
    },
  },
  plugins: [],
};
