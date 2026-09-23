package com.eduvibe.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eduvibe.dto.common.PageResponse;
import com.eduvibe.dto.user.CreateUserRequest;
import com.eduvibe.dto.user.CreateUserResponse;
import com.eduvibe.dto.user.InvitationResponse;
import com.eduvibe.dto.user.UpdateUserStatusRequest;
import com.eduvibe.dto.user.UserResponse;
import com.eduvibe.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Panel de administración de usuarios.
 *
 * Todo el controlador exige rol de administrador; la regla está en
 * {@link com.eduvibe.config.SecurityConfig}, en un único sitio, en lugar de
 * repetida método a método.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Listado con filtros opcionales por rol, estado y texto libre.
     */
    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> listar(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "q") String busqueda,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(userService.listar(role, status, busqueda, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.obtener(id));
    }

    /** Alta de una cuenta. Devuelve además la invitación emitida. */
    @PostMapping
    public ResponseEntity<CreateUserResponse> crear(@Valid @RequestBody CreateUserRequest peticion) {
        CreateUserResponse creado = userService.crear(peticion);

        return ResponseEntity
                .created(URI.create("/api/users/" + creado.user().id()))
                .body(creado);
    }

    /** Activación o desactivación. No existe el borrado. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest peticion) {

        return ResponseEntity.ok(userService.cambiarEstado(id, peticion.status()));
    }

    /** Vuelve a emitir la invitación de una cuenta pendiente. */
    @PostMapping("/{id}/invitation")
    public ResponseEntity<InvitationResponse> reenviarInvitacion(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.reenviarInvitacion(id));
    }
}
