package com.infratrack.operationsintelligence.dto;

import java.util.List;

public class RecentActivityResponse {

    private List<RecentActivityItemResponse> items;

    public List<RecentActivityItemResponse> getItems() {
        return items;
    }

    public void setItems(List<RecentActivityItemResponse> items) {
        this.items = items;
    }
}
