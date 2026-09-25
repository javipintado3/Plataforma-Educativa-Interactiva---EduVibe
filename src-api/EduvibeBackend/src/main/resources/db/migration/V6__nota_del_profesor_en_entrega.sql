-- Nota que el profesorado puede dejar en una entrega antes de calificarla
-- (p.ej. "revisa este apartado"), independiente de la calificación: no hace
-- falta poner nota para poder comentar algo.
ALTER TABLE submissions ADD COLUMN teacher_note text;
