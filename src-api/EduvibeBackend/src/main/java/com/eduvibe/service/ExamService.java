package com.eduvibe.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.exam.AnswerRequest;
import com.eduvibe.dto.exam.BankQuestionResponse;
import com.eduvibe.dto.exam.ExamAttemptResponse;
import com.eduvibe.dto.exam.ExamAttemptSummaryResponse;
import com.eduvibe.dto.exam.ExamDetailResponse;
import com.eduvibe.dto.exam.ExamQuestionResponse;
import com.eduvibe.dto.exam.ExamResponse;
import com.eduvibe.dto.exam.ExamResultResponse;
import com.eduvibe.dto.exam.SaveExamRequest;
import com.eduvibe.exception.BadRequestException;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Enrollment;
import com.eduvibe.model.Exam;
import com.eduvibe.model.ExamAnswer;
import com.eduvibe.model.ExamAttempt;
import com.eduvibe.model.ExamOption;
import com.eduvibe.model.ExamQuestion;
import com.eduvibe.model.Notification;
import com.eduvibe.model.SchoolClass;
import com.eduvibe.model.User;
import com.eduvibe.model.enums.EnrollmentRole;
import com.eduvibe.repository.EnrollmentRepository;
import com.eduvibe.repository.ExamAnswerRepository;
import com.eduvibe.repository.ExamAttemptRepository;
import com.eduvibe.repository.ExamOptionRepository;
import com.eduvibe.repository.ExamQuestionRepository;
import com.eduvibe.repository.ExamRepository;
import com.eduvibe.repository.UserRepository;
import com.eduvibe.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

