-- ===========================================================================
-- El email identifica de forma única a una persona en toda la plataforma.
--
-- La especificación proponía UNIQUE (org_id, email), que permite el mismo
-- correo en dos organizaciones distintas. Pero el inicio de sesión pide solo
-- email y contraseña, sin elegir centro: con esa restricción, dos cuentas
-- homónimas en centros diferentes harían la consulta ambigua y no habría forma
-- de saber a cuál se quiere entrar.
--
-- Se mantiene además la restricción por organización, que sigue siendo la que
-- expresa la regla de negocio "no puede haber dos alumnos con el mismo correo
-- en el mismo centro".
-- ===========================================================================

CREATE UNIQUE INDEX idx_users_email_global ON users (email);
