-- ===========================================================================
-- Exámenes de opción múltiple, con corrección automática y tiempo límite.
--
-- Deliberadamente aparte de assignments/submissions, aunque los dos cuelgan
-- de una clase y de un tema: una tarea es un enunciado abierto que alguien
-- corrige a mano, un examen es preguntas cerradas que se corrigen solas y se
-- responden contrarreloj. Forzarlos en el mismo modelo habría dejado
-- columnas que solo tienen sentido para uno de los dos.
--
-- No hay un estado guardado para "en curso" / "entregado": se deriva de si
-- exam_attempts.submitted_at está a NULL, igual que ya se hace con si una
-- entrega llega tarde comparándola con la fecha límite en vez de guardar un
-- booleano que podría dejar de coincidir.
-- ===========================================================================

CREATE TABLE exams (
  id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id          uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  topic_id          uuid REFERENCES topics(id) ON DELETE SET NULL,
  title             text NOT NULL,
  description       text,
  duration_minutes  int NOT NULL CHECK (duration_minutes > 0),
  due_date          timestamptz,
  created_by        uuid NOT NULL REFERENCES users(id),
  created_at        timestamptz NOT NULL DEFAULT now(),
  updated_at        timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_exams_class ON exams(class_id);
CREATE TRIGGER trg_exams_updated BEFORE UPDATE ON exams
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


CREATE TABLE exam_questions (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  exam_id     uuid NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
  text        text NOT NULL,
  points      int NOT NULL DEFAULT 1 CHECK (points > 0),
  sort_order  int NOT NULL DEFAULT 0
);
CREATE INDEX idx_exam_questions_exam ON exam_questions(exam_id, sort_order);


CREATE TABLE exam_options (
  id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  question_id  uuid NOT NULL REFERENCES exam_questions(id) ON DELETE CASCADE,
  text         text NOT NULL,
  correct      boolean NOT NULL DEFAULT false,
  sort_order   int NOT NULL DEFAULT 0
);
CREATE INDEX idx_exam_options_question ON exam_options(question_id, sort_order);


-- Un intento por alumno y examen: en esta primera versión no hay repesca.
CREATE TABLE exam_attempts (
  id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  exam_id       uuid NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
  student_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  started_at    timestamptz NOT NULL DEFAULT now(),
  submitted_at  timestamptz,
  score         numeric(5,2),
  UNIQUE (exam_id, student_id)
);
CREATE INDEX idx_exam_attempts_student ON exam_attempts(student_id);


CREATE TABLE exam_answers (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  attempt_id          uuid NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE,
  question_id         uuid NOT NULL REFERENCES exam_questions(id) ON DELETE CASCADE,
  -- SET NULL y no CASCADE: si se retoca una opción no debe poder borrar sin
  -- querer la respuesta que un alumno ya dio a otra pregunta.
  selected_option_id  uuid REFERENCES exam_options(id) ON DELETE SET NULL,
  UNIQUE (attempt_id, question_id)
);
