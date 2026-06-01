/** @type {import('tailwindcss').Config} */
// Tailwind CSS v3 configuration.
// Content globs cover the HTML entry point and all source TS/TSX files so that
// utility classes used in the ported prototype components are not purged.
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {},
  },
  plugins: [],
};
