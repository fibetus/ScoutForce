package pl.s30331.ScoutForce.controller.dto;

/**
 * Web-layer transfer object exposing the minimal club information required by the frontend.
 *
 * <p>This is a presentation-layer projection of the {@code Club} domain entity. It intentionally
 * exposes only the fields consumed by the UI (host/guest club name and the player's club details),
 * avoiding serialization of the entity's lazy associations.</p>
 *
 * @param name   the club name
 * @param city   the city the club is based in
 * @param league the league the club competes in
 */
public record ClubDto(String name, String city, String league) {
}
