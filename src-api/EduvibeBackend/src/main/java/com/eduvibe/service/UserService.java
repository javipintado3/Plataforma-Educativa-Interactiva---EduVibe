package com.eduvibe.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eduvibe.dto.common.PageResponse;
import com.eduvibe.dto.user.CreateUserRequest;
import com.eduvibe.dto.user.CreateUserResponse;
import com.eduvibe.dto.user.InvitationResponse;
import com.eduvibe.dto.user.UserResponse;
import com.eduvibe.exception.BadRequestException;
import com.eduvibe.exception.ConflictException;
import com.eduvibe.exception.NotFoundException;
import com.eduvibe.model.Organization;
import com.eduvibe.model.User;
import com.eduvibe.model.enums.UserRole;
import com.eduvibe.model.enums.UserStatus;
import com.eduvibe.repository.OrganizationRepository;
import com.eduvibe.repository.UserRepository;
import com.eduvibe.repository.spec.UserSpecifications;
import com.eduvibe.security.AuthenticatedUser;

import lombok.RequiredArgsConstructor;

/**
 * Gestión de usuarios desde el panel de administración.
 *
 * Todas las operaciones quedan acotadas a la organización de quien las pide:
 * un administrador no puede ver ni tocar cuentas de otro centro aunque conozca
 * su identificador.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final InvitationService invitationService;
    private final AuthService authService;

    /**
     * Da de alta una cuenta en estado pendiente y emite su invitación.
     *
     * La cuenta nace sin contraseña a propósito: la establece la persona al
     * aceptar la invitación, de modo que nunca existe una contraseña que el
     * administrador conozca.
     */
    @Transactional
    public CreateUserResponse crear(CreateUserRequest peticion) {
        AuthenticatedUser admin = authService.identidadActual();
        Organization organizacion = organizacionDe(admin);

        String email = User.normalizarEmail(peticion.email());

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Ya existe una cuenta con el email " + email);
        }

        if (!organizacion.admiteEmail(email)) {
            throw new BadRequestException(
                    "El email debe pertenecer al dominio " + organizacion.getAllowedDomain());
        }

        User usuario = new User(organizacion, email, peticion.name().trim(),
                UserRole.desdeValor(peticion.role()));
        // saveAndFlush y no save: el INSERT tiene que ejecutarse ya para que
        // lleguen los valores que pone la base de datos (created_at), que si no
        // viajarían a null en la respuesta.
        userRepository.saveAndFlush(usuario);

        InvitationResponse invitacion = invitationService.emitirPara(usuario);

        return new CreateUserResponse(UserResponse.de(usuario), invitacion);
    }

    /**
     * Listado con filtros opcionales. Los criterios se componen con
     * Specifications, de forma que el filtro que no se indica ni siquiera
     * aparece en la consulta.
     */
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listar(String role, String status, String busqueda, Pageable pageable) {
        AuthenticatedUser admin = authService.identidadActual();

        Specification<User> filtro = Specification
                .where(UserSpecifications.deOrganizacion(admin.organizationId()))
                .and(UserSpecifications.conRol(rolONulo(role)))
                .and(UserSpecifications.conEstado(estadoONulo(status)))
                .and(UserSpecifications.queContenga(busqueda));

        Page<User> pagina = userRepository.findAll(filtro, pageable);

        return PageResponse.de(pagina, UserResponse::de);
    }

    private UserRole rolONulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : UserRole.desdeValor(valor);
    }

    private UserStatus estadoONulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : UserStatus.desdeValor(valor);
    }

    @Transactional(readOnly = true)
    public UserResponse obtener(UUID id) {
        return UserResponse.de(buscarEnMiOrganizacion(id));
    }

    /**
     * Activa o desactiva una cuenta.
     *
     * No hay borrado: desactivar conserva el histórico académico, que es lo que
     * se quiere en un centro educativo. Y un administrador no puede
     * desactivarse a sí mismo, o podría dejar la organización sin nadie capaz
     * de volver a entrar.
     */
    @Transactional
    public UserResponse cambiarEstado(UUID id, String nuevoEstado) {
        AuthenticatedUser admin = authService.identidadActual();

        if (admin.id().equals(id)) {
            throw new BadRequestException("No puedes cambiar el estado de tu propia cuenta");
        }

        User usuario = buscarEnMiOrganizacion(id);
        UserStatus estado = UserStatus.desdeValor(nuevoEstado);

        if (estado == UserStatus.ACTIVE && usuario.getPasswordHash() == null) {
            throw new BadRequestException(
                    "Esta cuenta aún no ha aceptado su invitación, así que no se puede activar. "
                            + "Vuelve a enviarle la invitación.");
        }

        usuario.setStatus(estado);
        userRepository.save(usuario);

        return UserResponse.de(usuario);
    }

    /**
     * Vuelve a emitir la invitación de una cuenta que todavía no la ha
     * aceptado. No se crea un usuario nuevo: se renueva el enlace del que ya
     * existe.
     */
    @Transactional
    public InvitationResponse reenviarInvitacion(UUID id) {
        User usuario = buscarEnMiOrganizacion(id);

        if (usuario.getStatus() != UserStatus.PENDING) {
            throw new ConflictException("Esta cuenta ya está activada; no procede reenviar la invitación");
        }

        return invitationService.emitirPara(usuario);
    }

    private User buscarEnMiOrganizacion(UUID id) {
        AuthenticatedUser admin = authService.identidadActual();

        return userRepository.findById(id)
                .filter(u -> u.getOrganization().getId().equals(admin.organizationId()))
                // Un usuario de otra organización se trata como inexistente:
                // responder 403 confirmaría que ese identificador existe.
                .orElseThrow(() -> NotFoundException.de("Usuario", id));
    }

    private Organization organizacionDe(AuthenticatedUser admin) {
        return organizationRepository.findById(admin.organizationId())
                .orElseThrow(() -> NotFoundException.de("Organización", admin.organizationId()));
    }
}
