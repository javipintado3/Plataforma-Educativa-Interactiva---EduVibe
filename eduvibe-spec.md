# Eduvibe — Especificación de producto

Plataforma educativa que combina lo mejor de Google Classroom (simplicidad, sin fricción) y Moodle (estructura, calificaciones, materiales). Este documento cubre: modelo de alta de usuarios, esquema de base de datos y diseño de la aplicación por rol.

---

## 1. Modelo de alta de usuarios

**Principio central: no existe registro público.** Todas las cuentas nacen desde un admin. Esto replica cómo funciona realmente Google Classroom (las cuentas ya existen en Google Workspace, gestionadas por el admin del dominio del centro) y cómo debería configurarse Moodle en producción (autorregistro desactivado).

### Flujo

1. **Admin crea el usuario** — nombre, email institucional, rol y clase, individualmente o por carga masiva (CSV/Excel).
2. **Sistema genera una invitación** — token de un solo uso, firmado, con expiración (48h por defecto).
3. **Email automático al usuario** — contiene únicamente el enlace de invitación. Nunca se envía una contraseña en texto plano.
4. **Usuario establece su contraseña** — al abrir el enlace, define su propia contraseña. El token se consume (un solo uso).
5. **Cuenta activa, acceso habilitado** — el login solo funciona para cuentas en estado `active`. `pending` y `disabled` quedan bloqueadas.

### Reglas de seguridad

- El link de invitación es un token firmado de un solo uso, nunca una contraseña temporal enviada por email.
- Si el token expira, el admin **reenvía la invitación** — nunca se crea un usuario duplicado.
- "Olvidé mi contraseña" solo opera sobre cuentas ya existentes; nunca crea cuentas nuevas (para que no se convierta en una puerta trasera de registro).
- Opcional (recomendado a futuro): login federado con Google Workspace / Microsoft 365 restringido al dominio del centro, para no gestionar contraseñas en absoluto.
- Restricción de dominio: si `organizations.allowed_domain` está definido, solo se pueden dar de alta emails de ese dominio.

### Roles

| Rol | Puede |
|---|---|
| `admin` | Dar de alta/baja usuarios, crear clases, gestionar la organización |
| `teacher` | Crear tareas, materiales, avisos, calificar, ver sus clases |
| `student` | Ver sus clases, entregar tareas, ver sus notas |
| `guardian` (opcional) | Ver el progreso de un alumno vinculado, sin poder editar nada |

---

## 2. Esquema de base de datos

DDL en sintaxis PostgreSQL, pensado para implementarse tal cual (ajustar si se usa otro motor). **El código real se aparta de este DDL en varios puntos deliberados** (enumerados como `varchar`+`CHECK`, `topics` en vez de `units`, exámenes con modelo propio...): ver "Divergencias entre esta spec y el código" al final de esta sección.

