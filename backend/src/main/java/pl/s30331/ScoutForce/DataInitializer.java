package pl.s30331.ScoutForce;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.*;
import pl.s30331.ScoutForce.model.enums.*;
import pl.s30331.ScoutForce.repository.ScoutRepository;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the database when empty.
 *
 * <p>Scenario coverage for Create Scouting Report:</p>
 * <ul>
 *   <li>Player A – observed, watched matches with stats → happy path + seeded report</li>
 *   <li>Player B – not observed by default scout</li>
 *   <li>Player C – observed, watched match → happy path</li>
 *   <li>Player D – observed, mix of watched/unwatched matches → filtering (A1)</li>
 *   <li>Player E – observed, no match stats → E1</li>
 *   <li>Player F – observed (direct association), stats only in unwatched match → E1</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ScoutRepository scoutRepository;
    private final EntityManager entityManager;

    /**
     * Populates the database when no scouts exist yet.
     *
     * @param args unused Spring Boot command-line arguments
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (scoutRepository.count() > 0) {
            return;
        }

        Club lakers     = persistClub("Los Angeles Lakers", "NBA", "Los Angeles", "USA", "Western", "Pacific");
        Club celtics    = persistClub("Boston Celtics", "NBA", "Boston", "USA", "Eastern", "Atlantic");
        Club duke       = persistClub("Duke Blue Devils", "NCAA", "Durham", "USA", null, null);
        Club realMadrid = persistClub("Real Madrid Baloncesto", "EuroLeague", "Madrid", "Spain", null, null);

        Director director = new Director();
        director.setFirstName("Rob");
        director.setLastName("Pelinka");
        director.setBirthDate(LocalDate.of(1969, 12, 23));
        director.setEmail("rob.pelinka@lakers.com");
        director.setHireDate(LocalDate.of(2017, 3, 10));
        director.setDailyRate(BigDecimal.valueOf(500));
        director.setEmployer(lakers);
        entityManager.persist(director);

        Scout defaultScout = new Scout();
        defaultScout.setFirstName("John");
        defaultScout.setLastName("Hammond");
        defaultScout.setBirthDate(LocalDate.of(1985, 6, 15));
        defaultScout.setEmail("john.hammond@lakers.com");
        defaultScout.setHireDate(LocalDate.of(2020, 1, 1));
        defaultScout.setDailyRate(BigDecimal.valueOf(300));
        defaultScout.setEmployer(lakers);
        defaultScout.setLicenseNumber("SCT-001");
        defaultScout.setSpecialization("Guards");
        defaultScout.setObservationRegion("NCAA West");
        defaultScout.setSentByDirector(director);
        director.getSentScouts().add(defaultScout);
        entityManager.persist(defaultScout);

        Scout otherScout = new Scout();
        otherScout.setFirstName("Mike");
        otherScout.setLastName("Bratz");
        otherScout.setBirthDate(LocalDate.of(1978, 3, 22));
        otherScout.setEmail("mike.bratz@celtics.com");
        otherScout.setHireDate(LocalDate.of(2018, 9, 1));
        otherScout.setDailyRate(BigDecimal.valueOf(280));
        otherScout.setEmployer(celtics);
        otherScout.setLicenseNumber("SCT-002");
        otherScout.setSpecialization("Forwards");
        otherScout.setObservationRegion("EuroLeague");
        otherScout.setSentByDirector(director);
        director.getSentScouts().add(otherScout);
        entityManager.persist(otherScout);
        entityManager.flush();

        Player playerA = createPlayer("LeBron", "James", "lebron@nba.com",
                LocalDate.of(1984, 12, 30), PositionType.SMALL_FORWARD,
                PlayerStatus.OBSERVED, 113.0, 206.0, 214.0, lakers);
        playerA.becomeProfessionalPlayer("USA");
        entityManager.persist(playerA);

        Player playerB = createPlayer("Luka", "Doncic", "luka@nba.com",
                LocalDate.of(1999, 2, 28), PositionType.POINT_GUARD,
                PlayerStatus.NEW, 104.0, 201.0, 208.0, realMadrid);
        playerB.becomeProfessionalPlayer("Slovenia");
        entityManager.persist(playerB);

        Player playerC = createPlayer("Paolo", "Banchero", "paolo@nba.com",
                LocalDate.of(2002, 11, 12), PositionType.POWER_FORWARD,
                PlayerStatus.OBSERVED, 113.0, 208.0, 213.0, duke);
        playerC.becomeUniversityPlayer("Duke University", ClassType.FRESHMAN);
        entityManager.persist(playerC);

        Player playerD = createPlayer("Victor", "Wembanyama", "victor@nba.com",
                LocalDate.of(2004, 1, 4), PositionType.CENTER,
                PlayerStatus.NEW, 95.0, 224.0, 244.0, realMadrid);
        playerD.becomeProfessionalPlayer("France");
        entityManager.persist(playerD);

        Player playerE = createPlayer("Cooper", "Flagg", "cooper@nba.com",
                LocalDate.of(2006, 12, 21), PositionType.SMALL_FORWARD,
                PlayerStatus.NEW, 93.0, 206.0, 213.0, duke);
        playerE.becomeUniversityPlayer("Duke University", ClassType.FRESHMAN);
        entityManager.persist(playerE);

        Player playerF = createPlayer("Jalen", "Johnson", "jalen@ncaa.com",
                LocalDate.of(2002, 12, 18), PositionType.POWER_FORWARD,
                PlayerStatus.OBSERVED, 102.0, 206.0, 216.0, duke);
        playerF.becomeUniversityPlayer("Georgia Tech", ClassType.JUNIOR);
        entityManager.persist(playerF);

        entityManager.flush();

        Delegation delegation1 = persistDelegation("NCAA West Tour", defaultScout, director,
                LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 10), "Los Angeles");
        Delegation delegation2 = persistDelegation("EuroLeague Trip", otherScout, director,
                LocalDate.of(2024, 11, 5), LocalDate.of(2024, 11, 12), "Madrid");
        Delegation delegation3 = persistDelegation("Mixed Scouting", defaultScout, director,
                LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 5), "Madrid");

        Match match1 = persistMatch(LocalDate.of(2024, 11, 3), "Staples Center",
                lakers, celtics, 110, 105, delegation1);
        Match match2 = persistMatch(LocalDate.of(2024, 11, 5), "Pauley Pavilion",
                duke, lakers, 88, 92, delegation1);
        Match match3 = persistMatch(LocalDate.of(2024, 11, 7), "WiZink Center",
                realMadrid, celtics, 95, 89, delegation2);
        Match match4 = persistMatch(LocalDate.of(2024, 12, 2), "WiZink Center",
                realMadrid, lakers, 101, 99, delegation3);
        Match match5 = persistMatch(LocalDate.of(2024, 11, 9), "WiZink Center",
                realMadrid, duke, 88, 82, delegation2);

        entityManager.persist(createMatchStats(playerA, match1, 36, 28, 8, 5, 2, 1, 3, 2, 10, 20, 3, 7, 5, 6));
        entityManager.persist(createMatchStats(playerA, match2, 34, 22, 10, 7, 1, 2, 4, 3, 8, 18, 2, 5, 4, 5));
        entityManager.persist(createMatchStats(playerB, match3, 38, 32, 9, 8, 1, 0, 5, 2, 12, 22, 4, 9, 4, 4));
        entityManager.persist(createMatchStats(playerC, match2, 30, 18, 7, 3, 0, 2, 2, 4, 7, 15, 1, 3, 3, 4));
        entityManager.persist(createMatchStats(playerD, match4, 32, 24, 12, 3, 2, 4, 2, 3, 9, 17, 2, 4, 4, 5));
        entityManager.persist(createMatchStats(playerD, match5, 28, 20, 10, 2, 1, 5, 3, 4, 8, 16, 1, 3, 3, 4));
        entityManager.persist(createMatchStats(playerF, match3, 31, 16, 9, 4, 1, 1, 2, 3, 6, 14, 1, 4, 2, 3));

        defaultScout.getWatchedMatches().addAll(List.of(match1, match2, match4));
        otherScout.getWatchedMatches().addAll(List.of(match3, match5));

        observePlayer(defaultScout, playerA);
        observePlayer(defaultScout, playerC);
        observePlayer(defaultScout, playerD);
        observePlayer(defaultScout, playerE);
        observePlayer(defaultScout, playerF);

        observePlayer(otherScout, playerB);

        seedReport(defaultScout, playerA, List.of(match1, match2),
                "Dominant two-way impact across observed games.",
                RecommendationType.BUY,
                List.of(
                        rating("offense", "Elite finishing and court vision.", 8, "0.50"),
                        rating("defense", "Active hands, solid rotations.", 7, "0.30"),
                        rating("athleticism", "Still explosive for his age.", 9, "0.20")
                ));

        seedReport(defaultScout, playerC, List.of(match2),
                "Promising freshman with high upside.",
                RecommendationType.STRONG_BUY,
                List.of(
                        rating("offense", "Smooth face-up game.", 9, "0.60"),
                        rating("character", "Coachable, high motor.", 8, "0.40")
                ));

        entityManager.flush();
    }

    /**
     * Creates and persists a {@link Club}.
     *
     * @return the managed club instance
     */
    private Club persistClub(String name, String league, String city, String country,
                             String conference, String division) {
        Club c = new Club();
        c.setName(name);
        c.setLeague(league);
        c.setCity(city);
        c.setCountry(country);
        c.setConference(conference);
        c.setDivision(division);
        entityManager.persist(c);
        return c;
    }

    /**
     * Builds a transient {@link Player} with identity fields; experience is set separately.
     */
    private Player createPlayer(String firstName, String lastName, String email,
                                LocalDate birthDate, PositionType position,
                                PlayerStatus status, Double weight, Double height,
                                Double wingspan, Club club) {
        Player p = new Player();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);
        p.setBirthDate(birthDate);
        p.setPosition(position);
        p.setPlayerStatus(status);
        p.setWeight(weight);
        p.setHeight(height);
        p.setWingspan(wingspan);
        p.setClub(club);
        return p;
    }

    /**
     * Creates a {@link Delegation} and wires both sides of director/scout associations.
     */
    private Delegation persistDelegation(String name, Scout scout, Director director,
                                         LocalDate start, LocalDate end, String destination) {
        Delegation d = new Delegation();
        d.setName(name);
        d.setScout(scout);
        d.setCreatedBy(director);
        d.setStartDate(start);
        d.setEndDate(end);
        d.setDestination(destination);
        d.setStatus(DelegationStatus.FINISHED);
        director.getCreatedDelegations().add(d);
        scout.getDelegations().add(d);
        entityManager.persist(d);
        return d;
    }

    /**
     * Persists a {@link Match} after {@link Match#validateBothTeams()}.
     */
    private Match persistMatch(LocalDate date, String place, Club host, Club guest,
                               int hostScore, int guestScore, Delegation delegation) {
        Match m = new Match();
        m.setDate(date);
        m.setPlace(place);
        m.setHost(host);
        m.setGuest(guest);
        m.setHostScore(hostScore);
        m.setGuestScore(guestScore);
        m.setDelegation(delegation);
        m.validateBothTeams();
        entityManager.persist(m);
        return m;
    }

    /**
     * Builds a {@link MatchStats} row linking a player to a match (not yet persisted).
     */
    private MatchStats createMatchStats(Player player, Match match,
                                        int minutes, int points, int rebounds, int assists,
                                        int steals, int blocks, int turnovers, int fouls,
                                        int fgMade, int fgAtt, int tpMade, int tpAtt,
                                        int ftMade, int ftAtt) {
        MatchStats ms = new MatchStats();
        ms.setPlayer(player);
        ms.setMatch(match);
        ms.setMinutesPlayed(minutes);
        ms.setPoints(points);
        ms.setRebounds(rebounds);
        ms.setAssists(assists);
        ms.setSteals(steals);
        ms.setBlocks(blocks);
        ms.setTurnovers(turnovers);
        ms.setFouls(fouls);
        ms.setFieldGoalsMade(fgMade);
        ms.setFieldGoalsAttempted(fgAtt);
        ms.setThreePointersMade(tpMade);
        ms.setThreePointersAttempted(tpAtt);
        ms.setFreeThrowsMade(ftMade);
        ms.setFreeThrowsAttempted(ftAtt);
        return ms;
    }

    /**
     * Maintains both sides of the scout ↔ player {@code observedPlayers} association.
     */
    private void observePlayer(Scout scout, Player player) {
        scout.getObservedPlayers().add(player);
        player.getObservingScouts().add(scout);
    }

    /** Factory for a single {@link DetailedRating} with decimal weight. */
    private DetailedRating rating(String type, String comment, int score, String weight) {
        DetailedRating dr = new DetailedRating();
        dr.setType(type);
        dr.setComment(comment);
        dr.setRating(BigDecimal.valueOf(score));
        dr.setWeight(new BigDecimal(weight));
        return dr;
    }

    /**
     * Persists a sample {@link ScoutingReport} without running service-layer validation
     * (seed data is known to satisfy all invariants).
     */
    private void seedReport(Scout scout, Player player, List<Match> matches,
                            String note, RecommendationType recommendation,
                            List<DetailedRating> ratings) {
        ScoutingReport report = new ScoutingReport();
        report.setCreatedAt(LocalDate.of(2024, 11, 6));
        report.setNote(note);
        report.setRecommendation(recommendation);
        report.setCreatedBy(scout);
        report.setPlayer(player);
        report.setBasedOnMatches(matches);
        for (DetailedRating dr : ratings) {
            dr.setScoutingReport(report);
        }
        report.setDetailedRatings(ratings);
        entityManager.persist(report);
    }
}
