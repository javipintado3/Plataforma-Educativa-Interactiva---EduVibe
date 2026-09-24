package com.eduvibe.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduvibe.dto.perfil.AvatarRequest;
import com.eduvibe.dto.perfil.ResumenPerfilResponse;
import com.eduvibe.dto.user.UserResponse;
import com.eduvibe.service.PerfilService;

import lombok.RequiredArgsConstructor;

/**
 * Resumen de perfil de quien está autenticado. Quién es (nombre, email, rol)
 * ya lo da {@code GET /api/auth/me}; esto es solo lo que se añade encima.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    @GetMapping("/summary")
    public ResponseEntity<ResumenPerfilResponse> resumen() {
        return ResponseEntity.ok(perfilService.resumen());
    }

    @PutMapping("/avatar")
    public ResponseEntity<UserResponse> actualizarAvatar(@RequestBody AvatarRequest peticion) {
        return ResponseEntity.ok(perfilService.actualizarAvatar(peticion.url()));
    }
}
