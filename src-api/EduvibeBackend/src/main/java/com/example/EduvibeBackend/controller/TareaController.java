package com.example.EduvibeBackend.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.EduvibeBackend.dto.GetTareaDTO;
import com.example.EduvibeBackend.dto.TareaDTO;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.repository.UserRepository;
import com.example.EduvibeBackend.service.impl.ClaseServiceImpl;
import com.example.EduvibeBackend.service.impl.TareaServiceImpl;

import io.jsonwebtoken.io.IOException;

/**
 * Controlador REST para gestionar las tareas.
 */
@RestController
@RequestMapping("/tareas")
public class TareaController {

    @Autowired
    private TareaServiceImpl tareaService;
    
    @Autowired
    private ClaseServiceImpl claseService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Crear una nueva tarea.
     *
     * @param tareaDto objeto DTO con los detalles de la tarea a crear
     * @return la tarea creada envuelta en un ResponseEntity con estado HTTP 201 (CREATED)
     */
    @PostMapping("/crear")
    public ResponseEntity<TareaDTO> crearTarea(@RequestBody TareaDTO tareaDto) {
        TareaDTO nuevaTarea = tareaService.crearTarea(tareaDto);
        return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
    }
    
    /**
     * Crear una nueva tarea y asignarla a una clase.
     *
     * @param idClase el ID de la clase
     * @param tareaDto objeto DTO con los detalles de la tarea a crear
     * @return la tarea creada envuelta en un ResponseEntity con estado HTTP 201 (CREATED)
     */
    @PostMapping("/crear/{idClase}")
    public ResponseEntity<TareaDTO> crearTareaAsignandoClase(@PathVariable Long idClase, @RequestBody TareaDTO tareaDto) {
        try {
            TareaDTO nuevaTarea = tareaService.crearTareaAsignandoClase(idClase, tareaDto);
            return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
        } catch (GlobalException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Asignar una tarea a un usuario.
     *
     * @param idTarea el ID de la tarea
     * @param idUsuario el ID del usuario
     */
    @PostMapping("/asignar")
    public void asignarTareaAUsuario(@RequestParam Long idTarea, @RequestParam Integer idUsuario) {
        tareaService.asignarTareaAUsuario(idTarea, idUsuario);
    }

    /**
     * Obtener una tarea por su ID.
     *
     * @param id el ID de la tarea
     * @return la tarea obtenida envuelta en un ResponseEntity con estado HTTP 200 (OK), o un 404 (NOT FOUND) si no se encuentra
     */
    @GetMapping("/{id}")
    public ResponseEntity<GetTareaDTO> obtenerTareaPorId(@PathVariable Long id) {
        GetTareaDTO tarea = tareaService.obtenerTareaPorId(id);
        if (tarea != null) {
            return new ResponseEntity<>(tarea, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Editar una tarea existente.
     *
     * @param id el ID de la tarea a editar
     * @param tareaDto objeto DTO con los nuevos detalles de la tarea
     * @return la tarea actualizada envuelta en un ResponseEntity con estado HTTP 200 (OK), o un 404 (NOT FOUND) si no se encuentra
     */
    @PutMapping("editar/{id}")
    public ResponseEntity<TareaDTO> editarTarea(@PathVariable Long id, @RequestBody TareaDTO tareaDto) {
        TareaDTO tareaActualizada = tareaService.editarTarea(id, tareaDto);
        if (tareaActualizada != null) {
            return new ResponseEntity<>(tareaActualizada, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Eliminar una tarea por su ID.
     *
     * @param id el ID de la tarea a eliminar
     * @return un ResponseEntity vacío con estado HTTP 204 (NO CONTENT)
     */
    @DeleteMapping("eliminar/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        tareaService.eliminarTarea(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    /**
     * Obtener todas las tareas.
     *
     * @return una lista de todas las tareas envuelta en un ResponseEntity con estado HTTP 200 (OK)
     */
    @GetMapping("/todos")
    public ResponseEntity<List<GetTareaDTO>> obtenerTodasLasTareas() {
        List<GetTareaDTO> tareas = tareaService.obtenerTodasLasTareas();
        return new ResponseEntity<>(tareas, HttpStatus.OK);
    }
    
    /**
     * Obtener la media de calificaciones de una clase.
     *
     * @param idClase el ID de la clase
     * @return la media de calificaciones envuelta en un ResponseEntity con estado HTTP 200 (OK), o un 204 (NO CONTENT) si no se encuentra
     */
    @GetMapping("/mediaCalificaciones/{idClase}")
    public ResponseEntity<Double> obtenerMediaCalificaciones(@PathVariable Long idClase) {
        Double media = tareaService.calcularMediaCalificaciones(idClase);
        if (media != null) {
            return ResponseEntity.ok(media);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    
    /**
     * Editar la solución escrita de una tarea.
     *
     * @param id el ID de la tarea
     * @param nuevaSolucionEscrita la nueva solución escrita
     * @return la tarea actualizada envuelta en un ResponseEntity con estado HTTP 200 (OK), o un 404 (NOT FOUND) si no se encuentra
     */
    @PutMapping("/solucion/{id}")
    public ResponseEntity<TareaDTO> editarSolucionEscrita(@PathVariable Long id, @RequestBody String nuevaSolucionEscrita) {
        TareaDTO tareaActualizada = tareaService.editarSolucionEscrita(id, nuevaSolucionEscrita);
        if (tareaActualizada != null) {
            return new ResponseEntity<>(tareaActualizada, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Obtener las tareas de una clase.
     *
     * @param idClase el ID de la clase
     * @return una lista de las tareas de la clase
     */
    @GetMapping("/clase/{idClase}")
    public List<GetTareaDTO> getTareasByClase(@PathVariable Long idClase) {
        Clase clase = claseService.obtenerClaseSinDto(idClase);
        return tareaService.getTareasByClase(clase);
    }

    /**
     * Obtener las tareas de una clase para el usuario actual.
     *
     * @param claseId el ID de la clase
     * @return una lista de tareas de la clase para el usuario actual envuelta en un ResponseEntity con estado HTTP 200 (OK)
     */
    @GetMapping("/clase/user/{claseId}")
    public ResponseEntity<List<GetTareaDTO>> getTareasByClaseForCurrentUser(@PathVariable Long claseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
                                  .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Clase clase = claseService.obtenerClaseSinDto(claseId);
        if (clase == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        List<GetTareaDTO> tareas = tareaService.getTareasByClaseAndUser(clase, user);
        return new ResponseEntity<>(tareas, HttpStatus.OK);
    }

    /**
     * Editar la calificación de una tarea.
     *
     * @param id el ID de la tarea
     * @param nuevaCalificacion la nueva calificación
     * @return un ResponseEntity vacío con estado HTTP 200 (OK), o un 404 (NOT FOUND) si no se encuentra
     */
    @PutMapping("/calificacion/{id}")
    public ResponseEntity<Void> editarCalificacionTarea(@PathVariable Long id, @RequestParam Double nuevaCalificacion) {
        try {
            tareaService.editarCalificacionTarea(id, nuevaCalificacion);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (GlobalException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
