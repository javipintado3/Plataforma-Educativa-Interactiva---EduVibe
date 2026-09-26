-- Fase 2: restricción de acceso a materiales por fecha.
-- Null (el valor por defecto) mantiene el comportamiento actual: visible
-- desde que se publica.
ALTER TABLE resources
  ADD COLUMN available_from timestamptz;
