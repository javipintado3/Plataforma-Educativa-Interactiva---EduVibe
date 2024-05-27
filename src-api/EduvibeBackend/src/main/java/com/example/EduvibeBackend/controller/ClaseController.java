package com.example.EduvibeBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.service.ClaseService;

@RestController
@RequestMapping("clases")
public class ClaseController {

    @Autowired
    private ClaseService claseService;

    // Crear una nueva clase
    @PostMapping("/crear")
    public ResponseEntity<ClaseDto> crearClase(@RequestBody ClaseDto claseDto) {
        ClaseDto nuevaClase = claseService.crearClase(claseDto);
        return ResponseEntity.ok(nuevaClase);
    }

    // Obtener una clase por su ID
    @GetMapping("/{id}")
    public ResponseEntity<ClaseDto> obtenerClasePorId(@PathVariable Long id) {
        ClaseDto clase = claseService.obtenerClasePorId(id);
        if (clase != null) {
            return ResponseEntity.ok(clase);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Editar una clase existente
    @PutMapping("editar/{id}")
    public ResponseEntity<ClaseDto> editarClase(@PathVariable Long id, @RequestBody ClaseDto claseDto) {
        ClaseDto claseActualizada = claseService.editarClase(id, claseDto);
        if (claseActualizada != null) {
            return ResponseEntity.ok(claseActualizada);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Eliminar una clase por su ID
    @DeleteMapping("eliminar/{id}")
    public ResponseEntity<Void> eliminarClase(@PathVariable Long id) {
        claseService.eliminarClase(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener todas las clases
    @GetMapping("/todos")
    public ResponseEntity<List<ClaseDto>> obtenerTodasLasClases() {
        List<ClaseDto> clases = claseService.obtenerTodasLasClases();
        return ResponseEntity.ok(clases);
    }
}