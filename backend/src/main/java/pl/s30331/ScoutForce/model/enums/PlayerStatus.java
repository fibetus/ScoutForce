package pl.s30331.ScoutForce.model.enums;

/**
 * Scouting pipeline status of a {@link pl.s30331.ScoutForce.model.Player}.
 */
public enum PlayerStatus {
    /** Newly discovered; not yet fully evaluated. */
    NEW,
    /** Actively observed by at least one scout. */
    OBSERVED,
    /** Awaiting medical clearance. */
    MEDICAL_VERIFICATION,
    /** Invited to a team workout. */
    INVITED_TO_WORKOUT,
    /** Shortlisted on the organisation's draft board. */
    INVITED_TO_BIG_BOARD,
    /** Removed from active consideration. */
    DELISTED
}
