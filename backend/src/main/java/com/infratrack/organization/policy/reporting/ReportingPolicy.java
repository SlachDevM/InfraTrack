package com.infratrack.organization.policy.reporting;

import com.infratrack.reporting.ReportingExportFormat;

import java.util.List;

/**
 * Organizational reporting defaults (BDR-004).
 *
 * <p>No user reporting preferences exist yet. Future user preferences will override these defaults.
 */
public interface ReportingPolicy {

    ReportingExportFormat defaultExportFormat();

    List<ReportingExportFormat> enabledExportFormats();

    ReportingDateRange defaultReportingDateRange();
}
