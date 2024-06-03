package com.example.EduvibeBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.EduvibeBackend.dto.ClaseDto;
import com.example.EduvibeBackend.dto.GetTareaDTO;
import com.example.EduvibeBackend.dto.TareaDTO;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.Tarea;
import com.example.EduvibeBackend.service.impl.ClaseServiceImpl;
import com.example.EduvibeBackend.service.impl.TareaServiceImpl;

@RestController
@RequestMapping("/tareas")
public class TareaController {

    @Autowired
    private TareaServiceImpl tareaService;
    @Autowired
    private ClaseServiceImpl claseService;

    @PostMapping("/crear")
    public ResponseEntity<TareaDTO> crearTarea(@RequestBody TareaDTO tareaDto) {
        TareaDTO nuevaTarea = tareaService.crearTarea(tareaDto);
        return new ResponseEntity<>(nuevaTarea, HttpStatus.CREATED);
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
    

    @GetMapping("/clase/{idClase}")
    public List<GetTareaDTO> getTareasByClase(@PathVariable Long idClase) {
        Clase clase = claseService.obtenerClaseSinDto(idClase);
        return tareaService.getTareasByClase(clase);
    }
    

}
