package com.infratrack.operationaldocument;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Business classification of an operational document (UC-012)")
public enum OperationalDocumentType {
    PHOTO,
    REPORT,
    MANUAL,
    PROCEDURE,
    QUOTATION,
    DRAWING,
    OTHER
}
