-- ===========================================================================
-- Foto de perfil.
--
-- Opcional, igual que la portada de clase: sin ella, la interfaz sigue
-- mostrando las iniciales, así que no hace falta un valor por defecto ni
-- migrar nada para las cuentas que ya existen.
-- ===========================================================================

ALTER TABLE users ADD COLUMN avatar_url text;
