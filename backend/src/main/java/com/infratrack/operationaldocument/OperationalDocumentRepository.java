package com.infratrack.operationaldocument;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OperationalDocumentRepository extends JpaRepository<OperationalDocument, Long> {

    List<OperationalDocument> findByAssetIdOrderByUploadedAtDesc(Long assetId);
}
