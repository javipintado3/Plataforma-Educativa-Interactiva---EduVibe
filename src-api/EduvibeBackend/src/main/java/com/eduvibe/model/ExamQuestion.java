package com.eduvibe.model;

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

/** Una pregunta de un examen. Sus opciones viven en {@link ExamOption}. */
@Entity
@Table(name = "exam_questions")
@Getter
@Setter
@NoArgsConstructor
public class ExamQuestion {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "points", nullable = false)
    private int points;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ExamQuestion(Exam exam, String text, int points, int sortOrder) {
        this.exam = exam;
        this.text = text;
        this.points = points;
        this.sortOrder = sortOrder;
    }
}
