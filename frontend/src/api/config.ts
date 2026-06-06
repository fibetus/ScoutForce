/**
 * Base URL of the Spring Boot backend.
 *
 * Sourced from the `VITE_API_BASE_URL` environment variable at build time, falling
 * back to the local development server (`http://localhost:8080`) when the variable
 * is not provided.
 */
export const API_BASE_URL: string =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

/**
 * License number of the demo scout seeded in the backend (`DataInitializer`).
 * The runtime scout id is resolved via `GET /api/scouts/default` on app startup.
 */
export const DEFAULT_SCOUT_LICENSE = "SCT-001";
