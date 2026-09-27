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

DDL en sintaxis PostgreSQL, pensado para implementarse tal cual (ajustar si se usa otro motor).

```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "citext";

CREATE TYPE user_role AS ENUM ('admin', 'teacher', 'student', 'guardian');
CREATE TYPE user_status AS ENUM ('pending', 'active', 'disabled');
CREATE TYPE enrollment_role AS ENUM ('teacher', 'student');
CREATE TYPE submission_status AS ENUM ('draft', 'submitted', 'graded', 'late');

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
  created_at      timestamptz NOT NULL DEFAULT now(),
  UNIQUE (org_id, email)
);
CREATE INDEX idx_users_org ON users(org_id);

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
  teacher_id      uuid NOT NULL REFERENCES users(id),
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
  counts_toward_grade boolean NOT NULL DEFAULT true,
                                            -- false = cuestionario/actividad de repaso, no entra en la media del tema
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
```

### Relaciones (resumen)

```
organizations 1───N users
users         1───N invitations
users         1───N classes            (como profesor, teacher_id)
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
```

---

## 3. Diseño de la aplicación

### Navegación por rol

**Alumno**
- Mis clases (pantalla principal — tarjetas de clase)
- Calendario
- Notas
- Perfil

**Profesor**
- Mis clases
- Calificar entregas
- Crear tarea / material / aviso
- Alumnos (por clase)

**Admin**
- Usuarios (alta individual + carga masiva CSV, activar/desactivar, reenviar invitación)
- Clases y matriculaciones
- Organización (dominio permitido, roles, ajustes generales)

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
- Corrección automática de preguntas cerradas
- Temporizador y número de intentos limitado
- Aleatorizar orden de preguntas/respuestas
- Modo examen con bloqueo de navegador

### 4.6 Calificaciones
- Libro de calificaciones centralizado por clase
- Ponderación de notas (media ponderada por tipo de actividad)
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
- Progreso del alumno en el tiempo (gráfica de notas)
- Alertas de riesgo (alumno con varias entregas sin hacer)
- Panel de dirección/admin: uso de la plataforma por profesor/clase

### 4.11 Administración (backoffice)
- Gestión de usuarios (sección 1 de este documento)
- Gestión de cursos académicos (crear curso 2026-2027, migrar clases)
- Copias de seguridad / exportación de datos
- Auditoría: quién cambió qué y cuándo
- Configuración de organización: dominio permitido, logo, colores, política de contraseñas

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

## 5. Roadmap por fases

### Fase 0 — Cimientos (antes de cualquier feature de producto)
- Modelo de datos base (sección 2 de este documento)
- Autenticación + flujo de invitación (sección 1)
- Estructura de la app y navegación por rol (sección 3)

*Todo lo demás depende de esta fase.*

### Fase 1 — MVP (Classroom funcional completo)
Cubre 4.1, 4.2, 4.3, 4.4, 4.6 (básico), 4.7 (muro), 4.9 (básico).

- Alta de usuarios y clases, matriculación manual y por CSV
- Subir materiales (sin versionado ni restricciones de acceso todavía)
- Crear tareas y recibir entregas (archivo o texto)
- Libro de calificaciones simple (nota por tarea, sin ponderación todavía)
- Muro de anuncios por clase
- Notificaciones básicas in-app (nueva tarea, nueva nota)

*Con esto ya tienes una plataforma usable en un centro real.*

### Fase 2 — Estructura tipo Moodle
Depende de la Fase 1.

- Ponderación de notas y rúbricas
- Restricciones de acceso a contenido por fecha/progreso
- Foros de debate
- Calendario unificado + recordatorios
- Entrega grupal
- Penalización automática por entrega tardía

### Fase 3 — Evaluación avanzada
Depende de la Fase 2 (usa el mismo libro de calificaciones y calendario).

- Cuestionarios con banco de preguntas y corrección automática
- Temporizador, intentos limitados, aleatorización
- Detección de plagio (integración externa)
- Feedback en audio/vídeo

### Fase 4 — Escala institucional (nivel Blackboard)
Depende de tener ya varias clases/cursos académicos en producción (Fase 1-2 estables).

- Multi-organización / multi-centro
- SSO (Google Workspace, Microsoft 365, SAML)
- Archivado y clonado de cursos entre años académicos
- Auditoría completa y copias de seguridad
- Analítica avanzada y alertas de riesgo
- Modo examen con bloqueo de navegador

### Fase 5 — Diferenciación
Independiente, se puede intercalar en cualquier fase anterior según prioridad de negocio.

- Modo "vista de padres"
- Gamificación ligera
- IA integrada para resumir materiales y generar preguntas de repaso
- Apps móviles nativas, modo oscuro, multi-idioma

### Orden de dependencias (resumen)

```
Fase 0 (cimientos)
   └─> Fase 1 (MVP)
          └─> Fase 2 (estructura Moodle)
                 └─> Fase 3 (evaluación avanzada)
          └─> Fase 4 (escala institucional)
   └─> Fase 5 (diferenciación) — intercalable en cualquier momento
```
