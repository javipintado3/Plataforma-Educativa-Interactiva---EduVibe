package com.example.EduvibeBackend.utility;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.EduvibeBackend.exception.GlobalException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenUtils {

	private static final Logger LOG = LoggerFactory.getLogger(TokenUtils.class);

	/**
	 * Clave con la que se firman y verifican los tokens. Se inyecta desde la
	 * configuración (variable de entorno JWT_SECRET) en lugar de estar escrita en
	 * el código, que acaba publicada en el repositorio.
	 */
	private static SecretKey signingKey;

	// Definimos el tiempo de validez del token en segundos.
	private final static Long ACCESS_TOKEN_VALIDATY_SECONDS = (long) 3 * 60 * 60; // 3 horas

	/**
	 * Recibe el secreto de configuración. Si no se ha definido ninguno se genera
	 * una clave aleatoria al arrancar: sirve para desarrollo, pero invalida los
	 * tokens emitidos en ejecuciones anteriores.
	 *
	 * @param secret el secreto configurado, o cadena vacía si no hay ninguno.
	 */
	@Value("${app.jwt.secret:}")
	public void setSecret(String secret) {
		if (secret == null || secret.isBlank()) {
			signingKey = Jwts.SIG.HS256.key().build();
			LOG.warn("No hay JWT_SECRET configurado: se ha generado una clave aleatoria. "
					+ "Los tokens dejarán de ser válidos al reiniciar la aplicación.");
		} else {
			signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		}
	}

	private static SecretKey clave() {
		if (signingKey == null) {
			throw new IllegalStateException("La clave de firma del JWT aún no se ha inicializado");
		}
		return signingKey;
	}

	/**
	 * Este método va a generar un token. En el token incluiremos el username y el
	 * role.
	 * 
	 * @param username: guardaremos el email dentro del token
	 * @param rol:      guardaremos el role dentro del token.
	 * @return: el token que debe empezar por Bearer y un espacio.
	 */
	public static String generateToken(String username, String name, String rol, Integer id) {

		// Establecemos la fecha de expiración del token en milisegundos
		Date expirationDate = new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDATY_SECONDS * 1000);

		// Creamos un mapa para guardar toda la información que queramos guardar en el
		// token. El username no es necesario porque ya va en el subject.
		Map<String, Object> extra = new HashMap<>();
		extra.put("name", name);
		extra.put("rol", rol);
		extra.put("id", id);

		String token = Jwts.builder().subject(username).issuedAt(new Date())
				.expiration(expirationDate).claims(extra)
				.signWith(clave()).compact();

		return "Bearer " + token;
	}

	/**
	 * Obtiene el payLoad de un token
	 * 
	 * @param token
	 * @return
	 * @throws JwtException
	 * @throws IllegalArgumentException
	 * @throws NoSuchAlgorithmException
	 */
	public static Claims getAllClaimsFromToken(String token)
			throws JwtException, IllegalArgumentException, NoSuchAlgorithmException {

		return Jwts.parser().verifyWith(clave()).build().parseSignedClaims(token).getPayload();
	}

	/**
	 * Método para ver el usuario y el role que "contiene" el token. Lo primero que
	 * haremos es decodificar el claims. Si lanza una exception es que no es válido
	 * usando nuestra token secreto.
	 * 
	 * @param token
	 * @return
	 */
	public static UsernamePasswordAuthenticationToken getAuthentication(String token)
			throws JwtException, IllegalArgumentException, NoSuchAlgorithmException {
		Claims claims;

		if (!token.startsWith("Bearer ")) {
			throw new GlobalException("Formato token no válido");
		}
		token = token.substring(7);
		try {
			// Claims == PayLoad
			claims = getAllClaimsFromToken(token);
		} catch (IllegalArgumentException e) {
			throw new GlobalException("Imposible encontrar un JWT Token");
		} catch (ExpiredJwtException e) {
			throw new GlobalException("Token expirado");
		} catch (NoSuchAlgorithmException e) {
			throw new GlobalException("Algoritmo no válido");
		} catch (MalformedJwtException e) {
			throw new GlobalException("Token malformado");
		}

		String username = claims.getSubject();
		String rol = (String) claims.get("rol");
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(rol));

		return new UsernamePasswordAuthenticationToken(username, null, authorities);
	}
}
