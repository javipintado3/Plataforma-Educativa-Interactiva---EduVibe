-- Fase 2: foros de debate por tema, con hilos y moderación.
CREATE TABLE forum_threads (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  class_id        uuid NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
  topic_id        uuid REFERENCES topics(id) ON DELETE SET NULL,
  author_id       uuid NOT NULL REFERENCES users(id),
  title           text NOT NULL,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_forum_threads_class ON forum_threads(class_id);

-- El primer mensaje de un hilo es un forum_post más, no un campo aparte del
-- hilo: así responder y abrir un hilo son la misma operación repetida, en
-- vez de dos formas distintas de guardar un mensaje.
CREATE TABLE forum_posts (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  thread_id       uuid NOT NULL REFERENCES forum_threads(id) ON DELETE CASCADE,
  author_id       uuid NOT NULL REFERENCES users(id),
  content         text NOT NULL,
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_forum_posts_thread ON forum_posts(thread_id);
