package com.infratrack.maintenanceactivity;

import com.infratrack.maintenanceactivity.dto.MaintenanceActivityResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-activities")
@CrossOrigin(origins = "http://localhost:3000")
public class MaintenanceActivityListController {

    private final MaintenanceActivityService maintenanceActivityService;

    public MaintenanceActivityListController(MaintenanceActivityService maintenanceActivityService) {
        this.maintenanceActivityService = maintenanceActivityService;
    }

    @GetMapping
    public List<MaintenanceActivityResponse> listMaintenanceActivities() {
        return maintenanceActivityService.listAll();
    }
}
