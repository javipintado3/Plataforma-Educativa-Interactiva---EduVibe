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

/** Una opción de respuesta de una {@link ExamQuestion}. Exactamente una es correcta. */
@Entity
@Table(name = "exam_options")
@Getter
@Setter
@NoArgsConstructor
public class ExamOption {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private ExamQuestion question;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "correct", nullable = false)
    private boolean correct;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ExamOption(ExamQuestion question, String text, boolean correct, int sortOrder) {
        this.question = question;
        this.text = text;
        this.correct = correct;
        this.sortOrder = sortOrder;
    }
}
