-- Fase 2 de la spec: modo de vista de la clase (Temas estructurados vs.
-- Módulos con todo mezclado). Por defecto 'structured', que es el
-- comportamiento que ya tenía toda clase existente.
ALTER TABLE classes
  ADD COLUMN view_mode varchar(20) NOT NULL DEFAULT 'structured'
    CHECK (view_mode IN ('structured', 'flexible'));
