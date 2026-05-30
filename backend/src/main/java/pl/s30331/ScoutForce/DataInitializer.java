package pl.s30331.ScoutForce;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.*;
import pl.s30331.ScoutForce.model.enums.*;
import pl.s30331.ScoutForce.repository.PlayerRepository;
import pl.s30331.ScoutForce.repository.ScoutRepository;
import pl.s30331.ScoutForce.repository.ScoutingReportRepository;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Fallback data initializer – seeds the database with test data
 * if it is empty (no scouts exist). If the H2 file already contains
 * data from a previous run, this does nothing.
 *
 * Test scenario coverage:
 *  - Default scout (ID used as temporary default in the controller)
 *  - Player A: has matches observed by default scout → happy path
 *  - Player B: has matches, but NONE observed by default scout → E1 error
 *  - Player C: has matches observed by default scout → happy path (alternative)
 *  - Player D: has a mix – some matches observed by default scout, one NOT → tests filtering
 *  - Player E: no matches at all → E1 error
 *  - Second scout (not default) – owns some matches to test isolation
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ScoutRepository scoutRepository;
    private final PlayerRepository playerRepository;
    private final ScoutingReportRepository scoutingReportRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        // Fallback: only seed if database is empty
        if (scoutRepository.count() > 0) {
            return;
        }

        // ═══════════════════════════════════════════════════════════════════
        // CLUBS
        // ═══════════════════════════════════════════════════════════════════
        Club lakers = createClub("Los Angeles Lakers", "NBA", "Los Angeles", "USA", "Western", "Pacific");
        Club celtics = createClub("Boston Celtics", "NBA", "Boston", "USA", "Eastern", "Atlantic");
        Club duke = createClub("Duke Blue Devils", "NCAA", "Durham", "USA", null, null);
        Club realMadrid = createClub("Real Madrid Baloncesto", "EuroLeague", "Madrid", "Spain", null, null);

        entityManager.persist(lakers);
        entityManager.persist(celtics);
        entityManager.persist(duke);
        entityManager.persist(realMadrid);
        entityManager.flush();

        // ═══════════════════════════════════════════════════════════════════
        // DIRECTOR (needed for delegations)
        // ═══════════════════════════════════════════════════════════════════
        Director director = new Director();
        director.setFirstName("Rob");
        director.setLastName("Pelinka");
        director.setBirthDate(LocalDate.of(1969, 12, 23));
        director.setEmail("rob.pelinka@lakers.com");
        director.setHireDate(LocalDate.of(2017, 3, 10));
        director.setDailyRate(BigDecimal.valueOf(500));
        director.setEmployer(lakers);
        entityManager.persist(director);

        // ═══════════════════════════════════════════════════════════════════
        // SCOUTS
        // ═══════════════════════════════════════════════════════════════════

        // Default scout – the one we use for demo (ID will be set as default in controller)
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
        entityManager.persist(defaultScout);

        // Second scout – owns matches that default scout does NOT see
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
        entityManager.persist(otherScout);
        entityManager.flush();

        // ═══════════════════════════════════════════════════════════════════
        // PLAYERS
        // ═══════════════════════════════════════════════════════════════════

        // Player A – has matches observed by default scout (happy path)
        Player playerA = createPlayer("LeBron", "James", "lebron@nba.com",
                LocalDate.of(1984, 12, 30), PositionType.SMALL_FORWARD,
                PlayerStatus.OBSERVED, 113.0, 206.0, 214.0, lakers);
        ProfessionalExperience peA = new ProfessionalExperience("USA");
        peA.setPlayer(playerA);
        playerA.setProfessionalExperience(peA);
        entityManager.persist(playerA);

        // Player B – has matches, but NONE observed by default scout → E1
        Player playerB = createPlayer("Luka", "Doncic", "luka@nba.com",
                LocalDate.of(1999, 2, 28), PositionType.POINT_GUARD,
                PlayerStatus.NEW, 104.0, 201.0, 208.0, realMadrid);
        ProfessionalExperience peB = new ProfessionalExperience("Slovenia");
        peB.setPlayer(playerB);
        playerB.setProfessionalExperience(peB);
        entityManager.persist(playerB);

        // Player C – has matches observed by default scout (alternative happy path)
        Player playerC = createPlayer("Paolo", "Banchero", "paolo@nba.com",
                LocalDate.of(2002, 11, 12), PositionType.POWER_FORWARD,
                PlayerStatus.OBSERVED, 113.0, 208.0, 213.0, duke);
        UniversityExperience ueC = new UniversityExperience("Duke University", ClassType.FRESHMAN);
        ueC.setPlayer(playerC);
        playerC.setUniversityExperience(ueC);
        entityManager.persist(playerC);

        // Player D – mix: some matches observed by default scout, one NOT
        Player playerD = createPlayer("Victor", "Wembanyama", "victor@nba.com",
                LocalDate.of(2004, 1, 4), PositionType.CENTER,
                PlayerStatus.NEW, 95.0, 224.0, 244.0, realMadrid);
        ProfessionalExperience peD = new ProfessionalExperience("France");
        peD.setPlayer(playerD);
        playerD.setProfessionalExperience(peD);
        entityManager.persist(playerD);

        // Player E – no matches at all → E1
        Player playerE = createPlayer("Cooper", "Flagg", "cooper@nba.com",
                LocalDate.of(2006, 12, 21), PositionType.SMALL_FORWARD,
                PlayerStatus.NEW, 93.0, 206.0, 213.0, duke);
        UniversityExperience ueE = new UniversityExperience("Duke University", ClassType.FRESHMAN);
        ueE.setPlayer(playerE);
        playerE.setUniversityExperience(ueE);
        entityManager.persist(playerE);

        entityManager.flush();

        // ═══════════════════════════════════════════════════════════════════
        // DELEGATIONS
        // ═══════════════════════════════════════════════════════════════════
        Delegation delegation1 = createDelegation("NCAA West Tour", defaultScout, director,
                LocalDate.of(2024, 11, 1), LocalDate.of(2024, 11, 10), "Los Angeles");
        entityManager.persist(delegation1);

        Delegation delegation2 = createDelegation("EuroLeague Trip", otherScout, director,
                LocalDate.of(2024, 11, 5), LocalDate.of(2024, 11, 12), "Madrid");
        entityManager.persist(delegation2);

        Delegation delegation3 = createDelegation("Mixed Scouting", defaultScout, director,
                LocalDate.of(2024, 12, 1), LocalDate.of(2024, 12, 5), "Madrid");
        entityManager.persist(delegation3);

        entityManager.flush();

        // ═══════════════════════════════════════════════════════════════════
        // MATCHES
        // ═══════════════════════════════════════════════════════════════════

        // Match 1 – observed by DEFAULT scout (delegation1), playerA plays
        Match match1 = createMatch(LocalDate.of(2024, 11, 3), "Staples Center",
                lakers, celtics, 110, 105, delegation1);
        entityManager.persist(match1);

        // Match 2 – observed by DEFAULT scout (delegation1), playerA and playerC play
        Match match2 = createMatch(LocalDate.of(2024, 11, 5), "Pauley Pavilion",
                duke, lakers, 88, 92, delegation1);
        entityManager.persist(match2);

        // Match 3 – observed by OTHER scout only (delegation2), playerB plays
        Match match3 = createMatch(LocalDate.of(2024, 11, 7), "WiZink Center",
                realMadrid, celtics, 95, 89, delegation2);
        entityManager.persist(match3);

        // Match 4 – observed by DEFAULT scout (delegation3), playerD plays
        Match match4 = createMatch(LocalDate.of(2024, 12, 2), "WiZink Center",
                realMadrid, lakers, 101, 99, delegation3);
        entityManager.persist(match4);

        // Match 5 – observed by OTHER scout only (delegation2), playerD plays
        //           → this match should NOT appear for default scout's view of playerD
        Match match5 = createMatch(LocalDate.of(2024, 11, 9), "WiZink Center",
                realMadrid, duke, 88, 82, delegation2);
        entityManager.persist(match5);

        entityManager.flush();

        // ═══════════════════════════════════════════════════════════════════
        // MATCH STATS (player appearances in matches)
        // ═══════════════════════════════════════════════════════════════════

        // Player A in match1 and match2 (both observed by default scout)
        entityManager.persist(createMatchStats(playerA, match1, 36, 28, 8, 5, 2, 1, 3, 2, 10, 20, 3, 7, 5, 6));
        entityManager.persist(createMatchStats(playerA, match2, 34, 22, 10, 7, 1, 2, 4, 3, 8, 18, 2, 5, 4, 5));

        // Player B in match3 (observed by OTHER scout only → E1 for default scout)
        entityManager.persist(createMatchStats(playerB, match3, 38, 32, 9, 8, 1, 0, 5, 2, 12, 22, 4, 9, 4, 4));

        // Player C in match2 (observed by default scout)
        entityManager.persist(createMatchStats(playerC, match2, 30, 18, 7, 3, 0, 2, 2, 4, 7, 15, 1, 3, 3, 4));

        // Player D in match4 (observed by default scout) AND match5 (observed by other scout)
        entityManager.persist(createMatchStats(playerD, match4, 32, 24, 12, 3, 2, 4, 2, 3, 9, 17, 2, 4, 4, 5));
        entityManager.persist(createMatchStats(playerD, match5, 28, 20, 10, 2, 1, 5, 3, 4, 8, 16, 1, 3, 3, 4));

        // Player E – no match stats at all (E1)

        entityManager.flush();

        // ═══════════════════════════════════════════════════════════════════
        // SCOUT ↔ MATCH (watchedMatches)
        // ═══════════════════════════════════════════════════════════════════

        // Default scout watched: match1, match2, match4
        defaultScout.getWatchedMatches().addAll(List.of(match1, match2, match4));

        // Other scout watched: match3, match5
        otherScout.getWatchedMatches().addAll(List.of(match3, match5));

        entityManager.flush();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Builder helpers
    // ═══════════════════════════════════════════════════════════════════════

    private Club createClub(String name, String league, String city, String country,
                            String conference, String division) {
        Club c = new Club();
        c.setName(name);
        c.setLeague(league);
        c.setCity(city);
        c.setCountry(country);
        c.setConference(conference);
        c.setDivision(division);
        return c;
    }

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

    private Delegation createDelegation(String name, Scout scout, Director director,
                                        LocalDate start, LocalDate end, String destination) {
        Delegation d = new Delegation();
        d.setName(name);
        d.setScout(scout);
        d.setCreatedBy(director);
        d.setStartDate(start);
        d.setEndDate(end);
        d.setDestination(destination);
        d.setStatus(DelegationStatus.FINISHED);
        return d;
    }

    private Match createMatch(LocalDate date, String place, Club host, Club guest,
                              int hostScore, int guestScore, Delegation delegation) {
        Match m = new Match();
        m.setDate(date);
        m.setPlace(place);
        m.setHost(host);
        m.setGuest(guest);
        m.setHostScore(hostScore);
        m.setGuestScore(guestScore);
        m.setDelegation(delegation);
        return m;
    }

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
}
