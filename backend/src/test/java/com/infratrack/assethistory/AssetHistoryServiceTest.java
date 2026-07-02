package com.infratrack.assethistory;



import com.infratrack.asset.Asset;

import com.infratrack.asset.AssetAuthorizationService;

import com.infratrack.asset.AssetHistoryEvent;

import com.infratrack.asset.AssetHistoryEventRepository;

import com.infratrack.asset.AssetHistoryEventType;

import com.infratrack.asset.AssetRepository;

import com.infratrack.asset.AssetStatus;

import com.infratrack.assetcategory.AssetCategory;

import com.infratrack.department.Department;

import com.infratrack.exception.ForbiddenOperationException;

import com.infratrack.user.User;

import com.infratrack.user.UserRepository;

import com.infratrack.user.UserRole;

import com.infratrack.user.UserService;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.PageImpl;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;

import org.springframework.web.server.ResponseStatusException;



import java.time.LocalDate;

import java.util.ArrayList;

import java.util.List;

import java.util.Optional;

import java.util.Set;



import static org.assertj.core.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)

class AssetHistoryServiceTest {



    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

    private static final Long AUTHORIZED_USER_ID = 10L;



    @Mock

    private AssetRepository assetRepository;



    @Mock

    private AssetHistoryEventRepository assetHistoryEventRepository;



    @Mock

    private UserRepository userRepository;



    @Mock

    private UserService userService;



    @Mock

    private AssetAuthorizationService assetAuthorizationService;



    @InjectMocks

    private AssetHistoryService assetHistoryService;



    @BeforeEach

    void setUp() {

        lenient().when(assetRepository.findById(5L)).thenReturn(Optional.of(asset()));

        lenient().when(userService.getById(AUTHORIZED_USER_ID)).thenReturn(authorizedUser());

    }



    @Test

    void getAssetHistory_shouldReturnHistoryForExistingAsset() {

        when(assetHistoryEventRepository.findByAssetIdOrderByEventDateDescCreatedAtDesc(eq(5L), eq(DEFAULT_PAGEABLE)))

                .thenReturn(new PageImpl<>(List.of(

                        historyEvent(AssetHistoryEventType.MAINTENANCE_COMPLETED, 20L, LocalDate.of(2026, 6, 25)),

                        historyEvent(AssetHistoryEventType.ASSET_REGISTERED, 10L, LocalDate.of(2026, 6, 1))

                )));

        when(userRepository.findAllById(Set.of(20L, 10L))).thenReturn(List.of(

                user(10L, "Coordinator"),

                user(20L, "Field Worker")

        ));



        Page<AssetHistoryResponse> history =

                assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE);



        assertThat(history.getContent()).hasSize(2);

        assertThat(history.getContent().get(0).getEventType()).isEqualTo(AssetHistoryEventType.MAINTENANCE_COMPLETED);

        assertThat(history.getContent().get(1).getEventType()).isEqualTo(AssetHistoryEventType.ASSET_REGISTERED);

