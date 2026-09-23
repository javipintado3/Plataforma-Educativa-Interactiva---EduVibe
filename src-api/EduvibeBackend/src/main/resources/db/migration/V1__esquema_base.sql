-- ===========================================================================
-- Eduvibe v2 — esquema base
--
-- Decisiones que se apartan de la especificación, y por qué:
--
--  * Los enumerados son varchar + CHECK, no tipos ENUM de PostgreSQL. Añadir un
--    valor a un ENUM nativo exige ALTER TYPE, que no puede ejecutarse dentro de
--    la transacción en la que Flyway envuelve cada migración. Además encaja sin
--    fricción con @Enumerated(EnumType.STRING) y con ddl-auto=validate.
--
--  * El email es varchar y se normaliza a minúsculas en la aplicación, en lugar
--    de usar la extensión citext, que Hibernate no valida como varchar.
--
--  * classes no tiene teacher_id. Quién imparte una clase se responde con
--    enrollments.role_in_class = 'teacher': una sola fuente de verdad, y además
--    permite varios profesores por clase.
--
--  * submission_status no incluye 'late'. Llegar tarde no es un estado, es
--    submitted_at > due_date: una entrega puede estar calificada y tarde a la
--    vez, y un enum no puede expresar las dos cosas.
--
--  * Se añade la tabla topics, que la especificación no tenía pero que hace
--    falta para agrupar tareas y materiales por tema o semana.
--
--  * calendar_events guarda únicamente eventos manuales. Las fechas de entrega
--    se derivan de assignments.due_date en la consulta, para no tener el mismo
--    dato en dos sitios.
-- ===========================================================================

