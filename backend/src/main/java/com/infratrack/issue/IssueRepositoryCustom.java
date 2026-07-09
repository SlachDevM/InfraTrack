package com.infratrack.issue;

import java.time.LocalDateTime;
import java.util.List;

interface IssueRepositoryCustom {

    List<Issue> findForExport(Long departmentId, LocalDateTime from, LocalDateTime to);
}