```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "citext";

CREATE TYPE user_role AS ENUM ('admin', 'teacher', 'student', 'guardian');
CREATE TYPE user_status AS ENUM ('pending', 'active', 'disabled');
CREATE TYPE enrollment_role AS ENUM ('teacher', 'student');
CREATE TYPE submission_status AS ENUM ('draft', 'submitted', 'graded');
-- 'late' NO es un estado: se calcula (submitted_at > assignments.due_date), para que una
-- entrega pueda estar 'graded' y tarde a la vez sin conflicto de estados.

-- Centro / colegio / organización
CREATE TABLE organizations (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name            text NOT NULL,
  allowed_domain  text UNIQUE,              -- ej. 'iesmadrid.edu'; NULL = sin restricción
  created_at      timestamptz NOT NULL DEFAULT now()
);

-- Usuarios (admin, profesor, alumno, tutor)
CREATE TABLE users (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id          uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  email           citext NOT NULL,
  name            text NOT NULL,
  role            user_role NOT NULL DEFAULT 'student',
  status          user_status NOT NULL DEFAULT 'pending',
  password_hash   text,                     -- NULL hasta que el usuario lo establece (paso 4 del flujo)
  totp_secret     text,                     -- 2FA (TOTP); NULL = no activado. Va aquí, no en un ALTER aparte.
  totp_enabled    boolean NOT NULL DEFAULT false,
  created_at      timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, email)
);
CREATE INDEX idx_users_org ON users(org_id);

-- Vínculo tutor/padre <-> alumno (un guardian puede seguir a varios alumnos, y viceversa)
CREATE TABLE guardianships (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  guardian_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  student_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at      timestamptz NOT NULL DEFAULT now(),
  UNIQUE (guardian_id, student_id)
);
CREATE INDEX idx_guardianships_student ON guardianships(student_id);
-- El guardian NO es un alumno con permisos reducidos: es un rol de solo lectura sobre los
-- alumnos vinculados aquí. Cualquier servicio que resuelva "qué puede ver este usuario" debe
-- consultar esta tabla para el rol 'guardian', no tratarlo como si fuera 'student'.

-- Invitaciones (token de alta, un solo uso)
CREATE TABLE invitations (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token           text NOT NULL UNIQUE,
  expires_at      timestamptz NOT NULL,
  used_at         timestamptz,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_invitations_pending ON invitations(expires_at) WHERE used_at IS NULL;

CREATE TYPE class_view_mode AS ENUM ('structured', 'flexible');

-- Clases
CREATE TABLE classes (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id          uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  -- Sin teacher_id: el profesorado de una clase (titular + auxiliares) se resuelve por
  -- enrollments con role_in_class = 'teacher'. Una clase puede tener varios profesores.
  name            text NOT NULL,
  subject         text,
  color           text,                     -- para la franja de color de la tarjeta en el dashboard
  view_mode       class_view_mode NOT NULL DEFAULT 'structured',
                                            -- 'structured': temas con actividades/materiales separados (primaria/secundaria)
                                            -- 'flexible': todo mezclado dentro del tema, estilo Blackboard (universidad)
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_classes_org ON classes(org_id);

-- Temas/unidades/módulos (el mismo concepto sirve para "Tema 1: Sumas" o "Módulo 3: Estructuras de datos")
CREATE TABLE units (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  title           text NOT NULL,
  weight          numeric(5,2) NOT NULL DEFAULT 1.00 CHECK (weight > 0),
                                            -- Peso RELATIVO de esta unidad sobre la nota final (no un % que deba sumar 100).
                                            -- Por defecto 1 ("peso normal", igual que assignments.weight en el código): una
                                            -- unidad nueva entra en la media desde el primer momento y no queda fuera en
                                            -- silencio por tener peso 0.
                                            -- Nota final del curso = suma(nota_unidad × weight) / suma(weight), sobre las
                                            -- unidades que TIENEN nota. Una unidad no tiene nota si no tiene ninguna actividad
                                            -- calificada (vacía, o con todo aún pendiente de corregir): queda fuera del
                                            -- cálculo y se renormaliza, nunca cuenta como 0.
                                            -- Las actividades sin unidad (unit_id NULL) forman una unidad implícita
                                            -- "Sin tema" con weight 1, para que no se pierdan de la media.
  drop_lowest     int NOT NULL DEFAULT 0 CHECK (drop_lowest >= 0),
                                            -- Cuenta solo las (total − N) mejores actividades ponderables de la unidad, donde
                                            -- total incluye las aún sin calificar: "de 5 entregas cuenta la media de las 4
                                            -- mejores" = drop_lowest 1. Se descarta max(0, calificadas − (total − N)): hasta
                                            -- que haya más notas que ese número no se descarta nada, así al principio del
                                            -- curso (2 de 5 corregidas) no se tira la única nota mala que existe. N debe ser
                                            -- menor que el total, para que la unidad nunca se quede sin ninguna nota.
                                            -- "La peor" se decide por % obtenido (score/points), no por nota absoluta, y tras
                                            -- descartar se renormaliza sobre el weight de las actividades restantes.
  sort_order      int DEFAULT 0,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_units_class ON units(class_id);

-- Matriculaciones (alumno o profesor <-> clase)
CREATE TABLE enrollments (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role_in_class   enrollment_role NOT NULL,
  enrolled_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (class_id, user_id)
);
CREATE INDEX idx_enrollments_user ON enrollments(user_id);

-- Tareas, exámenes y trabajos (todo lo que puede llevar nota)
CREATE TABLE assignments (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  unit_id         uuid REFERENCES units(id) ON DELETE SET NULL,  -- NULL = no asignado a ningún tema
  title           text NOT NULL,
  type            text NOT NULL DEFAULT 'task',  -- 'task' | 'exam' | 'project' | 'quiz'
  description     text,
  due_date        timestamptz,
  points          int DEFAULT 100,
  weight          numeric(5,2) NOT NULL DEFAULT 1.00 CHECK (weight > 0),
                                            -- Peso RELATIVO dentro de su unidad (mismo criterio que units.weight: por defecto 1,
                                            -- no un % que deba sumar 100). Solo entran en el cálculo las actividades con
                                            -- counts_toward_grade = true Y con nota puesta (grades existente): lo no calificado
                                            -- se EXCLUYE y se renormaliza, nunca cuenta como 0. Para sacar una actividad de la
                                            -- media se usa counts_toward_grade = false, no weight = 0.
  counts_toward_grade boolean NOT NULL DEFAULT true,
                                            -- false = cuestionario/actividad de repaso, no entra en la media del tema
  allow_peer_review    boolean NOT NULL DEFAULT false,
                                            -- true = habilita evaluación por pares para esta actividad (ver peer_reviews)
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_assignments_class ON assignments(class_id);
CREATE INDEX idx_assignments_unit ON assignments(unit_id);

-- Entregas
CREATE TABLE submissions (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  assignment_id   uuid NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
  student_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  file_url        text,
  status          submission_status NOT NULL DEFAULT 'draft',
  submitted_at    timestamptz,
  UNIQUE (assignment_id, student_id)
);
CREATE INDEX idx_submissions_student ON submissions(student_id);

-- Notas
CREATE TABLE grades (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  submission_id   uuid NOT NULL UNIQUE REFERENCES submissions(id) ON DELETE CASCADE,
  score           numeric(5,2),
  feedback        text,
  graded_by       uuid NOT NULL REFERENCES users(id),
  graded_at       timestamptz NOT NULL DEFAULT now()
);

-- Muro de anuncios de la clase (estilo stream de Classroom)
CREATE TABLE announcements (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  author_id       uuid NOT NULL REFERENCES users(id),
  content         text NOT NULL,
  pinned          boolean NOT NULL DEFAULT false,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_announcements_class ON announcements(class_id);

-- Materiales / apuntes (estilo recursos de Moodle)
CREATE TABLE resources (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  unit_id         uuid REFERENCES units(id) ON DELETE SET NULL,  -- NULL = no asignado a ningún tema
  title           text NOT NULL,
  file_url        text,
  type            text,                     -- 'pdf' | 'link' | 'video' | 'doc' | 'quiz'...
  sort_order      int DEFAULT 0,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_resources_class ON resources(class_id);
CREATE INDEX idx_resources_unit ON resources(unit_id);

-- Eventos de calendario (entregas + eventos manuales del profe/admin)
CREATE TABLE calendar_events (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  org_id          uuid NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
  class_id        uuid REFERENCES classes(id) ON DELETE CASCADE,  -- NULL = evento de todo el centro
  title           text NOT NULL,
  event_date      timestamptz NOT NULL,
  type            text,                     -- 'assignment_due' | 'exam' | 'holiday' | 'other'
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_calendar_org ON calendar_events(org_id);

-- Notificaciones (nueva tarea, nota publicada, aviso...)
CREATE TABLE notifications (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type            text NOT NULL,
  payload         jsonb,
  read_at         timestamptz,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user ON notifications(user_id) WHERE read_at IS NULL;

-- Progreso de finalización por actividad/material (barra de progreso del tema)
CREATE TABLE completions (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  item_type       text NOT NULL,            -- 'assignment' | 'resource'
  item_id         uuid NOT NULL,             -- referencia polimórfica a assignments.id o resources.id
  completed_at    timestamptz NOT NULL DEFAULT now(),
  auto_completed  boolean NOT NULL DEFAULT false,  -- true = se marcó solo al entregar/aprobar; false = manual
  UNIQUE (user_id, item_type, item_id)
);
CREATE INDEX idx_completions_user ON completions(user_id);

-- Banco de preguntas reutilizable (para exámenes con selección aleatoria)
CREATE TABLE question_banks (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  title           text NOT NULL,
  created_at      timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE questions (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  bank_id         uuid NOT NULL REFERENCES question_banks(id) ON DELETE CASCADE,
  type            text NOT NULL,            -- 'multiple_choice' | 'true_false' | 'short_answer' | 'matching'
  prompt          text NOT NULL,
  options         jsonb,                    -- opciones/respuestas correctas según el tipo
  points          int DEFAULT 1,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_questions_bank ON questions(bank_id);

-- Configuración de examen: cuántas preguntas sacar del banco, tiempo, intentos
CREATE TABLE exam_configs (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  assignment_id   uuid NOT NULL UNIQUE REFERENCES assignments(id) ON DELETE CASCADE,
  bank_id         uuid REFERENCES question_banks(id),
  questions_per_attempt int,                -- ej. 10 de un banco de 30; NULL = usa todas las del banco
  time_limit_minutes    int,
  max_attempts          int DEFAULT 1,
  shuffle_questions     boolean NOT NULL DEFAULT true,
  shuffle_answers       boolean NOT NULL DEFAULT true
);

-- Preguntas concretas asignadas a cada intento de un alumno (subset aleatorio del banco)
CREATE TABLE exam_attempts (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  assignment_id   uuid NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
  student_id      uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  question_ids    uuid[] NOT NULL,          -- preguntas concretas sorteadas para este intento, en el orden mostrado
  started_at      timestamptz NOT NULL DEFAULT now(),
  submitted_at    timestamptz,
  score           numeric(5,2)
);
CREATE INDEX idx_exam_attempts_student ON exam_attempts(student_id);

-- Adaptaciones por alumno en un examen (tiempo extra, necesidades educativas especiales)
CREATE TABLE exam_accommodations (
  id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  assignment_id         uuid NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
  student_id            uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  extra_time_percent    int NOT NULL DEFAULT 0,   -- ej. 30 = 30% más tiempo sobre time_limit_minutes
  notes                 text,                      -- motivo, visible solo para profesor/admin
  created_by            uuid NOT NULL REFERENCES users(id),
  created_at            timestamptz NOT NULL DEFAULT now(),
  UNIQUE (assignment_id, student_id)
);

-- Evaluación por pares: emparejamiento de entregas para corrección entre alumnos
CREATE TYPE peer_review_status AS ENUM ('assigned', 'submitted', 'overdue');

CREATE TABLE peer_reviews (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  assignment_id   uuid NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
  submission_id   uuid NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,  -- entrega que se está corrigiendo
  reviewer_id     uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,        -- alumno que corrige
  score           numeric(5,2),
  feedback        text,
  status          peer_review_status NOT NULL DEFAULT 'assigned',
  due_at          timestamptz,
  submitted_at    timestamptz,
  UNIQUE (submission_id, reviewer_id)
);
CREATE INDEX idx_peer_reviews_reviewer ON peer_reviews(reviewer_id);

-- Intentos de login fallidos (rate limiting)
CREATE TABLE login_attempts (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email           citext NOT NULL,
  ip_address      inet,
  succeeded       boolean NOT NULL,
  attempted_at    timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_login_attempts_email ON login_attempts(email, attempted_at);

-- (2FA ya va en la tabla users, más arriba — no como ALTER suelto)

-- RGPD: solicitudes de exportación/borrado de datos
CREATE TYPE gdpr_request_type AS ENUM ('export', 'erase');
CREATE TYPE gdpr_request_status AS ENUM ('pending', 'completed', 'rejected');

CREATE TABLE gdpr_requests (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id         uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type            gdpr_request_type NOT NULL,
  status          gdpr_request_status NOT NULL DEFAULT 'pending',
  -- 'erase': NO es un borrado ni una anonimización totales (un expediente académico anonimizado
  -- no sirve para nada, el centro tiene que poder emitir certificados con nombre real). Se borra
  -- todo lo que NO forma parte del expediente obligatorio (datos de contacto, IP, historial de
  -- login, preferencias...) y se conserva identificable solo lo que la ley obliga a conservar
  -- (notas, entregas evaluadas) durante el plazo legal, tras el cual sí se purga. Ver sección 4.11.
  -- *No soy asesor legal — esto hay que validarlo con quien lleve la protección de datos del centro.*
  requested_at    timestamptz NOT NULL DEFAULT now(),
  resolved_at     timestamptz
);
```

