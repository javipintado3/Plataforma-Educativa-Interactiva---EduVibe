## Usuario:

Almacena información sobre los usuarios de la aplicación, incluyendo administradores, profesores y alumnos.
Permite la gestión de cuentas de usuarios, como la creación, edición y eliminación de cuentas.
Los usuarios están asociados a las clases a través de la relación Many-to-Many con la entidad Clase.

## Clase:

Representa las clases en la aplicación, que pueden ser creadas y gestionadas por los administradores.
Los profesores están asociados a las clases a través de la relación Many-to-One.
Los alumnos están asociados a las clases a través de la relación Many-to-Many.
Contiene información relevante para la gestión de clases, como el nombre, la descripción y las tareas asignadas.

## Tarea:

Representa las tareas asignadas en cada clase, que pueden ser creadas y gestionadas por los profesores.
Los alumnos pueden ver, entregar y verificar calificaciones de tareas a través de esta entidad.
Contiene información sobre la tarea, como el título, la descripción, la fecha de entrega y el estado.

## ListadoClase:

Representa la relación Many-to-Many entre usuarios y clases, almacenando la asociación entre usuarios y las clases a las que están inscritos.
Contiene atributos adicionales si es necesario, como la fecha de inscripción o el estado de la inscripción.
