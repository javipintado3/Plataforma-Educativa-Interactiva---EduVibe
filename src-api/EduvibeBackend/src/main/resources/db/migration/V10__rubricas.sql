-- Fase 2: rúbricas visibles para el alumnado antes de entregar.
-- Una tarea tiene como mucho una rúbrica, hecha de criterios con su propia
-- puntuación máxima. Al calificar con rúbrica, la nota final es la suma de
-- lo puntuado en cada criterio (RubricScore), no un número suelto.
CREATE TABLE rubrics (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  assignment_id   uuid NOT NULL UNIQUE REFERENCES assignments(id) ON DELETE CASCADE,
  created_at      timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE rubric_criteria (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  rubric_id       uuid NOT NULL REFERENCES rubrics(id) ON DELETE CASCADE,
  description     text NOT NULL,
  max_points      numeric(5,2) NOT NULL CHECK (max_points > 0),
  sort_order      int NOT NULL DEFAULT 0
);
CREATE INDEX idx_rubric_criteria_rubric ON rubric_criteria(rubric_id);

-- Cuelga de la nota (grades), no de la entrega: solo existe una vez que hay
-- calificación, igual que el resto de una corrección.
CREATE TABLE rubric_scores (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  grade_id        uuid NOT NULL REFERENCES grades(id) ON DELETE CASCADE,
  criterion_id    uuid NOT NULL REFERENCES rubric_criteria(id) ON DELETE CASCADE,
  points          numeric(5,2) NOT NULL CHECK (points >= 0),
  UNIQUE (grade_id, criterion_id)
);
CREATE INDEX idx_rubric_scores_grade ON rubric_scores(grade_id);
