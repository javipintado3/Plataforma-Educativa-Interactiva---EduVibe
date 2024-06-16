package com.example.EduvibeBackend.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa a un usuario en el sistema.
 * Implementa la interfaz UserDetails para la integración con Spring Security.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "usuarios")
public class User implements UserDetails {

    /**
     * Identificador único del usuario.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Nombre del usuario.
     */
    @NotNull
    private String nombre;

    /**
     * Correo electrónico del usuario.
     * Validado para asegurarse de que tiene un formato de email válido.
     */
    @Email(message = "El email no es válido")
    @NotNull
    private String email;

    /**
     * Contraseña del usuario.
     */
    @NotNull
    private String password;

    /**
     * Rol del usuario (por ejemplo, "admin", "profesor", "alumno").
     */
    private String rol;

    /**
     * Clases en las que está inscrito el usuario.
     * Relación Many-to-Many con la entidad Clase.
     */
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "usuario_clase", // Nombre de la tabla de unión
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "clase_id")
    )
    @JsonIgnore
    private Set<Clase> clases = new HashSet<>();

    /**
     * Lista de tareas asignadas al usuario.
     * Relación One-to-Many con la entidad Tarea.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarea> tareas = new ArrayList<>();

    /**
     * Obtiene las autoridades concedidas al usuario.
     *
     * @return una colección de autoridades concedidas.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(rol));
        return authorities;
    }

    /**
     * Obtiene la contraseña del usuario.
     *
     * @return la contraseña del usuario.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Obtiene el nombre de usuario (correo electrónico).
     *
     * @return el nombre de usuario.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indica si la cuenta del usuario ha expirado.
     *
     * @return true si la cuenta no ha expirado, false en caso contrario.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica si la cuenta del usuario está bloqueada.
     *
     * @return true si la cuenta no está bloqueada, false en caso contrario.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica si las credenciales del usuario han expirado.
     *
     * @return true si las credenciales no han expirado, false en caso contrario.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica si el usuario está habilitado.
     *
     * @return true si el usuario está habilitado, false en caso contrario.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Compara este usuario con otro objeto para verificar si son iguales.
     *
     * @param o el objeto a comparar.
     * @return true si los objetos son iguales, false en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    /**
     * Calcula el código hash del usuario.
     *
     * @return el código hash del usuario.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
