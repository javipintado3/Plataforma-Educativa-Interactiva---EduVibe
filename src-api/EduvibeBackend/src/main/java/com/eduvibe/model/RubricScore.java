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

/**
 * Lo puntuado en un criterio concreto, dentro de una corrección concreta.
 *
 * Cuelga de {@link Grade} y no de {@link Submission}: solo existe una vez que
 * hay calificación, igual que el resto de los datos de una corrección.
 */
@Entity
@Table(name = "rubric_scores")
@Getter
@Setter
@NoArgsConstructor
public class RubricScore {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criterion_id", nullable = false)
    private RubricCriterion criterion;

    @Column(name = "points", nullable = false, precision = 5, scale = 2)
    private BigDecimal points;

    public RubricScore(Grade grade, RubricCriterion criterion, BigDecimal points) {
        this.grade = grade;
        this.criterion = criterion;
        this.points = points;
    }
}
