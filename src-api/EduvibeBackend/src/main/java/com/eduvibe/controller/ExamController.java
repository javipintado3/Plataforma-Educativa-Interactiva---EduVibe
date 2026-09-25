package com.eduvibe.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduvibe.dto.exam.AnswerRequest;
import com.eduvibe.dto.exam.ExamAttemptResponse;
import com.eduvibe.dto.exam.ExamAttemptSummaryResponse;
import com.eduvibe.dto.exam.ExamDetailResponse;
import com.eduvibe.dto.exam.ExamQuestionResponse;
import com.eduvibe.dto.exam.ExamResultResponse;
import com.eduvibe.service.ExamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Un examen concreto y el intento de quien lo hace.
 *
 * Crear un examen cuelga de su clase ({@code POST /api/classes/{id}/exams}),
 * igual que una tarea; a partir de ahí tiene identidad propia.
 */
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping("/{examId}")
    public ResponseEntity<ExamDetailResponse> detalle(@PathVariable UUID examId) {
        return ResponseEntity.ok(examService.detalle(examId));
    }

    /** Preguntas con la respuesta correcta marcada. Solo para el profesorado. */
    @GetMapping("/{examId}/questions")
    public ResponseEntity<List<ExamQuestionResponse>> preguntas(@PathVariable UUID examId) {
        return ResponseEntity.ok(examService.preguntas(examId));
    }

    @DeleteMapping("/{examId}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID examId) {
        examService.eliminar(examId);
        return ResponseEntity.noContent().build();
    }

    /** Empieza el examen, o retoma el intento en curso si ya se había empezado. */
    @PostMapping("/{examId}/attempt")
    public ResponseEntity<ExamAttemptResponse> comenzarOReanudar(@PathVariable UUID examId) {
        return ResponseEntity.ok(examService.comenzarOReanudar(examId));
    }

    /** Autoguardado de una respuesta mientras se hace el examen. */
    @PutMapping("/{examId}/attempt/answers/{questionId}")
    public ResponseEntity<Void> guardarRespuesta(@PathVariable UUID examId, @PathVariable UUID questionId,
                                                 @Valid @RequestBody AnswerRequest peticion) {
        examService.guardarRespuesta(examId, questionId, peticion);
        return ResponseEntity.noContent().build();
    }

    /** Entrega el intento y lo corrige al momento. */
    @PostMapping("/{examId}/attempt/submit")
    public ResponseEntity<ExamResultResponse> entregar(@PathVariable UUID examId) {
        return ResponseEntity.ok(examService.entregar(examId));
    }

    /** Todos los intentos del examen. Solo para el profesorado de la clase. */
    @GetMapping("/{examId}/attempts")
    public ResponseEntity<List<ExamAttemptSummaryResponse>> intentos(@PathVariable UUID examId) {
        return ResponseEntity.ok(examService.listarIntentos(examId));
    }
}
