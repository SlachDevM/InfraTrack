package com.infratrack.issue;

import com.infratrack.asset.Asset;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class IssueRepositoryImpl implements IssueRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Issue> findForExport(Long departmentId, LocalDateTime from, LocalDateTime to) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Issue> query = criteriaBuilder.createQuery(Issue.class);
        Root<Issue> root = query.from(Issue.class);
        Join<Issue, Asset> asset = root.join("asset", JoinType.INNER);
        asset.join("department", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        if (departmentId != null) {
            predicates.add(criteriaBuilder.equal(asset.get("department").get("id"), departmentId));
        }
        if (from != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("recordedAt"), from));
        }
        if (to != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("recordedAt"), to));
        }

        query.select(root).distinct(true);
        query.where(predicates.toArray(Predicate[]::new));
        query.orderBy(criteriaBuilder.desc(root.get("recordedAt")));
        return entityManager.createQuery(query).getResultList();
    }
}
