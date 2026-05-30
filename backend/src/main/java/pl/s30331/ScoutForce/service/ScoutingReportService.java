package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.*;
import pl.s30331.ScoutForce.model.enums.RecommendationType;
import pl.s30331.ScoutForce.repository.PlayerRepository;
import pl.s30331.ScoutForce.repository.ScoutRepository;
import pl.s30331.ScoutForce.repository.ScoutingReportRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════
 *  PRIMARY USE CASE: Create Scouting Report
 *  Includes:  <<include>> View Players List  (ViewPlayersListService)
 *             <<include>> Add Detailed Ratings  (inline, client sends them)
 *  Extends:   <<extend>>  View Player's Matches (ViewPlayerMatchesService)
 * ═══════════════════════════════════════════════════════════════
 *
 * Validation order (fail fast before touching the database):
 *   1.  Pre-input validation (no DB hit yet)
 *       a. note / recommendation present                                      → E5
 *       b. ratings list non-empty                                             → E2
 *       c. each DetailedRating: type/comment non-blank, rating∈[1,10],
 *          weight∈[0.0, 1.0]                                                  → E4
 *
 *   2.  Aggregate fetch (DB hit) – Scout, Player
 *
 *   3.  Domain-scope validation (uses fetched aggregates)
 *       d. determine basedOnMatches; non-empty                                → E1
 *
 *   4.  Aggregate-level invariants on the fully-built ScoutingReport
 *       e. sum of weights == 1.0                                              → E3
 *       f. {subset} basedOnMatches ⊆ scout.watchedMatches  (defense in depth)
 *
 *   5.  Persist – cascade saves DetailedRatings as part of the composition.
 *
 * Transaction:
 *   The class is annotated {@code @Transactional} – every mutating method
 *   runs inside a single transaction. Any RuntimeException (incl. all the
 *   validation exceptions) triggers a full rollback, so a partially-built
 *   report never makes it to the database (NFR 5: atomicity).
 *   {@code rollbackFor = Exception.class} on mutating methods is set
 *   explicitly for self-documentation.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ScoutingReportService {

    private final ScoutingReportRepository scoutingReportRepository;
    private final ScoutRepository          scoutRepository;
    private final PlayerRepository         playerRepository;
    private final ViewPlayerMatchesService viewPlayerMatchesService;

    // ────────────────────────────────────────────────────────────────────────
    // PRIMARY USE CASE – createScoutingReport (default flow)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Creates and persists a new Scouting Report (default flow – all observed matches).
     *
     * @param scoutId        ID of the scout creating the report
     * @param playerId       ID of the player being scouted
     * @param note           free-text observation note
     * @param recommendation draft recommendation
     * @param ratings        list of DetailedRating objects built from the frontend payload
     * @return the persisted ScoutingReport with its generated id
     */
    @Transactional(rollbackFor = Exception.class)
    public ScoutingReport createScoutingReport(Long scoutId,
                                               Long playerId,
                                               String note,
                                               RecommendationType recommendation,
                                               List<DetailedRating> ratings) {

        // 1. Pre-input validation – fail fast (no DB hit yet)
        validateInput(note, recommendation, ratings);

        // 2. Fetch aggregates
        Scout  scout  = loadScout(scoutId);
        Player player = loadPlayer(playerId);

        // 3. Determine scope via association navigation – all observed matches
        //    of the player that the scout has actually watched
        List<Match> basedOnMatches = viewPlayerMatchesService
                .getObservedMatchesForPlayer(player, scout);

        if (basedOnMatches.isEmpty()) {
            throw new IllegalStateException("Chosen player has no matches you've observed.");
        }

        // 4. Build, validate aggregate invariants, persist
        ScoutingReport report = buildReport(scout, player, basedOnMatches,
                                            note, recommendation, ratings);
        return persistValidated(report);
    }

    // ────────────────────────────────────────────────────────────────────────
    // ALTERNATIVE FLOW A1 – createScoutingReportFromSelectedMatches
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Creates a Scouting Report scoped to a specific subset of matches
     * (alternative flow A1 – <<extend>> View Player's Matches).
     */
    @Transactional(rollbackFor = Exception.class)
    public ScoutingReport createScoutingReportFromSelectedMatches(
            Long scoutId,
            Long playerId,
            List<Long> selectedMatchIds,
            String note,
            RecommendationType recommendation,
            List<DetailedRating> ratings) {

        // 1. Pre-input validation – fail fast
        validateInput(note, recommendation, ratings);
        if (selectedMatchIds == null || selectedMatchIds.isEmpty()) {
            throw new IllegalStateException("Chosen player has no matches you've observed.");
        }

        // 2. Fetch aggregates
        Scout  scout  = loadScout(scoutId);
        Player player = loadPlayer(playerId);

        // 3. Resolve selected matches – strict validation: every requested ID
        //    must be in scout.watchedMatches, otherwise reject the request.
        List<Long> watchedIds = scout.getWatchedMatches().stream()
                .map(Match::getId)
                .collect(Collectors.toList());

        List<Long> unobserved = selectedMatchIds.stream()
                .filter(id -> !watchedIds.contains(id))
                .collect(Collectors.toList());

        if (!unobserved.isEmpty()) {
            throw new IllegalStateException(
                    "Selected matches contain IDs not observed by this scout: " + unobserved);
        }

        List<Match> selectedMatches = scout.getWatchedMatches().stream()
                .filter(m -> selectedMatchIds.contains(m.getId()))
                .collect(Collectors.toList());

        if (selectedMatches.isEmpty()) {
            throw new IllegalStateException("Chosen player has no matches you've observed.");
        }

        // 4. Build, validate aggregate invariants, persist
        ScoutingReport report = buildReport(scout, player, selectedMatches,
                                            note, recommendation, ratings);
        return persistValidated(report);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Private helpers – shared between both flows
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Stage 1 – pre-input validation. Pure in-memory checks, no DB hit.
     * Fails fast on E5 (note/recommendation), E2 (no ratings), E4 (ranges).
     */
    private void validateInput(String note,
                               RecommendationType recommendation,
                               List<DetailedRating> ratings) {
        // E5 – brak rekomendacji lub notki
        if (note == null || note.isBlank() || recommendation == null) {
            throw new IllegalStateException("Notes and recommendation cannot be empty.");
        }
        // E2 – brak ocen szczegółowych
        if (ratings == null || ratings.isEmpty()) {
            throw new IllegalStateException("Report needs at least one Detailed Rating.");
        }
        // E4 – walidacja zakresów każdej oceny szczegółowej
        for (DetailedRating dr : ratings) {
            dr.validateRanges();
        }
    }

    private Scout loadScout(Long scoutId) {
        return scoutRepository.findById(scoutId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Scout not found: " + scoutId));
    }

    private Player loadPlayer(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Player not found: " + playerId));
    }

    /**
     * Builds a fully-wired ScoutingReport aggregate (composition with
     * DetailedRatings, association with basedOnMatches). Does not persist.
     */
    private ScoutingReport buildReport(Scout scout,
                                       Player player,
                                       List<Match> basedOnMatches,
                                       String note,
                                       RecommendationType recommendation,
                                       List<DetailedRating> ratings) {
        ScoutingReport report = new ScoutingReport();
        report.setCreatedAt(LocalDate.now());
        report.setNote(note);
        report.setRecommendation(recommendation);
        report.setCreatedBy(scout);
        report.setPlayer(player);
        report.setBasedOnMatches(basedOnMatches);

        // composition – set the owning side on every child
        for (DetailedRating dr : ratings) {
            dr.setScoutingReport(report);
        }
        report.setDetailedRatings(ratings);
        return report;
    }

    /**
     * Stage 4 + 5 – aggregate-level invariants and persistence.
     * Runs the domain methods declared on {@link ScoutingReport}, then saves.
     */
    private ScoutingReport persistValidated(ScoutingReport report) {
        report.validateDetailedRatings();        // E3 – sum of weights = 1.0
        report.validateMatchesObservedByScout(); // {subset} – defense in depth
        return scoutingReportRepository.save(report);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Other use cases – STUB (not implemented)
    // ────────────────────────────────────────────────────────────────────────

    /** Retrieves all reports authored by a scout (stub). */
    @Transactional(readOnly = true)
    public List<ScoutingReport> getReportsByScout(Long scoutId) {
        // TODO: implement when needed
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /** Retrieves all reports for a player (stub). */
    @Transactional(readOnly = true)
    public List<ScoutingReport> getReportsByPlayer(Long playerId) {
        // TODO: implement when needed
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    /** Deletes a scouting report (stub). */
    public void deleteReport(Long reportId) {
        // TODO: implement when needed
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}
