package com.example.EduvibeBackend.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.EduvibeBackend.dto.RegistroUserDto;
import com.example.EduvibeBackend.dto.UsuarioDto;
import com.example.EduvibeBackend.entities.Clase;
import com.example.EduvibeBackend.entities.User;
import com.example.EduvibeBackend.exception.GlobalException;
import com.example.EduvibeBackend.repository.ClaseRepository;
import com.example.EduvibeBackend.repository.UserRepository;

/**
 * Servicio para la gestión de usuarios.
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encode;
    
    @Autowired
    private ClaseRepository claseRepository;

    /**
     * Obtiene un usuario por su email.
     * 
     * @param email el email del usuario a buscar
     * @return el usuario encontrado o null si no existe
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Obtiene un usuario por su ID y lo convierte a DTO.
     * 
     * @param idUser el ID del usuario a buscar
     * @return el usuario encontrado convertido a DTO
     */
    public UsuarioDto getUserById(Integer idUser) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new GlobalException("Usuario no encontrado con el ID proporcionado: " + idUser));
        return convertirAUsuarioDto(user);
    }

    /**
     * Obtiene los usuarios de una clase específica.
     * 
     * @param idClase el ID de la clase
     * @return una lista de usuarios en la clase convertidos a DTO
     */
    public List<UsuarioDto> obtenerUsuariosDeClase(Long idClase) {
        Clase clase = claseRepository.findById(idClase)
                .orElseThrow(() -> new GlobalException("Clase no encontrada"));
        
        List<User> users = userRepository.findUsersByClaseId(idClase);
        
        return users.stream().map(this::convertirAUsuarioDto).collect(Collectors.toList());
    }

    /**
     * Carga un usuario por su nombre de usuario (email).
     * 
     * @param email el email del usuario
     * @return los detalles del usuario
     * @throws UsernameNotFoundException si el usuario no es encontrado
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("El usuario no ha sido encontrado: " + email);
        }
        return userOptional.get();
    }

    /**
     * Registra un nuevo usuario.
     * 
     * @param userDto los datos del usuario a registrar
     * @return el usuario registrado
     * @throws GlobalException si el email ya está registrado o no es válido
     */
    public User registroUsuario(RegistroUserDto userDto) {
        // Verificar que el email termine en "@vibe.com"
        if (!userDto.email().toLowerCase().endsWith("@vibe.com")) {
            throw new GlobalException("El email debe terminar en '@vibe.com'");
        }

        if (userRepository.existsByEmailIgnoreCase(userDto.email())) {
            throw new GlobalException("El email ya ha sido registrado");
        }
        if (userDto.password().length() < 8) {
            throw new GlobalException("El campo contraseña debe de contener 8 o más caracteres");
        }

        User user = new User();
        user.setEmail(userDto.email());
        user.setNombre(userDto.nombre());
        user.setPassword(encode.encode(userDto.password()));
        user.setRol((userDto.rol() != null) ? userDto.rol() : "alumno"); // Asignar "user" si el rol es null

        User save = userRepository.save(user);
        return save;
    }

    /**
     * Cambia la contraseña de un usuario.
     * 
     * @param idUsuario el ID del usuario
     * @param newPassword la nueva contraseña
     * @throws GlobalException si la contraseña no cumple con los requisitos
     */
    public void changePassword(Integer idUsuario, String newPassword) {
        User user = userRepository.findById(idUsuario)
                .orElseThrow(() -> new GlobalException("Usuario no encontrado"));
        
        if (newPassword.length() < 8) {
            throw new GlobalException("El campo contraseña debe de contener 8 o más caracteres");
        }

        String newPasswordEncoded = encode.encode(newPassword);
        user.setPassword(newPasswordEncoded);
        userRepository.save(user);
    }

    /**
     * Obtiene todos los usuarios y los convierte a DTO.
     * 
     * @return una lista de usuarios convertidos a DTO
     */
    public List<UsuarioDto> listarTodosUsuarios() {
        List<User> usuarios = userRepository.findAll();
        return usuarios.stream()
                .map(this::convertirAUsuarioDto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte un objeto User a UsuarioDto.
     * 
     * @param usuario el usuario a convertir
     * @return el usuario convertido a DTO
     */
    private UsuarioDto convertirAUsuarioDto(User usuario) {
        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setId(usuario.getId());
        usuarioDto.setNombre(usuario.getNombre());
        usuarioDto.setEmail(usuario.getEmail());
        usuarioDto.setRol(usuario.getRol());
        return usuarioDto;
    }

    /**
     * Obtiene los usuarios de una clase específica.
     * 
     * @param idClase el ID de la clase
     * @return una lista de usuarios en la clase convertidos a DTO
     */
    public List<UsuarioDto> obtenerUsuariosPorClase(Long idClase) {
        List<User> usuarios = userRepository.findUsersByClaseId(idClase);

        if (usuarios.isEmpty()) {
            throw new GlobalException("No hay usuarios en esta clase");
        }

        return usuarios.stream()
                .map(this::convertirAUsuarioDto)
                .collect(Collectors.toList());
    }

    /**
     * Edita un usuario.
     * 
     * @param idUser el ID del usuario
     * @param usuarioDto los datos del usuario a editar
     * @return el usuario editado convertido a DTO
     * @throws GlobalException si el email ya está registrado o no es válido
     */
    public UsuarioDto editarUsuario(Integer idUser, UsuarioDto usuarioDto) {
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new GlobalException("Usuario no encontrado con el ID proporcionado: " + idUser));

        if (usuarioDto.getNombre() != null) {
            user.setNombre(usuarioDto.getNombre());
        }
        if (usuarioDto.getEmail() != null) {
            if (!user.getEmail().equals(usuarioDto.getEmail()) && userRepository.existsByEmailIgnoreCase(usuarioDto.getEmail())) {
                throw new GlobalException("El email ya ha sido registrado");
            }
            if (!usuarioDto.getEmail().toLowerCase().endsWith("@vibe.com")) {
                throw new GlobalException("El email debe terminar en '@vibe.com'");
            }
            user.setEmail(usuarioDto.getEmail());
        }
        if (usuarioDto.getRol() != null) {
            user.setRol(usuarioDto.getRol());
        }
        
        User updatedUser = userRepository.save(user);
        return convertirAUsuarioDto(updatedUser);
    }

    /**
     * Elimina un usuario.
     * 
     * @param idUser el ID del usuario a eliminar
     * @throws GlobalException si el usuario no es encontrado
     */
    public void eliminarUsuario(Integer idUser) {
        if (!userRepository.existsById(idUser)) {
            throw new GlobalException("Usuario no encontrado con el ID proporcionado: " + idUser);
        }
        userRepository.deleteById(idUser);
    }
}
