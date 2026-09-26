-- Fase 2: entrega grupal. Subgrupos reutilizables dentro de una clase (la
-- spec también los pide en "4.2 Subgrupos"); una tarea marcada como grupal
-- se entrega y se corrige una vez por subgrupo, no una vez por alumno.
CREATE TABLE class_groups (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  name            text NOT NULL,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_class_groups_class ON class_groups(class_id);

CREATE TABLE class_group_members (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  group_id        uuid NOT NULL REFERENCES class_groups(id) ON DELETE CASCADE,
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE (group_id, user_id)
);
CREATE INDEX idx_class_group_members_group ON class_group_members(group_id);
CREATE INDEX idx_class_group_members_user ON class_group_members(user_id);

ALTER TABLE assignments
  ADD COLUMN group_assignment boolean NOT NULL DEFAULT false;

-- Qué subgrupo entregó esta fila. Null salvo en tareas grupales: ahí, todas
-- las entregas del mismo subgrupo comparten lo escrito y la nota, pero cada
-- alumno conserva su propia fila (misma restricción de siempre: una entrega
-- por alumno y tarea), para no tocar el resto del modelo de calificaciones.
ALTER TABLE submissions
  ADD COLUMN class_group_id uuid REFERENCES class_groups(id) ON DELETE SET NULL;
