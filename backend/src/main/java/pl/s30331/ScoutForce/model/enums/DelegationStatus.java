package pl.s30331.ScoutForce.model.enums;

/**
 * Lifecycle state of a {@link pl.s30331.ScoutForce.model.Delegation}.
 */
public enum DelegationStatus {
    /** Created but not yet approved. */
    PLANNED,
    /** Approved by management; not yet started. */
    APPROVED,
    /** Scout is currently on the trip. */
    IN_PROGRESS,
    /** Trip completed. */
    FINISHED,
    /** Cancelled before or during travel. */
    CANCELLED
}
