package com.infratrack.unitofmeasure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, Long> {

    List<UnitOfMeasure> findByActiveTrueOrderByNameAsc();

    List<UnitOfMeasure> findByQuantityTypeAndActiveTrueOrderByNameAsc(QuantityType quantityType);

    List<UnitOfMeasure> findAllByOrderByNameAsc();

    Optional<UnitOfMeasure> findByIdAndActiveTrue(Long id);
}
