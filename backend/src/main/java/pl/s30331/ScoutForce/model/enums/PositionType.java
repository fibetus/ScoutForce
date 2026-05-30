package pl.s30331.ScoutForce.model.enums;

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

    public String getAbbreviation() { return abbreviation; }
    public int getNumber()          { return number; }
}
