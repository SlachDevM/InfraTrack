package com.infratrack.unitofmeasure;

import com.infratrack.unitofmeasure.dto.UnitOfMeasureResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnitOfMeasureService {

    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public UnitOfMeasureService(UnitOfMeasureRepository unitOfMeasureRepository) {
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Transactional(readOnly = true)
    public List<UnitOfMeasureResponse> list(Boolean activeOnly, QuantityType quantityType) {
        List<UnitOfMeasure> units;
        if (Boolean.TRUE.equals(activeOnly)) {
            units = quantityType == null
                    ? unitOfMeasureRepository.findByActiveTrueOrderByNameAsc()
                    : unitOfMeasureRepository.findByQuantityTypeAndActiveTrueOrderByNameAsc(quantityType);
        } else if (quantityType != null) {
            units = unitOfMeasureRepository.findAllByOrderByNameAsc().stream()
                    .filter(unit -> unit.getQuantityType() == quantityType)
                    .toList();
        } else {
            units = unitOfMeasureRepository.findAllByOrderByNameAsc();
        }
        return units.stream().map(UnitOfMeasureResponse::from).toList();
    }
}
