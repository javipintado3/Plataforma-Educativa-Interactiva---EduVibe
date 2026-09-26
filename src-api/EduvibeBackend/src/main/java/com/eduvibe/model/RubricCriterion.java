package com.eduvibe.model;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Un criterio de una rúbrica, con su propia puntuación máxima. */
@Entity
@Table(name = "rubric_criteria")
@Getter
@Setter
@NoArgsConstructor
public class RubricCriterion {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rubric_id", nullable = false)
    private Rubric rubric;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "max_points", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxPoints;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public RubricCriterion(Rubric rubric, String description, BigDecimal maxPoints, int sortOrder) {
        this.rubric = rubric;
        this.description = description;
        this.maxPoints = maxPoints;
        this.sortOrder = sortOrder;
    }
}
