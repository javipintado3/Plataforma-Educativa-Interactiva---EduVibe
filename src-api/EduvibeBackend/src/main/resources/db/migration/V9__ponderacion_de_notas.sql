-- Fase 2: ponderación de notas por tipo de actividad.
-- Cada tarea tiene un peso relativo en la media de la clase; 1.00 (el valor
-- por defecto) es "peso normal" y mantiene el comportamiento de siempre si
-- nadie lo toca.
ALTER TABLE assignments
  ADD COLUMN weight numeric(4,2) NOT NULL DEFAULT 1.00;

ALTER TABLE assignments
  ADD CONSTRAINT chk_assignments_weight_positivo
  CHECK (weight > 0);
