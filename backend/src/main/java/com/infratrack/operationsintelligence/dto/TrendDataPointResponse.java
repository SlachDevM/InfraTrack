package com.infratrack.operationsintelligence.dto;

public class TrendDataPointResponse {

    private String period;
    private long count;

    public TrendDataPointResponse() {
    }

    public TrendDataPointResponse(String period, long count) {
        this.period = period;
        this.count = count;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
