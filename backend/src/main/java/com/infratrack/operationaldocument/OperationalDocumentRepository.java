package com.infratrack.operationaldocument;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationalDocumentRepository extends JpaRepository<OperationalDocument, Long> {

    @EntityGraph(attributePaths = {"asset"})
    List<OperationalDocument> findByAssetIdOrderByUploadedAtDesc(Long assetId);

    @EntityGraph(attributePaths = {"asset"})
    Page<OperationalDocument> findByAssetIdOrderByUploadedAtDesc(Long assetId, Pageable pageable);
}
