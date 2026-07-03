package com.infratrack.organization.policy.reporting;

import com.infratrack.reporting.ReportingExportFormat;

import java.util.List;

/**
 * Default reporting policy matching the original fixed InfraTrack behaviour.
 */
public class DefaultReportingPolicy implements ReportingPolicy {

    private static final List<ReportingExportFormat> ENABLED_FORMATS = List.of(
            ReportingExportFormat.CSV,
            ReportingExportFormat.XLSX,
            ReportingExportFormat.PDF);

    @Override
    public ReportingExportFormat defaultExportFormat() {
        return ReportingExportFormat.CSV;
    }

    @Override
    public List<ReportingExportFormat> enabledExportFormats() {
        return ENABLED_FORMATS;
    }

    @Override
    public ReportingDateRange defaultReportingDateRange() {
        return ReportingDateRange.LAST_30_DAYS;
    }
}
