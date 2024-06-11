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

@RestController
@RequestMapping("/tareas")
public class TareaController {

    @Autowired
    private TareaServiceImpl tareaService;
    
    @Autowired
    private ClaseServiceImpl claseService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/crear")
    public ResponseEntity<TareaDTO> crearTarea(@RequestBody TareaDTO tareaDto) {
        TareaDTO nuevaTarea = tareaService.crearTarea(tareaDto);
        return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
    }
    
    @PostMapping("/crear/{idClase}")
    public ResponseEntity<TareaDTO> crearTareaAsignandoClase(@PathVariable Long idClase, @RequestBody TareaDTO tareaDto) {
        try {
            TareaDTO nuevaTarea = tareaService.crearTareaAsignandoClase(idClase, tareaDto);
            return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
        } catch (GlobalException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    

    @PostMapping("/asignar")
    public void asignarTareaAUsuario(@RequestParam Long idTarea, @RequestParam Integer idUsuario) {
    	tareaService.asignarTareaAUsuario(idTarea, idUsuario);
    }


    @GetMapping("/{id}")
    public ResponseEntity<GetTareaDTO> obtenerTareaPorId(@PathVariable Long id) {
        GetTareaDTO tarea = tareaService.obtenerTareaPorId(id);
        if (tarea != null) {
            return new ResponseEntity<>(tarea, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("editar/{id}")
    public ResponseEntity<TareaDTO> editarTarea(@PathVariable Long id, @RequestBody TareaDTO tareaDto) {
        TareaDTO tareaActualizada = tareaService.editarTarea(id, tareaDto);
        if (tareaActualizada != null) {
            return new ResponseEntity<>(tareaActualizada, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("eliminar/{id}")
    public ResponseEntity<Void> eliminarTarea(@PathVariable Long id) {
        tareaService.eliminarTarea(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/todos")
    public ResponseEntity<List<GetTareaDTO>> obtenerTodasLasTareas() {
        List<GetTareaDTO> tareas = tareaService.obtenerTodasLasTareas();
        return new ResponseEntity<>(tareas, HttpStatus.OK);
    }

    @PostMapping("/{id}/archivo")
    public ResponseEntity<String> agregarArchivoAdjunto(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo) throws java.io.IOException {
        try {
            tareaService.agregarArchivoAdjunto(id, archivo);
            return ResponseEntity.ok("Archivo adjunto agregado exitosamente.");
        } catch (IOException | SQLException e) {
            return ResponseEntity.status(500).body("Error al agregar archivo adjunto: " + e.getMessage());
        }
    }
    
    

    @GetMapping("/{id}/archivo/{indice}")
    public ResponseEntity<byte[]> descargarArchivoAdjunto(@PathVariable Long id, @PathVariable int indice) {
        try {
            byte[] archivo = tareaService.descargarArchivoAdjunto(id, indice);
            return ResponseEntity.ok(archivo);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/mediaCalificaciones/{idClase}")
    public ResponseEntity<Double> obtenerMediaCalificaciones(@PathVariable Long idClase) {
        Double media = tareaService.calcularMediaCalificaciones(idClase);
        if (media != null) {
            return ResponseEntity.ok(media);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    
    @PutMapping("/solucion/{id}")
    public ResponseEntity<TareaDTO> editarSolucionEscrita(@PathVariable Long id, @RequestBody String nuevaSolucionEscrita) {
        TareaDTO tareaActualizada = tareaService.editarSolucionEscrita(id, nuevaSolucionEscrita);
        if (tareaActualizada != null) {
            return new ResponseEntity<>(tareaActualizada, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    
    @GetMapping("/clase/{idClase}")
    public List<GetTareaDTO> getTareasByClase(@PathVariable Long idClase) {
        Clase clase = claseService.obtenerClaseSinDto(idClase);
        return tareaService.getTareasByClase(clase);
    }

    // Nuevo método para obtener tareas por clase para el usuario actual
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
