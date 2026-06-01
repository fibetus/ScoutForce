import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Vite configuration for the ScoutForce SPA (React 18 + TypeScript).
// The React plugin enables Fast Refresh and the automatic JSX runtime.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
});
