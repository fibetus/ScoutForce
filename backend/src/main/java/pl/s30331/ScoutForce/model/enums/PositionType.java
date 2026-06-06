package pl.s30331.ScoutForce.model.enums;

/**
 * On-court position of a {@link pl.s30331.ScoutForce.model.Player}.
 *
 * <p>Each constant carries a standard abbreviation and a numeric ordering used for sorting.</p>
 */
public enum PositionType {
    POINT_GUARD("PG", 1),
    SHOOTING_GUARD("SG", 2),
    SMALL_FORWARD("SF", 3),
    POWER_FORWARD("PF", 4),
    CENTER("C", 5);

    private final String abbreviation;
    private final int number;

    PositionType(String abbreviation, int number) {
        this.abbreviation = abbreviation;
        this.number = number;
    }

    /** @return standard abbreviation (e.g. {@code PG}) */
    public String getAbbreviation() { return abbreviation; }

    /** @return numeric position index (1 = point guard … 5 = center) */
    public int getNumber()          { return number; }
}
