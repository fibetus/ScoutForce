package pl.s30331.ScoutForce.model;

/**
 * <<interface>> PlayerExperience
 *
 * Represents the experience type of a Player (dynamic inheritance / Strategy pattern).
 * Implemented by UniversityExperience and ProfessionalExperience.
 * At most one implementation is active for a given Player at any time.
 */
public interface PlayerExperience {

    /** Returns a human-readable label for the type of experience. */
    String getExperienceType();
}
