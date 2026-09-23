package com.eduvibe.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduvibe.dto.submission.GradeRequest;
import com.eduvibe.dto.submission.SubmissionResponse;
import com.eduvibe.service.SubmissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Corrección de entregas.
 */
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    /**
     * Pone o corrige la nota. Es PUT porque una entrega tiene una sola
     * calificación: volver a llamarlo la sustituye, no añade otra.
     */
    @PutMapping("/{submissionId}/grade")
    public ResponseEntity<SubmissionResponse> calificar(@PathVariable UUID submissionId,
                                                        @Valid @RequestBody GradeRequest peticion) {
        return ResponseEntity.ok(submissionService.calificar(submissionId, peticion));
    }
}