-- Mantiene updated_at al día sin que la aplicación tenga que acordarse.
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ---------------------------------------------------------------------------
-- Centro / colegio / organización
-- ---------------------------------------------------------------------------
CREATE TABLE organizations (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name            text NOT NULL,
  allowed_domain  text UNIQUE,              -- ej. 'iesalixar.edu'; NULL = sin restricción
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_organizations_updated BEFORE UPDATE ON organizations
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Usuarios
-- ---------------------------------------------------------------------------
CREATE TABLE users (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id          uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  email           varchar(255) NOT NULL,    -- normalizado a minúsculas por la aplicación
  name            text NOT NULL,
  role            varchar(20) NOT NULL DEFAULT 'student'
                    CHECK (role IN ('admin', 'teacher', 'student', 'guardian')),
  status          varchar(20) NOT NULL DEFAULT 'pending'
                    CHECK (status IN ('pending', 'active', 'disabled')),
  password_hash   text,                     -- NULL hasta que el usuario la establece
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, email)
);
CREATE INDEX idx_users_org ON users(org_id);
CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Invitaciones de alta (token de un solo uso)
-- ---------------------------------------------------------------------------
CREATE TABLE invitations (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash      text NOT NULL UNIQUE,     -- se guarda el hash, no el token en claro
  expires_at      timestamptz NOT NULL,
  used_at         timestamptz,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_invitations_pending ON invitations(expires_at) WHERE used_at IS NULL;
CREATE INDEX idx_invitations_user ON invitations(user_id);


-- ---------------------------------------------------------------------------
-- Clases
-- ---------------------------------------------------------------------------
CREATE TABLE classes (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id          uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  name            text NOT NULL,
  subject         text,
  color           varchar(20),              -- franja de color de la tarjeta en el dashboard
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_classes_org ON classes(org_id);
CREATE TRIGGER trg_classes_updated BEFORE UPDATE ON classes
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Matriculaciones: quién pertenece a una clase, y como qué
-- ---------------------------------------------------------------------------
CREATE TABLE enrollments (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_in_class   varchar(20) NOT NULL CHECK (role_in_class IN ('teacher', 'student')),
  enrolled_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (class_id, user_id)
);
CREATE INDEX idx_enrollments_user ON enrollments(user_id);
CREATE INDEX idx_enrollments_class_role ON enrollments(class_id, role_in_class);


-- ---------------------------------------------------------------------------
-- Temas: agrupan tareas y materiales dentro de una clase
-- ---------------------------------------------------------------------------
CREATE TABLE topics (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  title           text NOT NULL,
  sort_order      int NOT NULL DEFAULT 0,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_topics_class ON topics(class_id, sort_order);


-- ---------------------------------------------------------------------------
-- Tareas
-- ---------------------------------------------------------------------------
CREATE TABLE assignments (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  topic_id        uuid REFERENCES topics(id) ON DELETE SET NULL,
  title           text NOT NULL,
  description     text,
  due_date        timestamptz,
  points          int NOT NULL DEFAULT 100 CHECK (points > 0),
  created_by      uuid NOT NULL REFERENCES users(id),
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_assignments_class ON assignments(class_id);
CREATE INDEX idx_assignments_due ON assignments(due_date) WHERE due_date IS NOT NULL;
CREATE TRIGGER trg_assignments_updated BEFORE UPDATE ON assignments
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Entregas
-- ---------------------------------------------------------------------------
CREATE TABLE submissions (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  assignment_id   uuid NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
  student_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content         text,                     -- respuesta escrita
  file_url        text,
  status          varchar(20) NOT NULL DEFAULT 'draft'
                    CHECK (status IN ('draft', 'submitted', 'graded')),
  submitted_at    timestamptz,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now(),
  UNIQUE (assignment_id, student_id),
  -- Una entrega enviada o calificada tiene necesariamente fecha de envío
  CHECK (status = 'draft' OR submitted_at IS NOT NULL)
);
CREATE INDEX idx_submissions_student ON submissions(student_id);
CREATE INDEX idx_submissions_assignment ON submissions(assignment_id);
CREATE TRIGGER trg_submissions_updated BEFORE UPDATE ON submissions
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Calificaciones
-- ---------------------------------------------------------------------------
CREATE TABLE grades (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  submission_id   uuid NOT NULL UNIQUE REFERENCES submissions(id) ON DELETE CASCADE,
  score           numeric(5,2) NOT NULL CHECK (score >= 0),
  feedback        text,
  graded_by       uuid NOT NULL REFERENCES users(id),
  graded_at       timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_grades_updated BEFORE UPDATE ON grades
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Muro de anuncios de la clase
-- ---------------------------------------------------------------------------
CREATE TABLE announcements (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  author_id       uuid NOT NULL REFERENCES users(id),
  content         text NOT NULL,
  pinned          boolean NOT NULL DEFAULT false,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_announcements_class ON announcements(class_id, pinned DESC, created_at DESC);
CREATE TRIGGER trg_announcements_updated BEFORE UPDATE ON announcements
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Materiales y apuntes
-- ---------------------------------------------------------------------------
CREATE TABLE resources (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  topic_id        uuid REFERENCES topics(id) ON DELETE SET NULL,
  title           text NOT NULL,
  file_url        text,
  type            varchar(20) CHECK (type IN ('pdf', 'link', 'video', 'doc', 'other')),
  sort_order      int NOT NULL DEFAULT 0,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_resources_class ON resources(class_id, sort_order);
CREATE TRIGGER trg_resources_updated BEFORE UPDATE ON resources
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Eventos de calendario introducidos a mano (exámenes, festivos, salidas...).
-- Las entregas NO se guardan aquí: se derivan de assignments.due_date.
-- ---------------------------------------------------------------------------
CREATE TABLE calendar_events (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id          uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  class_id        uuid REFERENCES classes(id) ON DELETE CASCADE,  -- NULL = todo el centro
  title           text NOT NULL,
  event_date      timestamptz NOT NULL,
  type            varchar(20) CHECK (type IN ('exam', 'holiday', 'other')),
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_calendar_org_date ON calendar_events(org_id, event_date);
CREATE TRIGGER trg_calendar_updated BEFORE UPDATE ON calendar_events
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Notificaciones
-- ---------------------------------------------------------------------------
CREATE TABLE notifications (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type            varchar(40) NOT NULL,
  payload         jsonb,
  read_at         timestamptz,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_unread ON notifications(user_id, created_at DESC) WHERE read_at IS NULL;
