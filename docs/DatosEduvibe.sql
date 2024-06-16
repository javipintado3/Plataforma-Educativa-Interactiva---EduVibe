
use eduvibe;
-- Insertar datos en la tabla de usuarios
INSERT INTO usuarios (email, nombre, password, rol) VALUES
-- Administrador
('admin@vibe.com', 'Admin', '$2a$10$ot35PY6tDrKjPUMzDp4PgOCCVIGA46BSytbTFii4.3pTBJc.9ScdW', 'admin'),

-- Profesores
('david.cifuentes@vibe.com', 'David Cifuentes', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'profesor'),
('antonio.gabriel@vibe.com', 'Antonio Gabriel', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'profesor'),
('monica.maria@vibe.com', 'Mónica Maria', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'profesor'),
('juan.pablo@vibe.com', 'Juan Pablo', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'profesor'),
('valentin.corredor@vibe.com', 'Valentín Corredor', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'profesor'),

-- Alumnos
('javier.pintado@vibe.com', 'Javier Pintado', '$2a$10$.QzCOopNML5S5DF517uf/uXfxjrxTeqXKVeO2GGAyhhiDcukx6wiC', 'alumno'),
('enrique.dominguez@vibe.com', 'Enrique Dominguez', '$2a$10$Mw19mEdWGrCegQA9ZqwJDOASH1PbIZbYnf66nStJkc1DZjE8ZV/mi', 'alumno'),
('manuel.rojo@vibe.com', 'Manuel Rojo', '$2a$10$/TWXiJzVK.Qg4ONswJaK/.VrShDQouvFi28rk8NdwMUcinx54lJMq', 'alumno'),
('marina.vazquez@vibe.com', 'Marina Vázquez', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('jimena.mojarro@vibe.com', 'Jimena Mojarro', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('alberto.vazquez@vibe.com', 'Alberto Vázquez', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('daniel.espinosa@vibe.com', 'Daniel Espinosa', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('maria.azcarate@vibe.com', 'María Azcárate', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('roberto.marquez@vibe.com', 'Roberto Márquez', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('andres.saez@vibe.com', 'Andrés Sáez', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('miguel.saez@vibe.com', 'Miguel Sáez', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('carlos.duran@vibe.com', 'Carlos Durán', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('candela.fernandez@vibe.com', 'Candela Fernández', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('irene.gonzalez@vibe.com', 'Irene González', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('rafael.tirado@vibe.com', 'Rafael Tirado', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('anselmo.garcia@vibe.com', 'Anselmo García', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('marta.candau@vibe.com', 'Marta Candau', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('javier.gamez@vibe.com', 'Javier Gamez', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('antonio.romero@vibe.com', 'Antonio Romero', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('helena.isnard@vibe.com', 'Helena Isnard', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('ismael.luna@vibe.com', 'Ismael Luna', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno'),
('carlos.landa@vibe.com', 'Carlos Landa', '$2a$10$SM4fZDg/AYJGvip5SsgLYe4Oa1GyeV/VN/ip0Fb/dHcC2HyFpa9YS', 'alumno');

-- Insertar datos en la tabla de clases
INSERT INTO clases (nombre, descripcion) VALUES
('Matemáticas', 'Clase de matemáticas avanzada'),
('Historia', 'Clase de historia del arte'),
('Ciencias', 'Clase de ciencias naturales'),
('Física', 'Clase de física teórica y experimental'),
('Química', 'Clase de química general'),
('Literatura', 'Clase de literatura clásica y moderna'),
('Filosofía', 'Clase de filosofía y pensamiento crítico'),
('Biología', 'Clase de biología y ciencias de la vida'),
('Geografía', 'Clase de geografía física y humana'),
('Educación Física', 'Clase de educación física y deportes');

-- Insertar datos en la tabla de usuario_clase (relación muchos a muchos)
-- Asignar profesores a clases
INSERT INTO usuario_clase (usuario_id, clase_id) VALUES
-- Profesores en sus respectivas clases
((SELECT id FROM usuarios WHERE email = 'david.cifuentes@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Matemáticas' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'antonio.gabriel@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Física' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'monica.maria@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Química' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'juan.pablo@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Literatura' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'valentin.corredor@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1)),

-- Inscribiendo alumnos en clases
-- Clase de Matemáticas
((SELECT id FROM usuarios WHERE email = 'javier.pintado@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Matemáticas' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'enrique.dominguez@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Matemáticas' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'manuel.rojo@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Matemáticas' LIMIT 1)),

-- Clase de Física
((SELECT id FROM usuarios WHERE email = 'marina.vazquez@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Física' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'jimena.mojarro@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Física' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'alberto.vazquez@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Física' LIMIT 1)),

-- Clase de Química
((SELECT id FROM usuarios WHERE email = 'daniel.espinosa@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Química' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'maria.azcarate@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Química' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'roberto.marquez@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Química' LIMIT 1)),

-- Clase de Literatura
((SELECT id FROM usuarios WHERE email = 'andres.saez@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Literatura' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'miguel.saez@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Literatura' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'carlos.duran@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Literatura' LIMIT 1)),

-- Clase de Filosofía
((SELECT id FROM usuarios WHERE email = 'candela.fernandez@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'irene.gonzalez@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'rafael.tirado@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1)),

-- Clase de Biología
((SELECT id FROM usuarios WHERE email = 'anselmo.garcia@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Biología' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'marta.candau@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Biología' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'javier.gamez@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Biología' LIMIT 1)),

-- Clase de Geografía
((SELECT id FROM usuarios WHERE email = 'antonio.romero@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Geografía' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'helena.isnard@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Geografía' LIMIT 1)),
((SELECT id FROM usuarios WHERE email = 'ismael.luna@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Geografía' LIMIT 1)),

-- Clase de Educación Física
((SELECT id FROM usuarios WHERE email = 'carlos.landa@vibe.com'), (SELECT id_clase FROM clases WHERE nombre = 'Educación Física' LIMIT 1));

-- Insertar datos en la tabla de tareas
-- Asignar tareas solo a los alumnos con calificaciones y soluciones reales

-- Tareas para Matemáticas
INSERT INTO tarea (calificacion, enunciado, estado, fecha_apertura, nombre_tarea, solucion_escrita, id_clase, usuario_id) VALUES
(9.5, 'Resolver problemas de álgebra: Simplificar (3x^2 - 2x + 5) - (x^2 - 4x - 1)', 1, '2023-01-01', 'Tarea 1 de Matemáticas', '2x^2 + 2x + 6', (SELECT id_clase FROM clases WHERE nombre = 'Matemáticas' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'javier.pintado@vibe.com')),
(4.5, 'Resolver problemas de geometría: Calcular el área de un triángulo con base 5 cm y altura 10 cm', 1, '2023-01-02', 'Tarea 2 de Matemáticas', '25 cm^2', (SELECT id_clase FROM clases WHERE nombre = 'Matemáticas' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'enrique.dominguez@vibe.com')),
(8.0, 'Resolver ecuaciones cuadráticas: Resolver x^2 - 4x + 4 = 0', 1, '2023-01-03', 'Tarea 3 de Matemáticas', 'x = 2', (SELECT id_clase FROM clases WHERE nombre = 'Matemáticas' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'manuel.rojo@vibe.com')),

-- Tareas para Física
(9.0, 'Calcular la velocidad final: Un objeto en caída libre desde una altura de 20 m', 1, '2023-02-01', 'Tarea 1 de Física', '19.8 m/s', (SELECT id_clase FROM clases WHERE nombre = 'Física' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'marina.vazquez@vibe.com')),
(8.5, 'Explicar la Ley de Newton: Describir la primera ley de Newton', 1, '2023-02-02', 'Tarea 2 de Física', 'Un objeto en reposo permanecerá en reposo y un objeto en movimiento continuará en movimiento a menos que actúe sobre él una fuerza externa.', (SELECT id_clase FROM clases WHERE nombre = 'Física' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'jimena.mojarro@vibe.com')),
(2.8, 'Calcular la energía cinética: Un objeto de 2 kg moviéndose a 3 m/s', 1, '2023-02-03', 'Tarea 3 de Física', '9 J', (SELECT id_clase FROM clases WHERE nombre = 'Física' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'alberto.vazquez@vibe.com')),

-- Tareas para Química
(9.0, 'Balancear ecuaciones químicas: H2 + O2 -> H2O', 1, '2023-03-01', 'Tarea 1 de Química', '2H2 + O2 -> 2H2O', (SELECT id_clase FROM clases WHERE nombre = 'Química' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'daniel.espinosa@vibe.com')),
(8.5, 'Explicar la Ley de Avogadro: Definir la ley y proporcionar un ejemplo', 1, '2023-03-02', 'Tarea 2 de Química', 'La ley de Avogadro establece que el volumen de un gas es directamente proporcional al número de moles del gas, si la temperatura y la presión son constantes.', (SELECT id_clase FROM clases WHERE nombre = 'Química' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'maria.azcarate@vibe.com')),
(3.8, 'Determinar la molaridad: Calcular la molaridad de una solución con 5 moles de soluto en 2 litros de solución', 1, '2023-03-03', 'Tarea 3 de Química', '2.5 M', (SELECT id_clase FROM clases WHERE nombre = 'Química' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'roberto.marquez@vibe.com')),

-- Tareas para Literatura
(9.2, 'Análisis de una obra literaria: Analizar "Cien años de soledad" de Gabriel García Márquez', 1, '2023-04-01', 'Tarea 1 de Literatura', 'La obra explora temas de soledad, realismo mágico y la historia de la familia Buendía.', (SELECT id_clase FROM clases WHERE nombre = 'Literatura' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'andres.saez@vibe.com')),
(8.9, 'Comparar autores: Comparar las obras de Shakespeare y Cervantes', 1, '2023-04-02', 'Tarea 2 de Literatura', 'Ambos autores exploraron temas universales y complejidades humanas, aunque sus estilos y contextos históricos eran diferentes.', (SELECT id_clase FROM clases WHERE nombre = 'Literatura' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'miguel.saez@vibe.com')),
(1.5, 'Escribir un poema: Componer un poema sobre la naturaleza', 1, '2023-04-03', 'Tarea 3 de Literatura', 'Un poema lírico que celebra la belleza y el misterio de la naturaleza.', (SELECT id_clase FROM clases WHERE nombre = 'Literatura' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'carlos.duran@vibe.com')),

-- Tareas para Filosofía
(9.0, 'Explicar la Alegoría de la Caverna de Platón', 1, '2023-05-01', 'Tarea 1 de Filosofía', 'La alegoría ilustra la diferencia entre el mundo de las apariencias y la realidad.', (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'candela.fernandez@vibe.com')),
(8.8, 'Analizar el existencialismo de Sartre', 1, '2023-05-02', 'Tarea 2 de Filosofía', 'El existencialismo de Sartre enfatiza la libertad individual, la responsabilidad y la angustia existencial.', (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'irene.gonzalez@vibe.com')),
(8.5, 'Comparar las teorías éticas de Kant y Mill', 1, '2023-05-03', 'Tarea 3 de Filosofía', 'Kant se enfoca en la ética deontológica basada en el deber, mientras que Mill promueve el utilitarismo basado en las consecuencias.', (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'rafael.tirado@vibe.com')),

-- Tareas para Biología
(9.2, 'Describir el proceso de fotosíntesis en plantas', 1, '2023-06-01', 'Tarea 1 de Biología', 'La fotosíntesis es el proceso por el cual las plantas convierten la luz solar en energía química.', (SELECT id_clase FROM clases WHERE nombre = 'Biología' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'anselmo.garcia@vibe.com')),
(9.0, 'Explicar la teoría de la evolución de Darwin', 1, '2023-06-02', 'Tarea 2 de Biología', 'La teoría de la evolución de Darwin propone que las especies evolucionan a través de la selección natural.', (SELECT id_clase FROM clases WHERE nombre = 'Biología' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'marta.candau@vibe.com')),
(8.8, 'Analizar la estructura del ADN', 1, '2023-06-03', 'Tarea 3 de Biología', 'El ADN es una doble hélice compuesta por nucleótidos que almacena la información genética.', (SELECT id_clase FROM clases WHERE nombre = 'Biología' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'javier.gamez@vibe.com')),

-- Tareas para Geografía
(8.5, 'Describir el ciclo del agua', 1, '2023-07-01', 'Tarea 1 de Geografía', 'El ciclo del agua incluye la evaporación, condensación, precipitación y el flujo de retorno al mar.', (SELECT id_clase FROM clases WHERE nombre = 'Geografía' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'antonio.romero@vibe.com')),
(8.8, 'Explicar los tipos de clima en el mundo', 1, '2023-07-02', 'Tarea 2 de Geografía', 'Los principales tipos de clima incluyen tropical, árido, templado, continental y polar.', (SELECT id_clase FROM clases WHERE nombre = 'Geografía' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'helena.isnard@vibe.com')),
(9.0, 'Analizar la geografía humana y sus componentes', 1, '2023-07-03', 'Tarea 3 de Geografía', 'La geografía humana estudia las relaciones entre las personas y su entorno, incluyendo la distribución de la población y el uso del suelo.', (SELECT id_clase FROM clases WHERE nombre = 'Geografía' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'ismael.luna@vibe.com')),

-- Tareas para Ciencias
(9.2, 'Explicar la ley de la gravedad de Newton', 1, '2023-10-01', 'Tarea 1 de Ciencias', 'La ley de la gravedad de Newton establece que todos los cuerpos se atraen con una fuerza proporcional a sus masas e inversamente proporcional al cuadrado de la distancia que los separa.', (SELECT id_clase FROM clases WHERE nombre = 'Ciencias' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'marina.vazquez@vibe.com')),
(9.0, 'Describir el ciclo del carbono', 1, '2023-10-02', 'Tarea 2 de Ciencias', 'El ciclo del carbono incluye la fotosíntesis, la respiración, la descomposición y la combustión, que mueven el carbono a través de la atmósfera, la biosfera y la geosfera.', (SELECT id_clase FROM clases WHERE nombre = 'Ciencias' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'jimena.mojarro@vibe.com')),
(8.8, 'Explicar el principio de conservación de la energía', 1, '2023-10-03', 'Tarea 3 de Ciencias', 'El principio de conservación de la energía establece que la energía no se crea ni se destruye, solo se transforma de una forma a otra.', (SELECT id_clase FROM clases WHERE nombre = 'Ciencias' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'alberto.vazquez@vibe.com')),

-- Tareas para Filosofía (continuación)
(2.2, 'Explicar el concepto de "la existencia precede a la esencia" en el existencialismo', 1, '2023-11-01', 'Tarea 1 de Filosofía', 'El concepto significa que los humanos no tienen un propósito o esencia predeterminados y deben definir su propia existencia a través de sus acciones.', (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'candela.fernandez@vibe.com')),
(9.0, 'Analizar la ética de Aristóteles en la "Ética a Nicómaco"', 1, '2023-11-02', 'Tarea 2 de Filosofía', 'Aristóteles argumenta que la virtud es el medio entre dos extremos y que la felicidad se alcanza a través de la práctica de la virtud.', (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'irene.gonzalez@vibe.com')),
(8.8, 'Comparar el empirismo de Hume con el racionalismo de Descartes', 1, '2023-11-03', 'Tarea 3 de Filosofía', 'Hume sostiene que todo conocimiento proviene de la experiencia sensorial, mientras que Descartes cree que la razón es la fuente principal del conocimiento.', (SELECT id_clase FROM clases WHERE nombre = 'Filosofía' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'rafael.tirado@vibe.com')),

-- Tareas para Educación Física (continuación)
(9.2, 'Describir los beneficios del entrenamiento de resistencia', 1, '2023-12-01', 'Tarea 1 de Educación Física', 'El entrenamiento de resistencia mejora la fuerza muscular, la densidad ósea y la salud metabólica.', (SELECT id_clase FROM clases WHERE nombre = 'Educación Física' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'carlos.landa@vibe.com')),
(9.0, 'Explicar los principios del entrenamiento cardiovascular', 1, '2023-12-02', 'Tarea 2 de Educación Física', 'El entrenamiento cardiovascular mejora la eficiencia del sistema cardiovascular, aumenta la capacidad pulmonar y reduce el riesgo de enfermedades cardiovasculares.', (SELECT id_clase FROM clases WHERE nombre = 'Educación Física' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'carlos.landa@vibe.com')),
(8.8, 'Describir la importancia de la flexibilidad y los ejercicios de estiramiento', 1, '2023-12-03', 'Tarea 3 de Educación Física', 'La flexibilidad mejora la amplitud de movimiento, reduce el riesgo de lesiones y mejora el rendimiento físico.', (SELECT id_clase FROM clases WHERE nombre = 'Educación Física' LIMIT 1), (SELECT id FROM usuarios WHERE email = 'carlos.landa@vibe.com'));
