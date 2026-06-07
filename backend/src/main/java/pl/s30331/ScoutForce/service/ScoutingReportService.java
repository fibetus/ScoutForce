package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.*;
import pl.s30331.ScoutForce.model.enums.RecommendationType;
import pl.s30331.ScoutForce.repository.ScoutingReportRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Primary use case: <strong>Create Scouting Report</strong>.
 *
 * <p>Includes {@code View Players List} and {@code Add Detailed Ratings};
 * extends {@code View Player's Matches} (alternative flow A1 with a match subset).</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ScoutingReportService {

    private final ScoutingReportRepository scoutingReportRepository;
    private final ScoutService             scoutService;
    private final PlayerService            playerService;
    private final ViewPlayerMatchesService viewPlayerMatchesService;

    /**
     * Creates a report based on <em>all</em> matches observed by the scout in which the player appeared.
     *
     * @param scoutId        authoring scout
     * @param playerId       subject player
     * @param note           free-text evaluation (required)
     * @param recommendation buy/hold/pass recommendation
     * @param ratings        at least one weighted {@link DetailedRating}
     * @return persisted report after domain validation
     * @throws IllegalStateException E1 when the player has no observed matches in common with the scout
     */
    @Transactional(rollbackFor = Exception.class)
    public ScoutingReport createScoutingReport(Long scoutId,
                                               Long playerId,
                                               String note,
                                               RecommendationType recommendation,
                                               List<DetailedRating> ratings) {
        validateInput(note, recommendation, ratings);

        Scout  scout  = scoutService.getScoutById(scoutId);
        Player player = playerService.getPlayerById(playerId);

        List<Match> basedOnMatches = viewPlayerMatchesService
                .getObservedMatchesForPlayer(player, scout);

        if (basedOnMatches.isEmpty()) {
            throw new IllegalStateException("Chosen player has no matches you've observed.");
        }

        ScoutingReport report = buildReport(scout, player, basedOnMatches,
                                            note, recommendation, ratings);
        return persistValidated(report);
    }

    /**
     * Creates a report scoped to an explicit subset of match ids (flow A1).
     *
     * <p>Each selected id must belong to the intersection of the scout's watched matches
     * and the player's {@link MatchStats} entries.</p>
     *
     * @param scoutId           authoring scout
     * @param playerId          subject player
     * @param selectedMatchIds  non-empty list of match primary keys
     * @param note              free-text evaluation
     * @param recommendation    buy/hold/pass recommendation
     * @param ratings           weighted detailed ratings
     * @return persisted report
     * @throws IllegalStateException when any selected match is outside the allowed intersection
     */
    @Transactional(rollbackFor = Exception.class)
    public ScoutingReport createScoutingReportFromSelectedMatches(
            Long scoutId,
            Long playerId,
            List<Long> selectedMatchIds,
            String note,
            RecommendationType recommendation,
            List<DetailedRating> ratings) {

        validateInput(note, recommendation, ratings);
        if (selectedMatchIds == null || selectedMatchIds.isEmpty()) {
            throw new IllegalStateException("At least one match must be selected for the report.");
        }

        Scout  scout  = scoutService.getScoutById(scoutId);
        Player player = playerService.getPlayerById(playerId);

        List<Match> playerObserved = viewPlayerMatchesService
                .getObservedMatchesForPlayer(player, scout);
        Set<Long> playerObservedIds = playerObserved.stream()
                .map(Match::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Long> invalid = selectedMatchIds.stream()
                .filter(id -> !playerObservedIds.contains(id))
                .toList();

        if (!invalid.isEmpty()) {
            throw new IllegalStateException(
                    "One or more selected matches are not among the matches you have observed for this player.");
        }

        List<Match> selectedMatches = playerObserved.stream()
                .filter(m -> m.getId() != null && selectedMatchIds.contains(m.getId()))
                .toList();

        if (selectedMatches.isEmpty()) {
            throw new IllegalStateException(
                    "One or more selected matches are not among the matches you have observed for this player.");
        }

        ScoutingReport report = buildReport(scout, player, selectedMatches,
                                            note, recommendation, ratings);
        return persistValidated(report);
    }

    /**
     * Validates request-level fields shared by both create flows.
     *
     * @param note           must be non-blank
     * @param recommendation must be present
     * @param ratings        must contain at least one entry with valid ranges
     * @throws IllegalStateException when any check fails
     */
    private void validateInput(String note,
                               RecommendationType recommendation,
                               List<DetailedRating> ratings) {
        if (note == null || note.isBlank() || recommendation == null) {
            throw new IllegalStateException("Notes and recommendation cannot be empty.");
        }
        if (ratings == null || ratings.isEmpty()) {
            throw new IllegalStateException("Report needs at least one Detailed Rating.");
        }
        for (DetailedRating dr : ratings) {
            dr.validateRanges();
        }
    }

    /**
     * Assembles an unsaved {@link ScoutingReport} graph in memory.
     *
     * @param scout           authoring scout
     * @param player          subject player
     * @param basedOnMatches  matches that ground the evaluation
     * @param note            report text
     * @param recommendation  recommendation enum
     * @param ratings         detailed ratings (linked to the report)
     * @return transient report ready for validation and persist
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
        report.setBasedOnMatches(new ArrayList<>(basedOnMatches));

        for (DetailedRating dr : ratings) {
            dr.setScoutingReport(report);
        }
        report.setDetailedRatings(ratings);
        return report;
    }

    /**
     * Runs all {@link ScoutingReport} invariants and saves the entity.
     *
     * @param report fully wired report graph
     * @return managed instance returned by the repository
     */
    private ScoutingReport persistValidated(ScoutingReport report) {
        report.validateDetailedRatings();
        report.validateMatchesObservedByScout();
        report.validateMatchesPlayedByPlayer();
        return scoutingReportRepository.save(report);
    }
}