/**
 * Exámenes de opción múltiple de una clase: alta, consulta, y el intento del
 * alumnado con su corrección automática.
 *
 * Aparte de {@link AssignmentService} a propósito, igual que {@link Exam} está
 * aparte de {@link com.eduvibe.model.Assignment} en el modelo: quién puede
 * crearlos y editarlos lo decide {@link ClassAccessService}, con las mismas
 * reglas que una tarea.
 */
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamOptionRepository examOptionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassAccessService acceso;
    private final AuthService authService;
    private final TopicService topicService;
    private final NotificationService notificationService;

    // ------------------------------------------------------------ alta y consulta

    @Transactional
    public ExamDetailResponse crear(UUID classId, SaveExamRequest peticion) {
        SchoolClass clase = acceso.exigirEditable(classId);
        AuthenticatedUser autenticado = authService.identidadActual();

        User autor = userRepository.findById(autenticado.id())
                .orElseThrow(() -> NotFoundException.de("Usuario", autenticado.id()));

        Exam examen = new Exam(clase, peticion.title().trim(), peticion.description(),
                peticion.durationMinutes(), peticion.dueDate(), autor);
        examen.setTopic(topicService.resolverDeClase(peticion.topicId(), classId));
        examRepository.saveAndFlush(examen);

        List<SaveExamRequest.QuestionInput> nuevas = peticion.questionsOSinNinguna();
        List<SaveExamRequest.ReuseQuestionInput> reutilizadas = peticion.reuseQuestionsOSinNinguna();

        if (nuevas.isEmpty() && reutilizadas.isEmpty()) {
            throw new BadRequestException("El examen necesita al menos una pregunta");
        }

        int totalPuntos = 0;
        int orden = 0;

        for (SaveExamRequest.QuestionInput preguntaInput : nuevas) {
            exigirUnaSolaCorrecta(preguntaInput);

            ExamQuestion pregunta = new ExamQuestion(examen, preguntaInput.text().trim(),
                    preguntaInput.puntosOPorDefecto(), orden++);
            examQuestionRepository.save(pregunta);
            totalPuntos += pregunta.getPoints();

            int ordenOpcion = 0;
            for (SaveExamRequest.OptionInput opcionInput : preguntaInput.options()) {
                examOptionRepository.save(new ExamOption(pregunta, opcionInput.text().trim(),
                        opcionInput.correct(), ordenOpcion++));
            }
        }

        for (SaveExamRequest.ReuseQuestionInput reusoInput : reutilizadas) {
            totalPuntos += copiarDelBanco(examen, classId, reusoInput, orden++);
        }

        notificarAlumnado(classId, clase, examen);

        return ExamDetailResponse.de(examen, true, nuevas.size() + reutilizadas.size(), totalPuntos, null, null);
    }

    /**
     * Copia una pregunta del banco de la clase en este examen, con sus
     * propias opciones. Es una copia y no una referencia compartida a
     * propósito: si el profesorado retoca la pregunta original más adelante,
     * los exámenes ya hechos no deben cambiar retroactivamente lo que el
     * alumnado ya respondió.
     */
    private int copiarDelBanco(Exam examen, UUID classId, SaveExamRequest.ReuseQuestionInput reusoInput, int orden) {
        ExamQuestion original = examQuestionRepository.findById(reusoInput.questionId())
                .filter(q -> q.getExam().getSchoolClass().getId().equals(classId))
                .orElseThrow(() -> new BadRequestException("La pregunta indicada no es del banco de esta clase"));

        int puntos = reusoInput.puntosODelOriginal(original.getPoints());
        ExamQuestion copia = new ExamQuestion(examen, original.getText(), puntos, orden);
        examQuestionRepository.save(copia);

        for (ExamOption opcion : examOptionRepository.findByQuestionIdOrderBySortOrderAsc(original.getId())) {
            examOptionRepository.save(new ExamOption(copia, opcion.getText(), opcion.isCorrect(), opcion.getSortOrder()));
        }

        return puntos;
    }

    /**
     * El banco de preguntas de la clase: todas las ya usadas en algún examen,
     * para reutilizarlas en uno nuevo sin volver a escribirlas.
     */
    @Transactional(readOnly = true)
    public List<BankQuestionResponse> listarBanco(UUID classId) {
        acceso.exigirEditable(classId);

        List<ExamQuestion> banco = examQuestionRepository.findBancoDeClase(classId);
        Map<UUID, List<ExamOption>> opcionesPorPregunta = opcionesAgrupadasPorPregunta(banco);

        return banco.stream()
                .map(p -> BankQuestionResponse.de(p, opcionesPorPregunta.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    /** Cada pregunta necesita exactamente una opción correcta: ni cero ni dos. */
    private void exigirUnaSolaCorrecta(SaveExamRequest.QuestionInput pregunta) {
        long correctas = pregunta.options().stream().filter(SaveExamRequest.OptionInput::correct).count();
        if (correctas != 1) {
            throw new BadRequestException(
                    "Cada pregunta necesita exactamente una opción correcta (\"" + pregunta.text()
                            + "\" tiene " + correctas + ")");
        }
    }

    private void notificarAlumnado(UUID classId, SchoolClass clase, Exam examen) {
        List<User> alumnado = enrollmentRepository
                .findBySchoolClassIdAndRoleInClassOrderByUserNameAsc(classId, EnrollmentRole.STUDENT)
                .stream().map(Enrollment::getUser).toList();

        notificationService.emitirParaVarios(alumnado, Notification.EXAMEN_NUEVO, Map.of(
                "className", clase.getName(), "title", examen.getTitle(), "examId", examen.getId().toString()));
    }

    /**
     * Exámenes de una clase. Al profesorado le importan los intentos
     * recibidos; al alumnado, el estado de su propio intento.
     */
    @Transactional(readOnly = true)
    public List<ExamResponse> listar(UUID classId) {
        acceso.exigirVisible(classId);
        AuthenticatedUser usuario = authService.identidadActual();

        List<Exam> examenes = examRepository.findDeClase(classId);
        if (examenes.isEmpty()) {
            return List.of();
        }

        if (acceso.puedeCalificarEn(classId)) {
            return examenes.stream()
                    .map(examen -> ExamResponse.paraProfesor(examen,
                            (int) examQuestionRepository.countByExamId(examen.getId()),
                            examAttemptRepository.countByExamId(examen.getId())))
                    .toList();
        }

        List<UUID> examIds = examenes.stream().map(Exam::getId).toList();
        Map<UUID, ExamAttempt> misIntentos = new HashMap<>();
        for (ExamAttempt intento : examAttemptRepository.findByExamIdInAndStudentId(examIds, usuario.id())) {
            misIntentos.put(intento.getExam().getId(), intento);
        }

        return examenes.stream().map(examen -> {
            ExamAttempt intento = misIntentos.get(examen.getId());
            return ExamResponse.paraAlumno(examen,
                    (int) examQuestionRepository.countByExamId(examen.getId()),
                    estadoDe(intento),
                    intento != null && intento.estaEntregado() ? intento.getScore() : null);
        }).toList();
    }

    @Transactional(readOnly = true)
    public ExamDetailResponse detalle(UUID examId) {
        Exam examen = buscarVisible(examId);
        UUID classId = examen.getSchoolClass().getId();
        boolean puedoEditar = acceso.puedeCalificarEn(classId);

        int numeroPreguntas = (int) examQuestionRepository.countByExamId(examId);
        int totalPuntos = sumaDePuntos(examId);

        if (puedoEditar) {
            return ExamDetailResponse.de(examen, true, numeroPreguntas, totalPuntos, null, null);
        }

        AuthenticatedUser usuario = authService.identidadActual();
        ExamAttempt miIntento = examAttemptRepository.findByExamIdAndStudentId(examId, usuario.id()).orElse(null);

        return ExamDetailResponse.de(examen, false, numeroPreguntas, totalPuntos,
                estadoDe(miIntento), miIntento != null && miIntento.estaEntregado() ? miIntento.getScore() : null);
    }

    /** Las preguntas con sus respuestas correctas: solo para el profesorado, vista de repaso. */
    @Transactional(readOnly = true)
    public List<ExamQuestionResponse> preguntas(UUID examId) {
        Exam examen = examRepository.findById(examId)
                .orElseThrow(() -> NotFoundException.de("Examen", examId));
        acceso.exigirEditable(examen.getSchoolClass().getId());

        List<ExamQuestion> preguntas = examQuestionRepository.findByExamIdOrderBySortOrderAsc(examId);
        Map<UUID, List<ExamOption>> opcionesPorPregunta = opcionesAgrupadasPorPregunta(preguntas);

        return preguntas.stream()
                .map(p -> ExamQuestionResponse.de(p, opcionesPorPregunta.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    /** Borra un examen. Solo se permite mientras nadie lo haya empezado. */
    @Transactional
    public void eliminar(UUID examId) {
        Exam examen = examRepository.findById(examId)
                .orElseThrow(() -> NotFoundException.de("Examen", examId));
        acceso.exigirEditable(examen.getSchoolClass().getId());

        long intentos = examAttemptRepository.countByExamId(examId);
        if (intentos > 0) {
            throw new BadRequestException(
                    "No se puede borrar: ya hay " + intentos + " intento(s) registrado(s).");
        }

        examRepository.delete(examen);
    }

    // ---------------------------------------------------------------- intento

    /**
     * Empieza el examen, o retoma el intento en curso si ya se había
     * empezado: llamarlo dos veces no crea dos intentos.
     */
    @Transactional
    public ExamAttemptResponse comenzarOReanudar(UUID examId) {
        Exam examen = examRepository.findById(examId)
                .orElseThrow(() -> NotFoundException.de("Examen", examId));
        UUID classId = examen.getSchoolClass().getId();
        acceso.exigirSerAlumnoDe(classId);

        AuthenticatedUser autenticado = authService.identidadActual();
        User alumno = userRepository.findById(autenticado.id())
                .orElseThrow(() -> NotFoundException.de("Usuario", autenticado.id()));

        ExamAttempt intento = examAttemptRepository.findByExamIdAndStudentId(examId, alumno.getId()).orElse(null);

        if (intento == null) {
            if (examen.haVencido()) {
                throw new BadRequestException("Ya no se puede empezar este examen: pasó la fecha límite");
            }
            intento = new ExamAttempt(examen, alumno);
            examAttemptRepository.saveAndFlush(intento);
        } else if (intento.estaEntregado()) {
            throw new BadRequestException("Ya entregaste este examen");
        }

        return construirAttemptResponse(examen, intento);
    }

    private ExamAttemptResponse construirAttemptResponse(Exam examen, ExamAttempt intento) {
        List<ExamQuestion> preguntas = barajarPorIntento(
                examQuestionRepository.findByExamIdOrderBySortOrderAsc(examen.getId()), intento.getId());
        Map<UUID, List<ExamOption>> opcionesPorPregunta = opcionesAgrupadasPorPregunta(preguntas);

        Map<UUID, UUID> misRespuestas = new HashMap<>();
        for (ExamAnswer respuesta : examAnswerRepository.findByAttemptId(intento.getId())) {
            if (respuesta.getSelectedOption() != null) {
                misRespuestas.put(respuesta.getQuestion().getId(), respuesta.getSelectedOption().getId());
            }
        }

        List<ExamAttemptResponse.QuestionResponse> preguntasRespuesta = preguntas.stream()
                .map(p -> new ExamAttemptResponse.QuestionResponse(
                        p.getId(), p.getText(), p.getPoints(),
                        barajarPorIntentoYPregunta(opcionesPorPregunta.getOrDefault(p.getId(), List.of()),
                                intento.getId(), p.getId())
                                .stream()
                                .map(o -> new ExamAttemptResponse.OptionResponse(o.getId(), o.getText()))
                                .toList(),
                        misRespuestas.get(p.getId())))
                .toList();

        return new ExamAttemptResponse(intento.getId(), examen.getId(), examen.getTitle(),
                examen.getDurationMinutes(), intento.getStartedAt(), intento.limiteDeTiempo(),
                intento.estaEntregado(), preguntasRespuesta);
    }

    /** Guarda o cambia la respuesta a una pregunta. Autoguardado: se llama en cada clic. */
    @Transactional
    public void guardarRespuesta(UUID examId, UUID questionId, AnswerRequest peticion) {
        ExamAttempt intento = miIntentoEnCurso(examId);

        ExamQuestion pregunta = examQuestionRepository.findById(questionId)
                .filter(p -> p.getExam().getId().equals(examId))
                .orElseThrow(() -> NotFoundException.de("Pregunta", questionId));

        ExamOption opcion = null;
        if (peticion.selectedOptionId() != null) {
            opcion = examOptionRepository.findById(peticion.selectedOptionId())
                    .filter(o -> o.getQuestion().getId().equals(questionId))
                    .orElseThrow(() -> new BadRequestException("La opción indicada no es de esta pregunta"));
        }

        ExamAnswer respuesta = examAnswerRepository.findByAttemptIdAndQuestionId(intento.getId(), questionId)
                .orElseGet(() -> new ExamAnswer(intento, pregunta, null));

        respuesta.setSelectedOption(opcion);
        examAnswerRepository.save(respuesta);
    }

    /**
     * Entrega el intento y lo corrige al momento: cada pregunta acertada suma
     * sus puntos, las demás no restan.
     */
    @Transactional
    public ExamResultResponse entregar(UUID examId) {
        ExamAttempt intento = miIntentoEnCurso(examId);

        List<ExamQuestion> preguntas = barajarPorIntento(
                examQuestionRepository.findByExamIdOrderBySortOrderAsc(examId), intento.getId());
        Map<UUID, ExamAnswer> respuestaPorPregunta = new HashMap<>();
        for (ExamAnswer respuesta : examAnswerRepository.findByAttemptId(intento.getId())) {
            respuestaPorPregunta.put(respuesta.getQuestion().getId(), respuesta);
        }

        Map<UUID, UUID> correctaPorPregunta = new HashMap<>();
        for (ExamOption opcion : examOptionRepository
                .findByQuestionIdIn(preguntas.stream().map(ExamQuestion::getId).toList())) {
            if (opcion.isCorrect()) {
                correctaPorPregunta.put(opcion.getQuestion().getId(), opcion.getId());
            }
        }

        BigDecimal nota = BigDecimal.ZERO;
        List<ExamResultResponse.QuestionResultResponse> desglose = new ArrayList<>();

        for (ExamQuestion pregunta : preguntas) {
            ExamAnswer respuesta = respuestaPorPregunta.get(pregunta.getId());
            boolean acerto = respuesta != null && respuesta.esCorrecta();
            if (acerto) {
                nota = nota.add(BigDecimal.valueOf(pregunta.getPoints()));
            }

            UUID miRespuesta = respuesta == null || respuesta.getSelectedOption() == null
                    ? null : respuesta.getSelectedOption().getId();

            desglose.add(new ExamResultResponse.QuestionResultResponse(
                    pregunta.getId(), pregunta.getText(), pregunta.getPoints(),
                    miRespuesta, correctaPorPregunta.get(pregunta.getId()), acerto));
        }

        intento.entregar(nota);
        examAttemptRepository.save(intento);

        int totalPuntos = preguntas.stream().mapToInt(ExamQuestion::getPoints).sum();
        return new ExamResultResponse(intento.getId(), nota, totalPuntos, intento.getSubmittedAt(), desglose);
    }

    /** Todos los intentos de un examen, para la vista de corrección del profesorado. */
    @Transactional(readOnly = true)
    public List<ExamAttemptSummaryResponse> listarIntentos(UUID examId) {
        Exam examen = examRepository.findById(examId)
                .orElseThrow(() -> NotFoundException.de("Examen", examId));
        acceso.exigirEditable(examen.getSchoolClass().getId());

        return examAttemptRepository.findByExamIdOrderByStudentNameAsc(examId).stream()
                .map(intento -> new ExamAttemptSummaryResponse(
                        intento.getId(), intento.getStudent().getId(), intento.getStudent().getName(),
                        intento.getStartedAt(), intento.getSubmittedAt(), intento.getScore(),
                        intento.estaEntregado()))
                .toList();
    }

    // ------------------------------------------------------------------- ayuda

    /** Busca un examen comprobando que quien pregunta pueda ver su clase. */
    private Exam buscarVisible(UUID examId) {
        Exam examen = examRepository.findById(examId)
                .orElseThrow(() -> NotFoundException.de("Examen", examId));
        acceso.exigirVisible(examen.getSchoolClass().getId());
        return examen;
    }

    /** El intento de quien pregunta, si sigue a tiempo y sin entregar. */
    private ExamAttempt miIntentoEnCurso(UUID examId) {
        AuthenticatedUser autenticado = authService.identidadActual();
        ExamAttempt intento = examAttemptRepository.findByExamIdAndStudentId(examId, autenticado.id())
                .orElseThrow(() -> new BadRequestException("Todavía no has empezado este examen"));

        if (intento.estaEntregado()) {
            throw new BadRequestException("Ya entregaste este examen, no puedes cambiar tus respuestas");
        }
        if (Instant.now().isAfter(intento.limiteDeTiempo())) {
            throw new BadRequestException("Se acabó el tiempo para este examen");
        }
        return intento;
    }

    /**
     * Baraja una lista con una semilla fija por intento: cada alumno ve sus
     * preguntas en un orden distinto, pero recargar la página o volver a
     * entregar no las reordena otra vez, porque la semilla es siempre la
     * misma para ese intento. No hace falta guardar el orden en ningún sitio.
     */
    private <T> List<T> barajarPorIntento(List<T> lista, UUID intentoId) {
        List<T> copia = new ArrayList<>(lista);
        Collections.shuffle(copia, new Random(intentoId.getMostSignificantBits()));
        return copia;
    }

    /** Igual que {@link #barajarPorIntento}, pero con una semilla propia por pregunta: cada una baraja sus opciones distinto. */
    private <T> List<T> barajarPorIntentoYPregunta(List<T> lista, UUID intentoId, UUID preguntaId) {
        List<T> copia = new ArrayList<>(lista);
        Collections.shuffle(copia, new Random(intentoId.getMostSignificantBits() ^ preguntaId.getMostSignificantBits()));
        return copia;
    }

    private Map<UUID, List<ExamOption>> opcionesAgrupadasPorPregunta(List<ExamQuestion> preguntas) {
        if (preguntas.isEmpty()) {
            return Map.of();
        }
        List<UUID> questionIds = preguntas.stream().map(ExamQuestion::getId).toList();

        Map<UUID, List<ExamOption>> opcionesPorPregunta = new HashMap<>();
        for (ExamOption opcion : examOptionRepository.findByQuestionIdIn(questionIds)) {
            opcionesPorPregunta.computeIfAbsent(opcion.getQuestion().getId(), k -> new ArrayList<>()).add(opcion);
        }
        return opcionesPorPregunta;
    }

    private int sumaDePuntos(UUID examId) {
        return examQuestionRepository.findByExamIdOrderBySortOrderAsc(examId).stream()
                .mapToInt(ExamQuestion::getPoints)
                .sum();
    }

    private String estadoDe(ExamAttempt intento) {
        if (intento == null) {
            return "no_empezado";
        }
        return intento.estaEntregado() ? "entregado" : "en_curso";
    }
}
