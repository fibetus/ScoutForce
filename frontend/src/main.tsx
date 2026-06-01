/**
 * Vite/React entry point for the ScoutForce SPA.
 *
 * This module is the script referenced by `index.html`
 * (`<script type="module" src="/src/main.tsx">`). It performs the one-time
 * client bootstrap:
 *
 * 1. Imports the global stylesheet (`index.css`), which pulls in the Tailwind
 *    base/components/utilities layers and the `slideIn` keyframe used by the
 *    toast notification.
 * 2. Locates the `#root` mount node defined in `index.html`.
 * 3. Creates a React 18 concurrent root and renders the {@link App} container
 *    (the `ScoutForceNBA` shell) inside {@link StrictMode}.
 *
 * The `#root` lookup is null-guarded so a missing or renamed container surfaces
 * as an explicit, descriptive error instead of a silent no-op render.
 */

import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import App from "./App";
import "./index.css";

const rootElement = document.getElementById("root");

if (rootElement === null) {
  throw new Error(
    'Root container "#root" was not found in the document. ' +
      "Ensure index.html contains <div id=\"root\"></div>.",
  );
}

createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
