# Registro de Defectos — Registraduría

Este documento recopila los **defectos reales detectados durante las pruebas de sistema (HTTP)** del proyecto `registraduria`, al implementar el "Reto adicional con Sistemas" del README (validación con `@Valid` en `PersonDTO`). Antes de corregirlos se ejecutó cada caso contra el código sin validar para capturar el "Resultado obtenido" real (no simulado).

---

## Formato 1: Lista detallada (narrativa)

### Defecto 01 — `NullPointerException` no manejada cuando falta el campo `gender` *(Prueba de sistema REST)*

- **Capa afectada:** Delivery (`RegistryController.register`)
- **Caso de prueba:** `RegistryControllerIT.shouldReturnValidationErrorWhenGenderIsMissing()`
- **Entrada:**

```json
{ "name": "Eva", "id": 108, "age": 30, "alive": true }
```

(JSON válido, pero sin el campo `"gender"`)

- **Resultado esperado:** `422 Unprocessable Entity` (dato semánticamente inválido del cliente)
- **Resultado obtenido (antes de corregir):** `500 Internal Server Error`

```
java.lang.AssertionError: expected:<422 UNPROCESSABLE_ENTITY> but was:<500 INTERNAL_SERVER_ERROR>
	at edu.unisabana.tyvs.registry.delivery.rest.RegistryControllerIT.shouldReturnValidationErrorWhenGenderIsMissing(RegistryControllerIT.java:149)
```

- **Causa probable:** Jackson deserializa el JSON sin fallar (el campo simplemente queda `null` en `PersonDTO`). El controlador hace `Gender.valueOf(dto.getGender())`, y `Gender.valueOf(null)` lanza `NullPointerException`, **no** `IllegalArgumentException`. `RegistryExceptionHandler` solo tenía un `@ExceptionHandler(IllegalArgumentException.class)`, así que la `NullPointerException` no era capturada por ningún `@ExceptionHandler` existente y Spring respondía con el 500 por defecto.
- **Tipo de prueba:** Sistema (`TestRestTemplate`)
- **Estado:** **Resuelto** — se agregó `@NotBlank` a `PersonDTO.gender`, `@Valid` en el parámetro del controlador, y un nuevo `@ExceptionHandler(MethodArgumentNotValidException.class)` que responde `422 VALIDATION_ERROR`. Verificado por `RegistryControllerIT.shouldReturnValidationErrorWhenGenderIsMissing()`.
- **Prioridad:** Alta

> **Por qué importaba.** Un `NullPointerException` filtrándose hasta el cliente como un 500 genérico es exactamente el tipo de defecto que rompe la confianza en una API: el cliente no tiene forma de saber que el problema fue "olvidaste un campo" y no "el servidor se cayó". Además, `IllegalArgumentException` y `NullPointerException` no tienen relación de herencia entre sí — es un error común asumir que "cualquier entrada inválida" cae en la misma excepción.

---

### Defecto 02 — Un votante con `name` vacío se registra como válido sin ninguna advertencia *(Prueba de sistema REST)*

- **Capa afectada:** Delivery (`RegistryController.register`) / Dominio (`Person`, `Registry.registerVoter`)
- **Caso de prueba:** `RegistryControllerIT.shouldReturnValidationErrorWhenNameIsBlank()`
- **Entrada:**

```json
{ "name": "", "id": 109, "age": 30, "gender": "FEMALE", "alive": true }
```

- **Resultado esperado:** `422 Unprocessable Entity` (un nombre vacío es un dato de entrada inválido)
- **Resultado obtenido (antes de corregir):** `200 OK`

```
java.lang.AssertionError: expected:<422 UNPROCESSABLE_ENTITY> but was:<200 OK>
	at edu.unisabana.tyvs.registry.delivery.rest.RegistryControllerIT.shouldReturnValidationErrorWhenNameIsBlank(RegistryControllerIT.java:163)
```

