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
 * Identifier of the scout whose data is shown by the application.
 *
 * Authentication is intentionally out of scope at this stage, so the app operates
 * as a single fixed test scout (id `2` from `DataInitializer`). Sourced from the
 * `VITE_SCOUT_ID` environment variable, defaulting to `2` when not provided.
 */
export const CURRENT_SCOUT_ID: number = Number(
  import.meta.env.VITE_SCOUT_ID ?? 2,
);
