package com.infratrack.operationsintelligence.dto;

public class OperationsTrendResponse {

    private long from;
    private long to;
    private String bucket;
    private TrendScopeResponse scope;
    private TrendSeriesResponse series;

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public TrendScopeResponse getScope() {
        return scope;
    }

    public void setScope(TrendScopeResponse scope) {
        this.scope = scope;
    }

    public TrendSeriesResponse getSeries() {
        return series;
    }

    public void setSeries(TrendSeriesResponse series) {
        this.series = series;
    }
}
