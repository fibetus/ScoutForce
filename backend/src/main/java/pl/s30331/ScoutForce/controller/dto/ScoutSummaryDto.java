package pl.s30331.ScoutForce.controller.dto;

/**
 * Minimal scout projection for frontend bootstrap.
 *
 * @param id            scout primary key
 * @param firstName     first name
 * @param lastName      last name
 * @param licenseNumber unique license
 */
public record ScoutSummaryDto(Long id, String firstName, String lastName, String licenseNumber) {}
