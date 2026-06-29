package com.infratrack.unitofmeasure;

import com.infratrack.unitofmeasure.dto.UnitOfMeasureResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnitOfMeasureServiceTest {

    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @InjectMocks
    private UnitOfMeasureService unitOfMeasureService;

    @Test
    void list_shouldReturnActiveUnits() {
        when(unitOfMeasureRepository.findByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(celsius(), fahrenheit()));

        List<UnitOfMeasureResponse> responses = unitOfMeasureService.list(true, null);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCode()).isEqualTo("CELSIUS");
        assertThat(responses.get(1).getCode()).isEqualTo("FAHRENHEIT");
    }

    @Test
    void list_shouldFilterByQuantityType() {
        when(unitOfMeasureRepository.findByQuantityTypeAndActiveTrueOrderByNameAsc(QuantityType.TEMPERATURE))
                .thenReturn(List.of(celsius(), fahrenheit()));

        List<UnitOfMeasureResponse> responses = unitOfMeasureService.list(true, QuantityType.TEMPERATURE);

        assertThat(responses).extracting(UnitOfMeasureResponse::getQuantityType)
                .containsOnly(QuantityType.TEMPERATURE);
    }

    @Test
    void list_shouldReturnAllUnitsWhenActiveFilterNotRequested() {
        UnitOfMeasure inactive = celsius();
        inactive.setId(99L);
        when(unitOfMeasureRepository.findAllByOrderByNameAsc()).thenReturn(List.of(celsius(), inactive));

        List<UnitOfMeasureResponse> responses = unitOfMeasureService.list(false, null);

        assertThat(responses).hasSize(2);
    }

    private UnitOfMeasure celsius() {
        UnitOfMeasure unit = new UnitOfMeasure("CELSIUS", "°C", "Celsius", QuantityType.TEMPERATURE);
        unit.setId(1L);
        return unit;
    }

    private UnitOfMeasure fahrenheit() {
        UnitOfMeasure unit = new UnitOfMeasure("FAHRENHEIT", "°F", "Fahrenheit", QuantityType.TEMPERATURE);
        unit.setId(2L);
        return unit;
    }
}
