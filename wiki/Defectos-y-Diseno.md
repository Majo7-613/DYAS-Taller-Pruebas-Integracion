# Defectos y decisiones de diseño

El detalle completo de los defectos reales encontrados (entrada, resultado esperado/obtenido con
evidencia de log, causa, prueba que lo verifica) vive en
[`defectos.md`](../defectos.md) en la raíz del repositorio, siguiendo el formato de
[`defectos_template.md`](../defectos_template.md). Resumen:

| ID | Caso | Resultado esperado | Resultado obtenido (antes) | Estado |
|----|------|---------------------|------------------------------|--------|
| 01 | Falta el campo `gender` | `422 VALIDATION_ERROR` | `500 Internal Server Error` (`NullPointerException` no manejada) | Resuelto |
| 02 | `name` vacío (`""`) | `422 VALIDATION_ERROR` | `200 OK` / `VALID` (se aceptaba sin validar, sin ningún error) | Resuelto |

Ambos se corrigieron con el mismo mecanismo: `@NotBlank` en `PersonDTO.name`/`gender`, `@Valid`
en `RegistryController`, y un nuevo `@ExceptionHandler(MethodArgumentNotValidException.class)`
en `RegistryExceptionHandler` que responde `422`.

## Decisión de diseño: por qué `id` y `age` NO se validan con `@Valid`

Deliberadamente no se anotó `id` ni `age` en `PersonDTO`, aunque también son datos de entrada.
`Registry.registerVoter` ya es dueño de esas reglas de negocio (`id<=0` → `INVALID`; edad fuera
de rango → `INVALID_AGE`/`UNDERAGE`), y ya existen pruebas H2 ([[Pruebas-de-Integracion]]) y HTTP
([[Pruebas-de-Sistema]]) que verifican una respuesta `200` con el `RegisterResult` correcto para
esos casos. Si Bean Validation interceptara `id`/`age` antes de llegar al controlador, esos
mismos casos pasarían a responder `422` genérico en lugar de exponer el resultado de negocio
específico, duplicando la misma regla en dos capas con respuestas distintas según la puerta de
entrada por la que entre el dato. Bean Validation se reservó para lo que ninguna capa de dominio
valida: la sola **presencia** de `name` y `gender`.