        verify(assetAuthorizationService).requireCanViewAsset(any(User.class), any(Asset.class));

    }



    @Test

    void getAssetHistory_shouldReturn404WhenAssetNotFound() {

        when(assetRepository.findById(99L)).thenReturn(Optional.empty());



        assertThatThrownBy(() -> assetHistoryService.getAssetHistory(99L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE))

                .isInstanceOf(ResponseStatusException.class)

                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);



        verify(assetAuthorizationService, never()).requireCanViewAsset(any(), any());

        verify(assetHistoryEventRepository, never())

                .findByAssetIdOrderByEventDateDescCreatedAtDesc(any(), any());

    }



    @Test

    void getAssetHistory_shouldReturn403WhenUserCannotViewAsset() {

        doThrow(new ForbiddenOperationException(

                "You may only view asset history for assets in your own department."))

                .when(assetAuthorizationService).requireCanViewAsset(any(User.class), any(Asset.class));



        assertThatThrownBy(() -> assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE))

                .isInstanceOf(ForbiddenOperationException.class);



        verify(assetHistoryEventRepository, never())

                .findByAssetIdOrderByEventDateDescCreatedAtDesc(any(), any());

    }



    @Test

    void getAssetHistory_shouldReturnEmptyListWhenNoHistoryExists() {

        when(assetHistoryEventRepository.findByAssetIdOrderByEventDateDescCreatedAtDesc(eq(5L), eq(DEFAULT_PAGEABLE)))

                .thenReturn(new PageImpl<>(List.of()));



        Page<AssetHistoryResponse> history =

                assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE);



        assertThat(history.getContent()).isEmpty();

        verify(userRepository, never()).findAllById(any());

    }



    @Test

    void getAssetHistory_shouldPreserveReverseChronologicalOrderingFromRepository() {

        AssetHistoryEvent newest = historyEvent(

                AssetHistoryEventType.WORK_ORDER_ASSIGNED,

                40L,

                LocalDate.of(2026, 6, 25)

        );

        AssetHistoryEvent oldest = historyEvent(

                AssetHistoryEventType.ASSET_REGISTERED,

                10L,

                LocalDate.of(2026, 1, 1)

        );



        when(assetHistoryEventRepository.findByAssetIdOrderByEventDateDescCreatedAtDesc(eq(5L), eq(DEFAULT_PAGEABLE)))

                .thenReturn(new PageImpl<>(List.of(newest, oldest)));

        when(userRepository.findAllById(Set.of(40L, 10L))).thenReturn(List.of(

                user(10L, "Coordinator"),

                user(40L, "Coordinator Two")

        ));



        Page<AssetHistoryResponse> history =

                assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE);



        assertThat(history.getContent()).extracting(AssetHistoryResponse::getEventDate)

                .containsExactly(LocalDate.of(2026, 6, 25), LocalDate.of(2026, 1, 1));

    }



    @Test

    void getAssetHistory_shouldReturnAllExistingEventTypes() {

        List<AssetHistoryEvent> events = new ArrayList<>();

        long userId = 10L;

        LocalDate date = LocalDate.of(2026, 6, 1);

        for (AssetHistoryEventType eventType : AssetHistoryEventType.values()) {

            events.add(historyEvent(eventType, userId, date));

            date = date.plusDays(1);

        }



        when(assetHistoryEventRepository.findByAssetIdOrderByEventDateDescCreatedAtDesc(eq(5L), eq(DEFAULT_PAGEABLE)))

                .thenReturn(new PageImpl<>(events));

        when(userRepository.findAllById(Set.of(10L))).thenReturn(List.of(user(10L, "Coordinator")));



        Page<AssetHistoryResponse> history =

                assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE);



        assertThat(history.getContent()).extracting(AssetHistoryResponse::getEventType)

                .containsExactlyInAnyOrder(AssetHistoryEventType.values());

    }



    @Test

    void getAssetHistory_shouldExposeResponsibleUser() {

        when(assetHistoryEventRepository.findByAssetIdOrderByEventDateDescCreatedAtDesc(eq(5L), eq(DEFAULT_PAGEABLE)))

                .thenReturn(new PageImpl<>(List.of(

                        historyEvent(AssetHistoryEventType.ISSUE_RECORDED, 20L, LocalDate.of(2026, 6, 10))

                )));

        when(userRepository.findAllById(Set.of(20L))).thenReturn(List.of(user(20L, "Field Worker")));



        Page<AssetHistoryResponse> history =

                assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE);



        assertThat(history.getContent().get(0).getResponsibleUserId()).isEqualTo(20L);

        assertThat(history.getContent().get(0).getResponsibleUserName()).isEqualTo("Field Worker");

    }



    @Test

    void getAssetHistory_shouldLeaveResponsibleUserNameNullWhenUserUnavailable() {

        when(assetHistoryEventRepository.findByAssetIdOrderByEventDateDescCreatedAtDesc(eq(5L), eq(DEFAULT_PAGEABLE)))

                .thenReturn(new PageImpl<>(List.of(

                        historyEvent(AssetHistoryEventType.ISSUE_RECORDED, 20L, LocalDate.of(2026, 6, 10))

                )));

        when(userRepository.findAllById(Set.of(20L))).thenReturn(List.of());



        Page<AssetHistoryResponse> history =

                assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE);



        assertThat(history.getContent().get(0).getResponsibleUserId()).isEqualTo(20L);

        assertThat(history.getContent().get(0).getResponsibleUserName()).isNull();

    }



    @Test

    void getAssetHistory_shouldNotCreateNewAssetHistoryEvent() {

        when(assetHistoryEventRepository.findByAssetIdOrderByEventDateDescCreatedAtDesc(eq(5L), eq(DEFAULT_PAGEABLE)))

                .thenReturn(new PageImpl<>(List.of(

                        historyEvent(AssetHistoryEventType.ASSET_REGISTERED, 10L, LocalDate.of(2026, 6, 1))

                )));

        when(userRepository.findAllById(Set.of(10L))).thenReturn(List.of(user(10L, "Coordinator")));



        assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE);



        verify(assetHistoryEventRepository, never()).save(any());

    }



    @Test

    void getAssetHistory_shouldNotModifyAsset() {

        when(assetHistoryEventRepository.findByAssetIdOrderByEventDateDescCreatedAtDesc(eq(5L), eq(DEFAULT_PAGEABLE)))

                .thenReturn(new PageImpl<>(List.of()));



        assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE);



        verify(assetRepository, never()).save(any());

    }



    @Test

    void getAssetHistory_shouldLoadResponsibleUsersInSingleBatch() {

        when(assetHistoryEventRepository.findByAssetIdOrderByEventDateDescCreatedAtDesc(eq(5L), eq(DEFAULT_PAGEABLE)))

                .thenReturn(new PageImpl<>(List.of(

                        historyEvent(AssetHistoryEventType.WORK_ORDER_CREATED, 30L, LocalDate.of(2026, 6, 20)),

                        historyEvent(AssetHistoryEventType.ISSUE_RECORDED, 20L, LocalDate.of(2026, 6, 15))

                )));

        when(userRepository.findAllById(Set.of(30L, 20L))).thenReturn(List.of(

                user(20L, "Field Worker"),

                user(30L, "Manager")

        ));



        assetHistoryService.getAssetHistory(5L, AUTHORIZED_USER_ID, DEFAULT_PAGEABLE);



        verify(userRepository, times(1)).findAllById(eq(Set.of(30L, 20L)));

        verify(userRepository, never()).findById(any());

    }



    private AssetHistoryEvent historyEvent(

            AssetHistoryEventType eventType,

            Long performedByUserId,

            LocalDate eventDate) {

        return new AssetHistoryEvent(asset(), eventType, performedByUserId, eventDate);

    }



    private Asset asset() {

        Department department = new Department("Parks");

        department.setId(1L);

        AssetCategory category = new AssetCategory("Playground");

        category.setId(2L);

        Asset asset = new Asset(

                "Central Playground",

                department,

                category,

                "Memorial Park",

                AssetStatus.ACTIVE,

                LocalDate.of(2026, 6, 1),

                10L

        );

        asset.setId(5L);

        return asset;

    }



    private User authorizedUser() {

        User user = new User("coordinator@test.com", "password", "Coordinator", UserRole.OPERATIONAL_COORDINATOR);

        user.setId(AUTHORIZED_USER_ID);

        Department department = new Department("Parks");

        department.setId(1L);

        user.setDepartment(department);

        return user;

    }



    private User user(Long id, String name) {

        User user = new User("user" + id + "@test.com", "password", name, UserRole.FIELD_EMPLOYEE);

        user.setId(id);

        return user;

    }

}