**Nota sobre `exam_accommodations.notes`**: puede contener necesidades educativas especiales, que son datos de categoría especial (salud). Requiere control de acceso restringido (solo profesor/admin de esa clase) y registro de quién accede, no solo quién lo crea.

**Nota sobre `login_attempts`**: guarda email + IP; necesita una política de retención explícita (ej. purga automática pasados 90 días) — no puede acumularse indefinidamente ni queda cubierto por el flujo de borrado de `gdpr_requests` tal y como está descrito arriba.

### Relaciones (resumen)

```
organizations 1───N users
users         1───N invitations
users         1───N guardianships      (como guardian)
users         1───N guardianships      (como student)
classes       1───N enrollments
users         1───N enrollments        (como alumno o profesor)
classes       1───N units              (temas/módulos)
units         1───N assignments        (opcional: assignment puede no tener unit_id)
classes       1───N assignments
assignments   1───N submissions
users         1───N submissions        (como alumno)
submissions   1───1 grades
classes       1───N announcements
units         1───N resources          (opcional: resource puede no tener unit_id)
classes       1───N resources
organizations 1───N calendar_events
classes       1───N calendar_events    (opcional)
users         1───N notifications
users         1───N completions
classes       1───N question_banks
question_banks 1───N questions
assignments   1───1 exam_configs       (opcional)
assignments   1───N exam_attempts
users         1───N exam_attempts      (como alumno)
assignments   1───N exam_accommodations
users         1───N exam_accommodations (como alumno)
submissions   1───N peer_reviews
users         1───N peer_reviews       (como reviewer)
users         1───N gdpr_requests
```

