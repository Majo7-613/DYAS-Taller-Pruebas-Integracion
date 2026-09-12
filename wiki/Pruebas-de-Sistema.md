# Pruebas de Sistema (HTTP) y Contract Testing (Pact)

## HTTP — `RegistryControllerIT`

Levanta la aplicación completa en un puerto aleatorio (`@SpringBootTest(RANDOM_PORT)`) y la
ejercita con `TestRestTemplate`, exactamente como lo haría un cliente real: no conoce clases
internas (`Registry`, `Person`), solo el contrato HTTP de `POST /register`.

| Método | Entrada | HTTP | Body |
|---|---|---|---|
| `shouldRegisterValidPerson` | id=100, edad=30, viva | 200 | `VALID` |
| `shouldReturnDuplicatedWhenIdAlreadyRegistered` | id=101 registrado dos veces | 200 | `DUPLICATED` |
| `shouldReturnUnderageWhenPersonIsMinor` | id=102, edad=17 | 200 | `UNDERAGE` |
| `shouldReturnDeadWhenPersonIsNotAlive` | id=103, `alive=false` | 200 | `DEAD` |
| `shouldReturnBadRequestWhenGenderIsNotValid` | `gender="X"` | 400 | `INVALID_INPUT` |
| `shouldReturnInvalidAgeWhenAgeIsImpossible` | id=105, edad=121 | 200 | `INVALID_AGE` |
| `shouldReturnInvalidWhenIdIsNotPositive` | id=0 | 200 | `INVALID` |
| `shouldReturnBadRequestWhenJsonIsMalformed` | JSON con llave sin cerrar | 400 | `MALFORMED_JSON` |
| `shouldReturnValidationErrorWhenGenderIsMissing` | sin campo `gender` | 422 | `VALIDATION_ERROR` |
| `shouldReturnValidationErrorWhenNameIsBlank` | `name=""` | 422 | `VALIDATION_ERROR` |

Nota sobre los códigos: `200` con un `RegisterResult` en el cuerpo es un resultado de **negocio**
normal (`INVALID`, `INVALID_AGE`, `UNDERAGE`, `DEAD`, `DUPLICATED` son respuestas válidas, no
errores); `400` es un dato sintáctica o semánticamente reconocible pero fuera de rango
(`gender` inválido, JSON roto); `422` es sintaxis correcta con datos ausentes según Bean
Validation (`name`/`gender` en blanco) — ver [[Defectos-y-Diseno]].

## Contract testing con Pact

### Consumidor — `CertificadoServicePactTest`

`edu.unisabana.tyvs.certificados` es el consumidor: pide a la Registraduría registrar un votante
y solo emite el certificado (`"CERT-" + id`) si la respuesta es `VALID`. Declara 3 interacciones
contra un servidor simulado, generando `target/pacts/certificados-registraduria.json`:

| Interacción | `given` (estado) | Entrada | Respuesta declarada | Assert del consumidor |
|---|---|---|---|---|
| `votanteValido` | sin votante id=900 | id=900, edad=30 | 200 `VALID` | `emitirCertificado(...)` = `"CERT-900"` |
| `votanteDuplicado` | ya existe id=901 | id=901, edad=40 | 200 `DUPLICATED` | certificado = `null` |
| `votanteMenorDeEdad` | sin votante id=902 | id=902, edad=17 | 200 `UNDERAGE` | certificado = `null` |

### Proveedor — `RegistraduriaProviderPactIT`

Lee el pacto de `target/pacts/` y reproduce las 3 interacciones contra la aplicación real
levantada (`@SpringBootTest(RANDOM_PORT)`), sin que el consumidor se levante nunca. Cada `given`
tiene su `@State` correspondiente (`sinVotante900`, `conVotante901`, `sinVotante902`), responsable
de dejar la base de datos en el estado que esa interacción necesita.

### Por qué hace falta, si ya hay pruebas de sistema

`RegistryControllerIT` verifica al proveedor **contra sí mismo**: si alguien cambia el formato de
respuesta (por ejemplo, de texto plano a `{"resultado":"VALID"}`) y actualiza la prueba junto con
el código, esa prueba sigue en verde. Pact verifica al proveedor contra lo que el consumidor
**realmente** espera, sin que ambos se levanten nunca al mismo tiempo — ese es el hueco que
ninguna otra técnica de este taller cubre.
