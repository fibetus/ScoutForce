package pl.s30331.ScoutForce.model.enums;

/**
 * Scout's acquisition recommendation at the end of a {@link pl.s30331.ScoutForce.model.ScoutingReport}.
 */
public enum RecommendationType {
    /** Highest conviction — pursue aggressively. */
    STRONG_BUY,
    /** Positive evaluation — recommend signing/drafting. */
    BUY,
    /** Insufficient upside or fit — no strong view. */
    NEUTRAL,
    /** Do not pursue further. */
    PASS
}