### Divergencias entre esta spec y el código (a 2026-09-29)

Fuente de verdad del código: `src-api/EduvibeBackend/src/main/resources/db/migration/` (V1–V13). Los comentarios de migraciones y commits que dicen "Fase N" corresponden a "Bloque N" de esta spec.

**Decisiones del código que se apartan del DDL de arriba** (documentadas en `V1__esquema_base.sql`):

- Enumerados como `varchar` + `CHECK`, no `CREATE TYPE ... AS ENUM`: añadir un valor a un ENUM nativo no se puede hacer dentro de la transacción en la que Flyway envuelve cada migración.
- `email` como `varchar(255)` normalizado a minúsculas en la aplicación, no `citext`; y **único global** (`V2`), no solo por organización, porque el login pide email y contraseña sin elegir centro.
- `invitations.token_hash` en lugar de `token`: se guarda el hash, nunca el token en claro.
- `topics` / `topic_id` en lugar de `units` / `unit_id`: mismo concepto, nombre por unificar (esta spec usa `units`).
- `calendar_events` solo guarda eventos manuales; las fechas de entrega se derivan de `assignments.due_date` para no tener el dato en dos sitios.
- `updated_at` + trigger `set_updated_at()` en casi todas las tablas.
- **Exámenes con modelo propio**, separado de `assignments`/`submissions`/`grades`: `exams`, `exam_questions`, `exam_options`, `exam_attempts`, `exam_answers` (abajo). No existe `exam_attempts.question_ids uuid[]`; hay **un solo intento** por alumno y examen (`UNIQUE (exam_id, student_id)`); `exam_answers` guarda la opción elegida por pregunta. El banco reutilizable se implementa copiando preguntas de otros exámenes de la clase, sin tablas `question_banks`/`questions`.

**Piezas que ya existen y esta spec no recogía** (DDL real, tal cual está en las migraciones):

