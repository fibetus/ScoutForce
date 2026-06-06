package pl.s30331.ScoutForce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.s30331.ScoutForce.model.*;
import pl.s30331.ScoutForce.model.enums.RecommendationType;
import pl.s30331.ScoutForce.repository.ScoutingReportRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Primary use case: Create Scouting Report.
 *
 * <p>Includes {@code View Players List} and {@code Add Detailed Ratings};
 * extends {@code View Player's Matches} (flow A1).</p>
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
     * Creates and persists a new Scouting Report (default flow – all observed matches).
     */
    @Transactional(rollbackFor = Exception.class)
    public ScoutingReport createScoutingReport(Long scoutId,
                                               Long playerId,
                                               String note,
                                               RecommendationType recommendation,
                                               List<DetailedRating> ratings) {
        validateInput(note, recommendation, ratings);

        Scout  scout  = scoutService.getScout(scoutId);
        Player player = playerService.getPlayer(playerId);

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
     * Creates a Scouting Report scoped to a specific subset of matches (flow A1).
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
            throw new IllegalStateException("Chosen player has no matches you've observed.");
        }

        Scout  scout  = scoutService.getScout(scoutId);
        Player player = playerService.getPlayer(playerId);

        List<Match> playerObserved = viewPlayerMatchesService
                .getObservedMatchesForPlayer(player, scout);
        Set<Long> playerObservedIds = playerObserved.stream()
                .map(Match::getId)
                .collect(Collectors.toSet());

        List<Long> invalid = selectedMatchIds.stream()
                .filter(id -> !playerObservedIds.contains(id))
                .toList();

        if (!invalid.isEmpty()) {
            throw new IllegalStateException("Chosen player has no matches you've observed.");
        }

        List<Match> selectedMatches = playerObserved.stream()
                .filter(m -> selectedMatchIds.contains(m.getId()))
                .toList();

        if (selectedMatches.isEmpty()) {
            throw new IllegalStateException("Chosen player has no matches you've observed.");
        }

        ScoutingReport report = buildReport(scout, player, selectedMatches,
                                            note, recommendation, ratings);
        return persistValidated(report);
    }

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

        for (DetailedRating dr : ratings) {
            dr.setScoutingReport(report);
        }
        report.setDetailedRatings(ratings);
        return report;
    }

    private ScoutingReport persistValidated(ScoutingReport report) {
        report.validateDetailedRatings();
        report.validateMatchesObservedByScout();
        report.validateMatchesPlayedByPlayer();
        return scoutingReportRepository.save(report);
    }
}
