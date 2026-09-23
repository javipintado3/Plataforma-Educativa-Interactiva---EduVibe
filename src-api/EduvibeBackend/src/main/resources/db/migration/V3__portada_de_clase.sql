-- ===========================================================================
-- Imagen de portada de una clase.
--
-- Es opcional: cuando no hay ninguna, la interfaz compone una portada propia
-- a partir del identificador y del color de la clase. Se hace así, y no
-- guardando una imagen por defecto en esta columna, por dos motivos: no
-- depende de que un archivo externo siga existiendo, y deja claro en los datos
-- qué clases tienen portada de verdad y cuáles no.
-- ===========================================================================

ALTER TABLE classes ADD COLUMN image_url text;