```sql
-- Columnas añadidas a tablas existentes
ALTER TABLE assignments ADD COLUMN late_penalty_percent integer NOT NULL DEFAULT 0
  CHECK (late_penalty_percent BETWEEN 0 AND 100);                       -- V7
ALTER TABLE assignments ADD COLUMN weight numeric(4,2) NOT NULL DEFAULT 1.00
  CHECK (weight > 0);                                                   -- V9 (hoy: peso sobre TODA la clase)
ALTER TABLE assignments ADD COLUMN group_assignment boolean NOT NULL DEFAULT false;  -- V12
ALTER TABLE submissions ADD COLUMN teacher_note text;                   -- V6 (nota rápida, sin calificar)
ALTER TABLE submissions ADD COLUMN class_group_id uuid
  REFERENCES class_groups(id) ON DELETE SET NULL;                       -- V12 (null salvo en tareas grupales)
-- submissions.content text (respuesta escrita) ya está en V1; además:
--   CHECK (status = 'draft' OR submitted_at IS NOT NULL)
ALTER TABLE grades ADD COLUMN raw_score numeric(5,2) NOT NULL;          -- V7: la nota tal cual, antes del descuento
ALTER TABLE grades ADD COLUMN late_penalty_applied boolean NOT NULL DEFAULT false;  -- V7
ALTER TABLE resources ADD COLUMN available_from timestamptz;            -- V8 (null = visible desde que se publica)
ALTER TABLE classes ADD COLUMN image_url text;                          -- V3
ALTER TABLE users ADD COLUMN avatar_url text;                           -- V4

-- V12: subgrupos reutilizables (trabajo en equipo). En una tarea grupal, todas las entregas
-- del mismo subgrupo comparten contenido y nota, pero cada alumno conserva su propia fila.
CREATE TABLE class_groups (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  name text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE class_group_members (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  group_id uuid NOT NULL REFERENCES class_groups(id) ON DELETE CASCADE,
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE (group_id, user_id)
);

-- V10: rúbricas. Una tarea tiene como mucho una; con rúbrica, la nota es la suma de los criterios.
CREATE TABLE rubrics (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  assignment_id uuid NOT NULL UNIQUE REFERENCES assignments(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE rubric_criteria (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  rubric_id uuid NOT NULL REFERENCES rubrics(id) ON DELETE CASCADE,
  description text NOT NULL,
  max_points numeric(5,2) NOT NULL CHECK (max_points > 0),
  sort_order int NOT NULL DEFAULT 0
);
CREATE TABLE rubric_scores (            -- cuelga de la nota (grades), no de la entrega
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  grade_id uuid NOT NULL REFERENCES grades(id) ON DELETE CASCADE,
  criterion_id uuid NOT NULL REFERENCES rubric_criteria(id) ON DELETE CASCADE,
  points numeric(5,2) NOT NULL CHECK (points >= 0),
  UNIQUE (grade_id, criterion_id)
);

-- V11: foros de debate por tema. El primer mensaje de un hilo es un forum_post más.
CREATE TABLE forum_threads (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  topic_id uuid REFERENCES topics(id) ON DELETE SET NULL,
  author_id uuid NOT NULL REFERENCES users(id),
  title text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE forum_posts (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  thread_id uuid NOT NULL REFERENCES forum_threads(id) ON DELETE CASCADE,
  author_id uuid NOT NULL REFERENCES users(id),
  content text NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

-- V5: exámenes de opción múltiple con corrección automática y tiempo límite
CREATE TABLE exams (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  topic_id uuid REFERENCES topics(id) ON DELETE SET NULL,
  title text NOT NULL,
  description text,
  duration_minutes int NOT NULL CHECK (duration_minutes > 0),
  due_date timestamptz,
  created_by uuid NOT NULL REFERENCES users(id),
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE exam_questions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  exam_id uuid NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
  text text NOT NULL,
  points int NOT NULL DEFAULT 1 CHECK (points > 0),
  sort_order int NOT NULL DEFAULT 0
);
CREATE TABLE exam_options (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  question_id uuid NOT NULL REFERENCES exam_questions(id) ON DELETE CASCADE,
  text text NOT NULL,
  correct boolean NOT NULL DEFAULT false,
  sort_order int NOT NULL DEFAULT 0
);
CREATE TABLE exam_attempts (            -- sin estado guardado: "en curso" = submitted_at IS NULL
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  exam_id uuid NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
  student_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  started_at timestamptz NOT NULL DEFAULT now(),
  submitted_at timestamptz,
  score numeric(5,2),
  UNIQUE (exam_id, student_id)
);
CREATE TABLE exam_answers (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  attempt_id uuid NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE,
  question_id uuid NOT NULL REFERENCES exam_questions(id) ON DELETE CASCADE,
  selected_option_id uuid REFERENCES exam_options(id) ON DELETE SET NULL,  -- SET NULL: retocar una opción no borra respuestas
  UNIQUE (attempt_id, question_id)
);
```

**Está en esta spec y todavía no existe en el código:**

- `units.weight`, `units.drop_lowest` y `assignments.counts_toward_grade`: la ponderación en dos niveles. Hoy `assignments.weight` normaliza sobre toda la clase, sin unidades de por medio.
- `guardianships` (hoy `guardian` existe como rol pero se trata como alumno, lo cual es incorrecto), `completions`, `login_attempts` (el Bloque 1 lo exige y no está), `totp_secret`/`totp_enabled`, `exam_accommodations`, `peer_reviews`, `gdpr_requests`.
- `exam_configs` y la selección aleatoria de un subconjunto de preguntas por intento (hoy solo se baraja el orden).

**Decisión abierta — exámenes y ponderación.** La nota de un examen vive en `exam_attempts.score`, no en `grades`, así que **hoy un examen no entra** en la media por unidad/curso ni en la sección "Notas" del alumno. Para que pese como una actividad más hace falta darle `weight` y `counts_toward_grade` (ya tiene `topic_id`) y que el cálculo lea de las dos fuentes (tarea calificada | examen entregado) como una lista común de elementos con `score`, `points` y `weight`, sin tener que unificar las tablas.

**Migración de pesos existentes.** El `assignments.weight` actual (default 1, `> 0`) es compatible con la semántica nueva: cada peso conserva su valor relativo. Lo que cambia es el ámbito de la normalización (antes toda la clase, ahora dentro de la unidad y luego entre unidades), así que la nota final de una clase con varias unidades y pesos distintos puede variar al migrar; hay que avisarlo en la pantalla de configuración.

---

## 3. Diseño de la aplicación

### Navegación por rol (navbar)

Secciones en la barra principal, por rol. "Perfil", "Ajustes" y "Cerrar sesión" van en el menú del avatar (no ocupan sección propia en el navbar), y la campana de notificaciones es un icono aparte, no una sección.

**Alumno**
- Mis clases *(home)*
- Calendario
- Notas

**Profesor**
- Mis clases
- Calendario
- Entregas pendientes *(acceso directo a lo que tiene sin corregir, cruzando todas sus clases; distinto de "Calificaciones", que es la tabla completa dentro de cada clase — no fusionar los dos conceptos en una sola sección)*

**Admin**
- Usuarios (alta individual + carga masiva CSV, activar/desactivar, reenviar invitación)
- Clases y matriculaciones *(nombre fijado — no "Clases" a secas, para que quede claro que incluye gestión de matrículas)*
- Organización (dominio permitido, roles, ajustes generales)

