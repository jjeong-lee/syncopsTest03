/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}", "./tests/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#202124",
        pine: "#1f513f",
        clay: "#c66f4e",
        paper: "#f7f1e8",
        mist: "#e6edf0",
      },
    },
  },
  plugins: [],
};
