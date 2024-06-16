package com.example.EduvibeBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.service.impl.ClaseServiceImpl;

/**
 * Controlador REST para gestionar las clases.
 */
@RestController
@RequestMapping("clases")
public class ClaseController {

    @Autowired
    private ClaseServiceImpl claseService;

    /**
     * Crear una nueva clase.
     * 
     * @param claseDto objeto DTO con los detalles de la clase a crear
     * @return la clase creada envuelta en un ResponseEntity
     */
    @PostMapping("/crear")
    public ResponseEntity<ClaseDto> crearClase(@RequestBody ClaseDto claseDto) {
        ClaseDto nuevaClase = claseService.crearClase(claseDto);
        return ResponseEntity.ok(nuevaClase);
    }

    /**
     * Obtener una clase por su ID.
     * 
     * @param id el ID de la clase
     * @return la clase obtenida envuelta en un ResponseEntity, o un 404 si no se encuentra
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClaseDto> obtenerClasePorId(@PathVariable Long id) {
        ClaseDto clase = claseService.obtenerClasePorId(id);
        if (clase != null) {
            return ResponseEntity.ok(clase);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Eliminar un usuario de una clase.
     * 
     * @param email el email del usuario a eliminar
     * @param idClase el ID de la clase
     * @return un ResponseEntity vacío con estado 204
     */
    @DeleteMapping("/{idClase}/eliminar-usuario")
    public ResponseEntity<Void> eliminarUsuarioDeClase(@RequestParam String email, @PathVariable Long idClase) {
        claseService.eliminarUsuarioDeClase(email, idClase);
        return ResponseEntity.noContent().build();
    }

    /**
     * Editar una clase existente.
     * 
     * @param id el ID de la clase a editar
     * @param claseDto objeto DTO con los nuevos detalles de la clase
     * @return la clase actualizada envuelta en un ResponseEntity, o un 404 si no se encuentra
     */
    @PutMapping("editar/{id}")
    public ResponseEntity<ClaseDto> editarClase(@PathVariable Long id, @RequestBody ClaseDto claseDto) {
        ClaseDto claseActualizada = claseService.editarClase(id, claseDto);
        if (claseActualizada != null) {
            return ResponseEntity.ok(claseActualizada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Inscribir un usuario en una clase.
     * 
     * @param idUsuario el ID del usuario a inscribir
     * @param idClase el ID de la clase
     * @return un ResponseEntity vacío con estado 200
     */
    @PostMapping("/inscribir")
    public ResponseEntity<Void> inscribirUsuarioEnClase(@RequestParam Integer idUsuario, @RequestParam Long idClase) {
        claseService.inscribirUsuarioEnClase(idUsuario, idClase);
        return ResponseEntity.ok().build();
    }

    /**
     * Eliminar una clase por su ID.
     * 
     * @param id el ID de la clase a eliminar
     * @return un ResponseEntity vacío con estado 204
     */
    @DeleteMapping("eliminar/{id}")
    public ResponseEntity<Void> eliminarClase(@PathVariable Long id) {
        claseService.eliminarClase(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener todas las clases.
     * 
     * @param token el token de autorización (usado para validación)
     * @return una lista de todas las clases envuelta en un ResponseEntity
     */
    @GetMapping("/todos")
    public ResponseEntity<List<ClaseDto>> obtenerTodasLasClases(@RequestHeader("Authorization") String token) {
        List<ClaseDto> clases = claseService.obtenerTodasLasClases();
        return ResponseEntity.ok(clases);
    }

    /**
     * Obtener clases por usuario.
     * 
     * @param idUsuario el ID del usuario
     * @return una lista de clases del usuario envuelta en un ResponseEntity
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ClaseDto>> obtenerClasesPorUsuario(@PathVariable Integer idUsuario) {
        List<ClaseDto> clases = claseService.obtenerClasesPorUsuario(idUsuario);
        return ResponseEntity.ok(clases);
    }
}