**Tutor/padre** (opcional)
- Resumen del alumno *(vista de solo lectura)*
- Calendario

El navbar es el mismo en modo `structured` y `flexible` — el cambio de vista ocurre dentro de la clase, no a nivel de navegación global.

### Pantalla principal del alumno — "Mis clases"

Grid de tarjetas, una por clase, con:
- Franja de color superior identificando la asignatura (igual que Classroom — reconocimiento visual instantáneo).
- Nombre de la clase + profesor.
- Línea inferior de estado: próxima entrega con fecha, o "sin tareas pendientes" — esto es lo que Moodle no da de un vistazo y Classroom sí.
- Accesos rápidos arriba: Calendario, Notas.

### Pantalla de una clase (al entrar) — modelo híbrido

**Principio central: mismo modelo de datos, dos vistas según `classes.view_mode`.** No es una elección global de la plataforma — se configura por clase, así un colegio de primaria y una universidad conviven en la misma app sin duplicar nada por debajo.

**Modo `structured` (por defecto — primaria/secundaria):**

Al entrar en la clase se ven directamente los **Temas** (`units`), no un muro. Al entrar en un tema, dos bloques separados:
- **Actividades** — exámenes, trabajos y tareas (`assignments` con `counts_toward_grade = true`), y la media del tema calculada a partir de estas.
- **Materiales** — apuntes y cuestionarios de repaso (`resources`, y `assignments` con `counts_toward_grade = false` para cuestionarios que no puntúan).

El **Muro** (anuncios generales, no ligados a un tema) pasa a ser una pestaña aparte, "Avisos" — ya no es la pantalla principal.

**Modo `flexible` (universidad):**

Al entrar en la clase se ven **Módulos** (mismo registro `units`, solo cambia el nombre en la interfaz). Dentro de cada módulo, todo el contenido aparece en una sola lista mezclada (documentos, tareas, exámenes, foros) — igual que Blackboard, para profesores que prefieren libertad total de organización.

**Común a ambos modos:**
- **Alumnos**: lista de matriculados (solo visible para profesor/admin).
- **Calificaciones / Grade Centre**: notas de esa clase. El alumno ve las suyas; el profesor ve la tabla completa (todas las columnas × todos los alumnos, estilo Grade Centre de Blackboard), disponible en ambos modos aunque se use más en `flexible`.
- El toggle `counts_toward_grade` en cada actividad es independiente del modo de vista — un cuestionario puede puntuar o no en cualquiera de los dos.

### Panel de admin — Usuarios

- Tabla de usuarios: nombre, email, rol, clase(s), estado (`pending` / `active` / `disabled`), con filtro por rol y estado.
- Botón "Nuevo usuario": formulario con nombre, email, rol, clase.
- Botón "Importar CSV": sube archivo, valida duplicados y formato antes de confirmar el alta masiva.
- Acción por fila: reenviar invitación (si `pending`), desactivar (si `active`), reactivar (si `disabled`).
- Nunca hay botón "eliminar" definitivo desde aquí — solo desactivar (soft delete), para no perder histórico académico.

### Pantalla "Configuración de calificaciones" (profesor/admin)

Ponderación en dos niveles, inspirada en el gradebook setup de Moodle pero simplificada:

- Árbol: Curso → Unidades → Actividades ponderables (`counts_toward_grade = true`).
- Cada fila: nombre, input de peso relativo (`weight`, por defecto 1), **porcentaje efectivo** calculado en vivo al lado (ej. "Unidad 1 · peso 2 · 40 %"), nota máxima, icono de reordenar (arrastrar).
- El input de peso se deshabilita si la unidad/actividad todavía no tiene contenido que ponderar (nada que ponderar = nada que editar).
- Fila "Total [Unidad]" en negrita con la suma ponderada de esa unidad.
- Fila "Total del curso" al final.
- **Sin validación de "suma 100 %"**: los pesos son relativos y el porcentaje efectivo se recalcula al editar, así no hay nada que cuadrar. En su lugar, un aviso informativo (no bloqueante) marca lo que queda fuera del cálculo —una unidad vacía o sin nada calificado— para que el profesor no se sorprenda de que "no cuente".
- Opción por unidad: "Descartar las N peores notas" (`units.drop_lowest`) — selector numérico limitado a 0…(actividades ponderables − 1), no un campo suelto.
- A diferencia de Moodle, no se mezclan nombres inconsistentes ("Unidad 1", "Tema 2"...) — el nombre viene siempre de `units.title`, un único campo.

### Progreso por tema

- Cada actividad/material tiene una marca de completado (`completions`), automática al entregar una tarea o manual (checkbox) para materiales de solo lectura.
- Barra de progreso visible en la cabecera del tema: % de actividades+materiales completados.
- Se agrega también a nivel de clase, en la tarjeta de "Mis clases" del alumno (ej. "Tema 3 de 5 completado").

### Evaluación por pares (peer review) — v1 simplificada

Para no replicar de golpe la complejidad completa del módulo "Taller" de Moodle:

- El profesor activa `assignments.allow_peer_review` y define cuántas entregas corrige cada alumno (típicamente 2-3).
- El sistema empareja automáticamente entregas↔revisores (`peer_reviews`), evitando que nadie se autoevalúe.
- **Sin anonimización en v1** — el revisor ve de quién es la entrega. Es una simplificación deliberada: anonimizar añade lógica de invalidación cuando coincide reviewer y clase pequeña, mejor dejarlo para una v2 si hace falta.
- Nota final de la actividad = media simple entre la nota del profesor y la media de las revisiones de pares, con el profesor pudiendo sobrescribir manualmente en cualquier momento.
- Si un revisor no entrega su revisión antes de `due_at`, su entrada queda en estado `overdue` y no cuenta en la media — no bloquea la nota del alumno evaluado.

