-- ===========================================================================
-- Datos de demostración
--
-- Vive en una localización de Flyway aparte (classpath:db/demo) para que en
-- producción baste con no incluirla:
--     FLYWAY_LOCATIONS=classpath:db/migration
--
-- Es una migración repetible (R__) y por tanto se ejecuta después de todas las
-- versionadas, y de nuevo cada vez que cambia su contenido. Por eso cada
-- INSERT es idempotente: identificadores fijos con ON CONFLICT DO NOTHING, o
-- las claves únicas naturales de la tabla.
--
-- Todas las cuentas comparten la contraseña: demo1234
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- Organización
-- ---------------------------------------------------------------------------
INSERT INTO organizations (id, name, allowed_domain) VALUES
  ('00000000-0000-0000-0000-000000000001', 'IES Ribera del Guadaira', NULL)
ON CONFLICT (id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Usuarios. El hash corresponde a 'demo1234' en todos los casos.
-- ---------------------------------------------------------------------------
INSERT INTO users (id, org_id, email, name, role, status, password_hash) VALUES
  -- Administración
  ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'admin@eduvibe.demo',            'Lucía Serrano',      'admin',   'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  -- Profesorado
  ('10000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000001', 'carmen.ortega@eduvibe.demo',    'Carmen Ortega',      'teacher', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000001', 'david.cifuentes@eduvibe.demo',  'David Cifuentes',    'teacher', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000001', 'rafael.tirado@eduvibe.demo',    'Rafael Tirado',      'teacher', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  -- Alumnado
  ('10000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-000000000001', 'marina.vazquez@eduvibe.demo',   'Marina Vázquez',     'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-000000000001', 'enrique.dominguez@eduvibe.demo','Enrique Domínguez',  'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000023', '00000000-0000-0000-0000-000000000001', 'jimena.mojarro@eduvibe.demo',   'Jimena Mojarro',     'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000024', '00000000-0000-0000-0000-000000000001', 'alberto.vazquez@eduvibe.demo',  'Alberto Vázquez',    'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000025', '00000000-0000-0000-0000-000000000001', 'daniel.espinosa@eduvibe.demo',  'Daniel Espinosa',    'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000026', '00000000-0000-0000-0000-000000000001', 'maria.azcarate@eduvibe.demo',   'María Azcárate',     'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000027', '00000000-0000-0000-0000-000000000001', 'andres.saez@eduvibe.demo',      'Andrés Sáez',        'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000028', '00000000-0000-0000-0000-000000000001', 'candela.fernandez@eduvibe.demo','Candela Fernández',  'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000029', '00000000-0000-0000-0000-000000000001', 'irene.gonzalez@eduvibe.demo',   'Irene González',     'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000030', '00000000-0000-0000-0000-000000000001', 'carlos.duran@eduvibe.demo',     'Carlos Durán',       'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000001', 'helena.isnard@eduvibe.demo',    'Helena Isnard',      'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  ('10000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000001', 'ismael.luna@eduvibe.demo',      'Ismael Luna',        'student', 'active',  '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.'),
  -- Una cuenta recién invitada, para que se vea el estado 'pending' en el panel
  ('10000000-0000-0000-0000-000000000040', '00000000-0000-0000-0000-000000000001', 'nuevo.alumno@eduvibe.demo',     'Pablo Reina',        'student', 'pending', NULL),
  -- Y una desactivada, para que se vea el ciclo completo
  ('10000000-0000-0000-0000-000000000041', '00000000-0000-0000-0000-000000000001', 'antigua.alumna@eduvibe.demo',   'Marta Candau',       'student', 'disabled', '$2a$10$eODwrYURyCqI8IclcJlGWevE4rH8UkwVYcfQPKJFhSPqcUbvRzwB.')
ON CONFLICT (id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Clases
-- ---------------------------------------------------------------------------
INSERT INTO classes (id, org_id, name, subject, color) VALUES
  ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '1º Bachillerato A', 'Matemáticas', '#2563eb'),
  ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '1º Bachillerato A', 'Historia',    '#db2777'),
  ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', '2º ESO B',          'Lengua',      '#059669'),
  ('20000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', '2º ESO B',          'Biología',    '#ea580c')
ON CONFLICT (id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Matriculaciones
--
-- Profesorado: Carmen imparte Matemáticas y Biología, David Historia y
-- Rafael Lengua. Alumnado: los doce en las dos de Bachillerato, y repartidos
-- por mitades en las dos de ESO.
-- ---------------------------------------------------------------------------
INSERT INTO enrollments (class_id, user_id, role_in_class) VALUES
  ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000011', 'teacher'),
  ('20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000011', 'teacher'),
  ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000012', 'teacher'),
  ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000013', 'teacher')
ON CONFLICT (class_id, user_id) DO NOTHING;

-- Todo el alumnado activo en las dos clases de Bachillerato
INSERT INTO enrollments (class_id, user_id, role_in_class)
SELECT c.id, u.id, 'student'
FROM users u
CROSS JOIN classes c
WHERE u.role = 'student'
  AND u.status = 'active'
  AND c.id IN ('20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002')
ON CONFLICT (class_id, user_id) DO NOTHING;

-- La primera mitad en Lengua, la segunda en Biología
INSERT INTO enrollments (class_id, user_id, role_in_class)
SELECT CASE WHEN orden <= 6 THEN '20000000-0000-0000-0000-000000000003'::uuid
            ELSE '20000000-0000-0000-0000-000000000004'::uuid END,
       id,
       'student'
FROM (
  SELECT id, row_number() OVER (ORDER BY email) AS orden
  FROM users
  WHERE role = 'student' AND status = 'active'
) AS alumnado
ON CONFLICT (class_id, user_id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Temas
-- ---------------------------------------------------------------------------
INSERT INTO topics (id, class_id, title, sort_order) VALUES
  ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Bloque 1 — Álgebra',              1),
  ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'Bloque 2 — Análisis',             2),
  ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', 'Bloque 1 — El siglo XIX',         1),
  ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', 'Bloque 2 — Entreguerras',         2),
  ('30000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000003', 'Bloque 1 — Comprensión lectora',  1),
  ('30000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000004', 'Bloque 1 — La célula',            1)
ON CONFLICT (id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Tareas. Unas ya vencidas y otras por venir, para que el panel muestre
-- "próxima entrega" con sentido.
-- ---------------------------------------------------------------------------
INSERT INTO assignments (id, class_id, topic_id, title, description, due_date, points, created_by) VALUES
  ('40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Sistemas de ecuaciones',      'Resuelve los ejercicios 1 a 12 de la página 47.',              now() - interval '10 days', 100, '10000000-0000-0000-0000-000000000011'),
  ('40000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Matrices y determinantes',    'Entrega en PDF, con el procedimiento desarrollado.',           now() - interval '3 days',  100, '10000000-0000-0000-0000-000000000011'),
  ('40000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', 'Límites y continuidad',       'Ejercicios del dosier. Se corrige en clase la semana que viene.', now() + interval '6 days',  50, '10000000-0000-0000-0000-000000000011'),
  ('40000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000003', 'Comentario de texto: 1898',   'Máximo dos caras. Sitúa el texto en su contexto histórico.',    now() - interval '5 days',  100, '10000000-0000-0000-0000-000000000012'),
  ('40000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000004', 'Eje cronológico 1918-1939',   'Incluye al menos quince hitos.',                                now() + interval '9 days', 100, '10000000-0000-0000-0000-000000000012'),
  ('40000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000005', 'Reseña de lectura',           'Una página sobre el libro trabajado en el trimestre.',          now() - interval '2 days', 100, '10000000-0000-0000-0000-000000000013'),
  ('40000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000005', 'Análisis sintáctico',         'Diez oraciones compuestas.',                                    now() + interval '4 days',  50, '10000000-0000-0000-0000-000000000013'),
  ('40000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000006', 'Práctica de microscopio',     'Memoria de la práctica con los dibujos de las preparaciones.',  now() - interval '7 days', 100, '10000000-0000-0000-0000-000000000011'),
  ('40000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000006', 'Orgánulos celulares',         'Esquema comparativo entre célula animal y vegetal.',            now() + interval '12 days', 100, '10000000-0000-0000-0000-000000000011')
ON CONFLICT (id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Entregas. Se reparten en tres estados para que la interfaz muestre los tres
-- casos: sin empezar, entregada pendiente de corregir, y ya calificada.
-- ---------------------------------------------------------------------------
INSERT INTO submissions (assignment_id, student_id, content, status, submitted_at)
SELECT s.assignment_id,
       s.student_id,
       CASE s.variante WHEN 0 THEN NULL ELSE 'Entrega del alumnado para la tarea.' END,
       CASE s.variante WHEN 0 THEN 'draft' WHEN 1 THEN 'submitted' ELSE 'graded' END,
       -- La restricción CHECK exige fecha de envío en todo lo que no sea borrador
       CASE s.variante WHEN 0 THEN NULL
            ELSE COALESCE(s.due_date, now()) - interval '1 day' END
FROM (
  SELECT a.id AS assignment_id,
         e.user_id AS student_id,
         a.due_date,
         (row_number() OVER (PARTITION BY a.id ORDER BY e.user_id)) % 3 AS variante
  FROM assignments a
  JOIN enrollments e ON e.class_id = a.class_id AND e.role_in_class = 'student'
  -- Solo de las tareas ya vencidas: las futuras se quedan sin entregar
  WHERE a.due_date < now()
) AS s
ON CONFLICT (assignment_id, student_id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Calificaciones de las entregas marcadas como corregidas. La nota se deriva
-- del identificador para que sea variada pero estable entre ejecuciones.
-- ---------------------------------------------------------------------------
INSERT INTO grades (submission_id, score, feedback, graded_by)
SELECT sub.id,
       55 + (abs(hashtext(sub.id::text)) % 46),      -- entre 55 y 100
       CASE (abs(hashtext(sub.id::text)) % 3)
         WHEN 0 THEN 'Buen trabajo. Cuida la presentación.'
         WHEN 1 THEN 'Correcto, aunque falta justificar algún paso.'
         ELSE        'Muy completo. Sigue así.'
       END,
       profe.user_id
FROM submissions sub
JOIN assignments a ON a.id = sub.assignment_id
JOIN LATERAL (
  SELECT e.user_id
  FROM enrollments e
  WHERE e.class_id = a.class_id AND e.role_in_class = 'teacher'
  ORDER BY e.enrolled_at
  LIMIT 1
) AS profe ON true
WHERE sub.status = 'graded'
ON CONFLICT (submission_id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Muro de anuncios
-- ---------------------------------------------------------------------------
INSERT INTO announcements (id, class_id, author_id, content, pinned) VALUES
  ('70000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000011', 'Recordad que el examen del bloque de álgebra es el próximo jueves. Entra todo lo visto hasta determinantes.', true),
  ('70000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000011', 'He subido las soluciones de los ejercicios de la página 47 en Materiales.', false),
  ('70000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000012', 'La salida al Archivo de Indias se traslada al día 12. Confirmad la autorización.', true),
  ('70000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000013', 'Las reseñas de lectura ya están corregidas. Podéis consultar la nota en Calificaciones.', false)
ON CONFLICT (id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Materiales
-- ---------------------------------------------------------------------------
INSERT INTO resources (id, class_id, topic_id, title, file_url, type, sort_order) VALUES
  ('80000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Apuntes de álgebra',                  'https://example.org/demo/algebra.pdf',    'pdf',   1),
  ('80000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'Soluciones ejercicios pág. 47',       'https://example.org/demo/soluciones.pdf', 'pdf',   2),
  ('80000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', 'Vídeo: concepto de límite',           'https://example.org/demo/limites',        'video', 3),
  ('80000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000003', 'Cronología del siglo XIX',            'https://example.org/demo/xix.pdf',        'pdf',   1),
  ('80000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000006', 'Guion de la práctica de microscopio', 'https://example.org/demo/practica.doc',   'doc',   1)
ON CONFLICT (id) DO NOTHING;


-- ---------------------------------------------------------------------------
-- Eventos de calendario introducidos a mano. Las fechas de entrega no van
-- aquí: se derivan de assignments.due_date.
-- ---------------------------------------------------------------------------
INSERT INTO calendar_events (id, org_id, class_id, title, event_date, type) VALUES
  ('90000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Examen bloque de álgebra',    now() + interval '7 days',  'exam'),
  ('90000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 'Salida al Archivo de Indias', now() + interval '12 days', 'other'),
  ('90000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', NULL,                                   'Día no lectivo',              now() + interval '20 days', 'holiday')
ON CONFLICT (id) DO NOTHING;
