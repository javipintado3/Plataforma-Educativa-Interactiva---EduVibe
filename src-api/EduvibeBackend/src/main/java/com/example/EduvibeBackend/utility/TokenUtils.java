package com.example.EduvibeBackend.utility;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.example.EduvibeBackend.exception.GlobalException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;


public class TokenUtils {
	
	
	
	// Declaramos el token que usaremos para el cifrado y la decodificación.
		// Lo suyo es que este token sea generado de forma aleatoria al empezar la
		// aplicación
		// Podemos poner el valor que queramos
		private final static String ACCESS_TOKEN_SECRET = "586E3272357538782F413F4428472B4B6250655368566B597033733676397924";

		// Definimos el tiempo de validez del token en segundos. Mientras estemos en
		// fase de desarrollo
		// podemos definir tiempos largos para que no nos den problema la caducidad del
		// token.
		// Cuando nuestra aplicación esté en explotación deberemos ajustar este tiempo.
		// Estos dos parámetro también se pueden definir el el fichero
		// aplicattion.properties

		private final static Long ACCESS_TOKEN_VALIDATY_SECONDS = (long) 3 * 60 * 60; // 3 horas

		/**
		 * Este método va a generar un token. En el token incluiremos el username, 
		 *  role. Esto es un ejemplo, en el token podemos almacenar la información que
		 * necesite nuestra aplicación, por ejemplo, nombre y apellido o lo que
		 * consideremos necesario.
		 * 
		 * @param username: guardaremos el email dentro del token
		 * @param role:     guardaremos el role dentro del token. Si puediera tener
		 *                  varios roles, también se podría guardar.
		 * @return: el token que debe empezar por Bearer y un espacio.
		 */
		
		
		public static String generateToken(String username, String name, String rol) {

			// Establecemos la fecha de expiración del token en milisegundos
			Date expirationDate = new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDATY_SECONDS * 1000);

			// Creamos un mapa para guardar toda la información que queramos guardar en el
			// token
			// El username no es necesario guardarlo porque ya va en el token, en el
			// subject.
			Map<String, Object> extra = new HashMap<>();
			extra.put("name", name);
			extra.put("rol", rol);

			// Construimos el token con el nombre del usuario, la fecha de expiración, la
			// información
			// que queremos guardar y el token que vamos a user para encriptarlo.
			String token = Jwts.builder().subject(username).issuedAt(new Date())
					.expiration(expirationDate).claims(extra)
					.signWith(Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes())).compact();

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

			Claims claims = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes())).build()
					.parseSignedClaims(token).getPayload();
			
			return claims;
		}

		/**
		 * Método para ver el usuario y el role que "contiene" el token. Lo primero que
		 * haremos es decodificar el claims. Si lanza una exception es que no es válido
		 * usando nuestra token secreto. Si es válido vemos la fecha de expiracion
		 * Sacamos el nombre y los roles y lo devolvemos.
		 * 
		 * @param token
		 * @return
		 * 
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
				throw new GlobalException("Imposible encontra un JWT Token");
			} catch (ExpiredJwtException e) {
				throw new GlobalException("Token expirado");
			} catch (NoSuchAlgorithmException e) {
				throw new GlobalException("Algoritmo no válido");
			}catch (MalformedJwtException e) {
				throw new GlobalException("Token malformado");
			}

			
			String username = claims.getSubject();
			String rol = (String) claims.get("rol");
			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(rol));

			return new UsernamePasswordAuthenticationToken(username, null, authorities);

		}

}