### Adaptaciones y accesibilidad en exámenes

- Por alumno y examen, el profesor puede asignar tiempo extra (`exam_accommodations.extra_time_percent`), visible solo para profesor/admin, nunca para el resto de la clase.
- El banco de preguntas (`question_banks` / `questions`) permite que cada alumno reciba un subconjunto aleatorio distinto del mismo examen (`exam_configs.questions_per_attempt`), no solo el mismo examen con las preguntas reordenadas.

### Seguridad de cuenta

- Rate limiting de login: tras varios intentos fallidos seguidos (`login_attempts`), se bloquea temporalmente ese email/IP.
- 2FA (TOTP) opcional, recomendado obligatorio para roles `admin` y `teacher` dado que la app maneja datos de menores.

### RGPD básico

- "Exportar mis datos": genera un archivo con todos los datos personales y académicos del usuario.
- "Borrar mi cuenta": no es un borrado total ni una anonimización total, y el propio flujo lo explica antes de confirmar. Se borra todo lo que no forma parte del expediente obligatorio (datos de contacto, foto, IP, historial de login, preferencias) y se conservan **identificables** solo las notas y entregas evaluadas durante el plazo legal, pasado el cual también se purgan: un expediente anonimizado no sirve, el centro tiene que poder emitir certificados con nombre real. Los plazos concretos los valida quien lleve la protección de datos del centro (esto no es asesoramiento legal).
- Datos de categoría especial (adaptaciones en exámenes) y retención de `login_attempts`: ver las notas bajo el DDL de la sección 2.

### Convenciones visuales

- Color de asignatura = identidad de la clase en toda la app (tarjeta, calendario, notificaciones).
- Estado de tarea (pendiente / entregada / calificada / atrasada) siempre visible sin entrar al detalle.
- Sin registro público en ningún punto de la interfaz — el login solo tiene campos de email/contraseña y "olvidé mi contraseña", nunca un enlace "crear cuenta".

---

## 4. Funcionalidades completas (Classroom + Moodle + Blackboard)

Listado de referencia con todo lo que puede llegar a tener una plataforma de este tipo. No todo entra en el MVP — ver el roadmap en la sección 5.

### 4.1 Gestión de usuarios y acceso
- Alta por admin, sin registro público
- Roles: admin, profesor, alumno, tutor/padre, coordinador/observador (opcional)
- Multi-organización / multi-centro en la misma plataforma (multi-tenant)
- SSO (Google Workspace, Microsoft 365, SAML)
- Perfil con foto, datos de contacto, preferencias de notificación
- **Rate limiting de login**: bloqueo temporal tras varios intentos fallidos seguidos (`login_attempts`)
- **2FA (TOTP)** opcional, recomendado obligatorio para `admin` y `teacher` por tratarse de una app con datos de menores

### 4.2 Gestión de cursos y clases
- Crear clase con código de asignatura, curso académico, profesor titular y auxiliares
- Matriculación manual, masiva (CSV) o por código de invitación
- Archivar curso al terminar el curso académico (sin borrar histórico)
- Duplicar/clonar curso de un año para el siguiente
- Subgrupos dentro de una clase (trabajos en equipo, desdobles)

### 4.3 Contenido y materiales
- Subir apuntes, PDFs, vídeos, enlaces, presentaciones
- Organización por temas/semanas o cronológica
- Vista previa de documentos sin descargar
- Control de versiones de materiales
- Restricción de acceso a contenido por fecha o por completar otro módulo antes

### 4.4 Tareas y evaluaciones
- Crear tarea con fecha límite, puntuación máxima, adjuntos, rúbrica
- Entrega de archivos, texto enriquecido o enlaces
- Entrega grupal (una entrega para varios alumnos)
- Reutilizar/programar tareas para el año siguiente
- Detección de plagio/similitud (integración tipo Turnitin)
- Penalización automática por entrega tardía

### 4.5 Cuestionarios y exámenes
- Banco de preguntas reutilizable (opción múltiple, V/F, respuesta corta, emparejar)
- **Selección aleatoria por alumno**: cada intento saca un subconjunto distinto del banco (`exam_configs.questions_per_attempt`), no solo el mismo examen reordenado
- Corrección automática de preguntas cerradas
- Temporizador y número de intentos limitado
- Aleatorizar orden de preguntas/respuestas
- **Adaptaciones/accesibilidad**: tiempo extra por alumno y examen (`exam_accommodations`), visible solo para profesor/admin
- Modo examen con bloqueo de navegador

### 4.6 Calificaciones
- Libro de calificaciones centralizado por clase
- Ponderación de notas en dos niveles: unidad → actividad (pantalla de configuración de calificaciones, ver sección 3)
- **Descartar N peores notas por unidad** (`units.drop_lowest`)
- **Evaluación por pares (v1 simplificada)**: emparejamiento automático entregas↔revisores, sin anonimización, nota final = media profesor + media de pares (ver sección 3)
- Rúbricas visibles para el alumno antes de entregar
- Feedback en texto, audio o vídeo sobre la entrega
- Vista alumno (solo sus notas) vs vista profesor (todo el grupo)
- Boletines/informes exportables en PDF por evaluación o trimestre

### 4.7 Comunicación
- Muro/anuncios por clase
- Foros de debate por tema, con hilos y moderación
- Mensajería directa profesor↔alumno, profesor↔tutor
- Comentarios privados en una entrega concreta
- Videollamada integrada o enlace a Meet/Zoom/Teams