- **Causa probable:** Ninguna capa valida el contenido de `name`: `PersonDTO` no tenía restricciones, `Person` es un value object sin validación propia, y la columna `name VARCHAR(100) NOT NULL` de H2 solo rechaza `NULL`, no una cadena vacía (`""` es un valor válido para una columna `NOT NULL`). El resultado: la Registraduría inscribe silenciosamente un votante sin nombre y responde `VALID`, como si el dato fuera correcto. **Nota:** esta investigación corrigió una hipótesis inicial (se esperaba un `503 PERSISTENCE_ERROR` por violar `NOT NULL`); la evidencia real mostró que el defecto es más silencioso — no hay ningún error, el dato simplemente se acepta.
- **Tipo de prueba:** Sistema (`TestRestTemplate`)
- **Estado:** **Resuelto** — mismo mecanismo del Defecto 01: `@NotBlank` en `PersonDTO.name` (que rechaza tanto ausente como vacío/solo-espacios) + `@Valid` + el nuevo handler de `MethodArgumentNotValidException`. Verificado por `shouldReturnValidationErrorWhenNameIsBlank()`.
- **Prioridad:** Media

> **Por qué importaba.** Este es más engañoso que el anterior: no hay ninguna excepción ni log de error, el sistema responde `200 VALID` como si todo estuviera bien. Un defecto que no genera ningún síntoma visible es el más difícil de encontrar sin una prueba dedicada — por eso apareció recién al escribir la prueba de "reto adicional" y no antes.

---

## Formato 2: Tabla de defectos (bug tracking)

| ID | Caso de Prueba | Capa | Resultado Esperado | Resultado Obtenido | Tipo | Estado | Prioridad |
|----|----------------|------|--------------------|--------------------|------|----------|------------|
| 01 | Falta el campo `gender` | Delivery | `422 VALIDATION_ERROR` | `500 Internal Server Error` | Sistema (HTTP) | Resuelto | Alta |
| 02 | `name` vacío (`""`) | Delivery/Dominio | `422 VALIDATION_ERROR` | `200 OK` / `VALID` (aceptado sin validar) | Sistema (HTTP) | Resuelto | Media |

---

## Nota de diseño (no es un defecto)

Al corregir ambos casos se decidió **no** anotar `id` ni `age` en `PersonDTO` con Bean Validation, aunque también son datos "de entrada". La razón: `Registry.registerVoter` ya es dueño de esas reglas de negocio (`id<=0` → `INVALID`; edad fuera de rango → `INVALID_AGE`/`UNDERAGE`), y ya existen pruebas H2 (`RegistryIT`) y HTTP (`RegistryControllerIT`) que verifican una respuesta `200` con el `RegisterResult` correcto para esos casos. Si Bean Validation interceptara `id`/`age` antes de llegar al controlador, esos mismos casos pasarían a responder `422` genérico en lugar de exponer el resultado de negocio específico (`INVALID`, `INVALID_AGE`, `UNDERAGE`), duplicando la regla en dos capas con respuestas distintas según la puerta de entrada. Bean Validation se usó únicamente para lo que ninguna capa de dominio valida: presencia de `name` y `gender`.

---

## Convenciones de Estado

| Estado | Significado |
|---------|-------------|
| **Abierto** | El defecto fue detectado pero no corregido. |
| **En progreso** | El defecto se encuentra en análisis o corrección. |
| **Resuelto** | El defecto fue corregido y validado mediante pruebas. |

---

## Observaciones

- Los dos defectos aparecieron en la misma capa (Delivery) y por la misma causa raíz: `PersonDTO` no tenía ninguna restricción de Bean Validation pese a que `spring-boot-starter-validation` ya estaba en el `pom.xml` desde el inicio del taller.
- El Defecto 02 es más grave desde el punto de vista de calidad de datos que el 01: no lanza ninguna excepción, por lo que ninguna prueba anterior a `RegistryControllerIT.shouldReturnValidationErrorWhenNameIsBlank()` lo había expuesto.
- La lógica de negocio en `Registry.registerVoter` (ya cubierta exhaustivamente por `RegistryWithMockTest`, incluidos ambos límites de edad) no mostró defectos nuevos al escribir las pruebas H2 y HTTP adicionales — la ausencia de hallazgos ahí es consistente con que esa clase ya tenía la cobertura más completa del proyecto antes de este trabajo.
