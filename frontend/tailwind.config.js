/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        knue: {
          green: "#03C75A",
          deep: "#064E3B",
          mist: "#E8F7EF",
          ink: "#17202A",
        },
      },
      fontFamily: { sans: ["Inter", "Pretendard", "system-ui", "sans-serif"] },
    },
  },
  plugins: [],
};