### 4.8 Calendario y planificación
- Calendario unificado: entregas, exámenes, eventos del centro
- Vista por clase y vista global del alumno
- Recordatorios automáticos antes de una entrega
- Sincronización con Google Calendar / Outlook

### 4.9 Notificaciones
- Nueva tarea, nueva nota, nuevo comentario, anuncio nuevo
- Configurables por usuario (email, push, in-app)
- Resumen diario/semanal opcional

### 4.10 Analítica y seguimiento
- Panel del profesor: quién ha entregado, quién no, % de la clase
- **Progreso por tema**: barra de completado de actividades+materiales por tema y por clase (`completions`)
- Progreso del alumno en el tiempo (gráfica de notas)
- Alertas de riesgo (alumno con varias entregas sin hacer)
- Panel de dirección/admin: uso de la plataforma por profesor/clase

### 4.11 Administración (backoffice)
- Gestión de usuarios (sección 1 de este documento)
- Gestión de cursos académicos (crear curso 2026-2027, migrar clases)
- Copias de seguridad / exportación de datos
- Auditoría: quién cambió qué y cuándo
- Configuración de organización: dominio permitido, logo, colores, política de contraseñas
- **RGPD**: exportar datos personales del usuario; "borrar cuenta" conserva identificable solo el expediente académico obligatorio y borra el resto (ver sección 3 y el comentario de `gdpr_requests` en la sección 2)

### 4.12 Accesibilidad y multiplataforma
- Responsive / apps móviles
- Modo oscuro
- Lector de pantalla / navegación por teclado
- Multi-idioma

### 4.13 Extras diferenciadores
- Gamificación ligera (insignias, rachas)
- Modo "vista de padres" de solo lectura
- IA integrada para resumir materiales o generar preguntas de repaso

---

## 5. Roadmap por bloques

### Bloque 0 — Cimientos (antes de cualquier feature de producto)
- Modelo de datos base (sección 2 de este documento)
- Autenticación + flujo de invitación (sección 1)
- Estructura de la app y navegación por rol (sección 3)

*Todo lo demás depende de este bloque.*

### Bloque 1 — MVP (Classroom funcional completo)
Cubre 4.1 (básico), 4.2, 4.3, 4.4, 4.6 (básico), 4.7 (muro), 4.9 (básico).

- Alta de usuarios y clases, matriculación manual y por CSV
- Modelo híbrido `view_mode` (`structured`/`flexible`) por clase, con navegación Temas → Actividades/Materiales (sección 3) — es parte del modelo base, no un extra posterior
- Subir materiales (sin versionado ni restricciones de acceso todavía)
- Crear tareas y recibir entregas (archivo o texto)
- Libro de calificaciones simple (nota por tarea, sin ponderación todavía)
- Muro de anuncios por clase (pestaña "Avisos", separado de la vista de temas)
- Notificaciones básicas in-app (nueva tarea, nueva nota)
- Rate limiting de login (protección básica desde el día uno, coste bajo de implementar)

*Con esto ya tienes una plataforma usable en un centro real.*

### Bloque 2 — Estructura tipo Moodle
Depende del Bloque 1.

- Ponderación de notas en dos niveles (unidad → actividad) y rúbricas — pantalla de configuración de calificaciones (sección 3)
- Descartar N peores notas por unidad
- Restricciones de acceso a contenido por fecha/progreso
- Progreso por tema (barra de completado)
- Foros de debate
- Calendario unificado + recordatorios
- Entrega grupal
- Penalización automática por entrega tardía

### Bloque 3 — Evaluación avanzada
Depende del Bloque 2 (usa el mismo libro de calificaciones y calendario).

- Cuestionarios con banco de preguntas y corrección automática
- Selección aleatoria de preguntas por alumno (subset del banco por intento)
- Temporizador, intentos limitados, aleatorización de orden
- Adaptaciones/accesibilidad en exámenes (tiempo extra por alumno)
- Evaluación por pares — v1 simplificada, sin anonimización (mayor riesgo de alcance del bloque; empezar aquí solo si el resto del bloque ya está estable)
- Detección de plagio (integración externa)
- Feedback en audio/vídeo

### Bloque 4 — Escala institucional (nivel Blackboard)
Depende de tener ya varias clases/cursos académicos en producción (Bloques 1-2 estables).

- Multi-organización / multi-centro
- SSO (Google Workspace, Microsoft 365, SAML)
- 2FA (TOTP) para admin/profesorado — natural en este bloque junto con SSO, aunque puede adelantarse si el centro lo pide antes
- Archivado y clonado de cursos entre años académicos
- Auditoría completa y copias de seguridad
- RGPD: exportación de datos y "borrar cuenta" (conserva identificable solo el expediente obligatorio, borra el resto)
- Analítica avanzada y alertas de riesgo
- Modo examen con bloqueo de navegador

### Bloque 5 — Diferenciación
Independiente, se puede intercalar en cualquier bloque anterior según prioridad de negocio.

- Modo "vista de padres"
- Gamificación ligera
- IA integrada para resumir materiales y generar preguntas de repaso
- Apps móviles nativas, modo oscuro, multi-idioma

### Orden de dependencias (resumen)

```
Bloque 0 (cimientos)
   └─> Bloque 1 (MVP)
          └─> Bloque 2 (estructura Moodle)
                 └─> Bloque 3 (evaluación avanzada)
          └─> Bloque 4 (escala institucional)
   └─> Bloque 5 (diferenciación) — intercalable en cualquier momento
```
