package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

class InspectionRepositoryImpl implements InspectionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Inspection> findForExport(Long departmentId, Long from, Long to) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Inspection> query = criteriaBuilder.createQuery(Inspection.class);
        Root<Inspection> root = query.from(Inspection.class);
        Join<Inspection, Asset> asset = root.join("asset", JoinType.INNER);
        asset.join("department", JoinType.INNER);
        asset.join("assetCategory", JoinType.LEFT);
        root.join("inspectionTemplate", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        if (departmentId != null) {
            predicates.add(criteriaBuilder.equal(asset.get("department").get("id"), departmentId));
        }
        if (from != null) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
        }

        query.select(root).distinct(true);
        query.where(predicates.toArray(Predicate[]::new));
        query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
        return entityManager.createQuery(query).getResultList();
    }
}
