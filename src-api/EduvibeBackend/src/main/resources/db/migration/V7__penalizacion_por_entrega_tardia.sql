-- Fase 2: penalización automática por entrega tardía.
-- El profesorado fija, por tarea, qué porcentaje se descuenta si la entrega
-- llega después del plazo. 0 (el valor por defecto) mantiene el
-- comportamiento actual: sin penalización.
-- integer, no smallint: Hibernate mapea "int" de Java a INTEGER por defecto,
-- y que no coincida con la columna real es justo lo que valida al arrancar.
ALTER TABLE assignments
  ADD COLUMN late_penalty_percent integer NOT NULL DEFAULT 0;

ALTER TABLE assignments
  ADD CONSTRAINT chk_assignments_late_penalty_percent
  CHECK (late_penalty_percent BETWEEN 0 AND 100);

-- Deja constancia de si la nota guardada ya lleva el descuento aplicado, para
-- poder mostrarlo en la pantalla de corrección sin recalcularlo.
ALTER TABLE grades
  ADD COLUMN late_penalty_applied boolean NOT NULL DEFAULT false;

-- La nota tal cual la escribe el profesorado, antes de aplicar el descuento.
-- Sin esto, reabrir el diálogo de corrección de una entrega tardía ya
-- calificada precargaría la nota YA penalizada, y al guardar otra vez el
-- descuento se aplicaría dos veces.
ALTER TABLE grades
  ADD COLUMN raw_score numeric(5,2);
UPDATE grades SET raw_score = score;
ALTER TABLE grades ALTER COLUMN raw_score SET NOT NULL;
