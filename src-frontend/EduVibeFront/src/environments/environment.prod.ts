/**
 * La URL de la API en producción se inyecta al construir la imagen o al
 * desplegar. Dejarla escrita aquí obligaría a recompilar para cambiar de
 * entorno, y fue justo lo que dejó el despliegue anterior apuntando a una
 * dirección inexistente.
 */
export const environment = {
  production: true,
  apiUrl: 'REEMPLAZAR_CON_LA_URL_DE_LA_API',
};
