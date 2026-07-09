package com.infratrack.inspection;

import java.util.List;

interface InspectionRepositoryCustom {

    List<Inspection> findForExport(Long departmentId, Long from, Long to);
}
