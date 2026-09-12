# Matriz de pruebas de integración

| Caso | Entrada | Resultado esperado | Tipo | Test que lo valida |
|------|---------|--------------------|------|--------------------|
| Persona válida | id=100, edad=30, viva | `VALID` | H2 | `RegistryIT.shouldRegisterValidPerson()` |
| Persona duplicada | id=100 registrado dos veces | `DUPLICATED` | H2 | `RegistryIT.shouldPersistValidVoterAndRejectDuplicates()` |
| Persona menor de edad | id=105, edad=17 | `UNDERAGE` | H2 | `RegistryIT.shouldReturnUnderageWhenPersonIsMinor()` |
| Edad imposible | id=106, edad=121 | `INVALID_AGE` | H2 | `RegistryIT.shouldReturnInvalidAgeWhenAgeIsImpossible()` |
| Persona fallecida | id=107, `alive=false` | `DEAD` | H2 | `RegistryIT.shouldReturnDeadWhenPersonIsNotAlive()` |
| ID inválido | id=0 | `INVALID` | H2 | `RegistryIT.shouldReturnInvalidWhenIdIsNotPositive()` |
| Duplicado sin base de datos | el mock dice que el id existe | `DUPLICATED` | Mock | `RegistryWithMockTest.shouldReturnDuplicatedWhenRepoSaysExists()` |
| Registro exitoso (mock) | el mock dice que el id no existe | `VALID` + `save()` invocado | Mock | `RegistryWithMockTest.shouldSaveWhenPersonIsValid()` |
| Fallo de persistencia | el puerto lanza `SQLException` | `RegistryPersistenceException` | Mock | `RegistryWithMockTest.shouldWrapPersistenceFailure()` |
| Edad imposible (negativa) | edad=-1 | `INVALID_AGE` | Mock | `RegistryWithMockTest.shouldReturnInvalidAgeWhenAgeIsNegative()` |
| Edad imposible (excede máximo) | edad=121 | `INVALID_AGE` | Mock | `RegistryWithMockTest.shouldReturnInvalidAgeWhenAgeExceedsMaximum()` |
| Valor límite inferior | edad=0 | `UNDERAGE` | Mock | `RegistryWithMockTest.shouldReturnUnderageWhenAgeIsZero()` |
| Valor límite superior | edad=120 | `VALID` | Mock | `RegistryWithMockTest.shouldAcceptTheMaximumAge()` |
| Persona nula | `p == null` | `INVALID` | Mock | `RegistryWithMockTest.shouldReturnInvalidWhenPersonIsNull()` |
| Registro exitoso (HTTP) | id=100, edad=30, viva | `200` / `VALID` | HTTP | `RegistryControllerIT.shouldRegisterValidPerson()` |
| Duplicado (HTTP) | id=101 registrado dos veces | `200` / `DUPLICATED` | HTTP | `RegistryControllerIT.shouldReturnDuplicatedWhenIdAlreadyRegistered()` |
| Menor de edad (HTTP) | id=102, edad=17 | `200` / `UNDERAGE` | HTTP | `RegistryControllerIT.shouldReturnUnderageWhenPersonIsMinor()` |
| Fallecido (HTTP) | id=103, `alive=false` | `200` / `DEAD` | HTTP | `RegistryControllerIT.shouldReturnDeadWhenPersonIsNotAlive()` |
| Género inválido | `gender="X"` | `400` / `INVALID_INPUT` | HTTP | `RegistryControllerIT.shouldReturnBadRequestWhenGenderIsNotValid()` |
| Edad imposible (HTTP) | id=105, edad=121 | `200` / `INVALID_AGE` | HTTP | `RegistryControllerIT.shouldReturnInvalidAgeWhenAgeIsImpossible()` |
| ID inválido (HTTP) | id=0 | `200` / `INVALID` | HTTP | `RegistryControllerIT.shouldReturnInvalidWhenIdIsNotPositive()` |
| JSON malformado | llave sin cerrar | `400` / `MALFORMED_JSON` | HTTP | `RegistryControllerIT.shouldReturnBadRequestWhenJsonIsMalformed()` |
| Falta `gender` (defecto 01) | JSON válido sin `gender` | `422` / `VALIDATION_ERROR` | HTTP | `RegistryControllerIT.shouldReturnValidationErrorWhenGenderIsMissing()` |
| `name` vacío (defecto 02) | `name=""` | `422` / `VALIDATION_ERROR` | HTTP | `RegistryControllerIT.shouldReturnValidationErrorWhenNameIsBlank()` |
| Persistencia real | `save` + `findById` | mismos datos de vuelta | Testcontainers | `RegistryRepositoryPostgresIT.shouldRoundTripRecord()` |
| Nombre demasiado largo | 150 caracteres vs `VARCHAR(100)` | excepción en tiempo de ejecución | Testcontainers | `RegistryRepositoryPostgresIT.shouldRejectOversizedName()` |
| Dialecto SQL divergente | `SELECT "name"` | resuelve en PostgreSQL, fallaría en H2 | Testcontainers | `RegistryRepositoryPostgresIT.shouldResolveQuotedLowercaseIdentifier()` |
| Contrato: votante válido | pacto id=900 | interacción verificada | Pact | `RegistraduriaProviderPactIT.verificarPacto()` (interacción `votanteValido`) |
| Contrato: votante duplicado | pacto id=901 | interacción verificada | Pact | `RegistraduriaProviderPactIT.verificarPacto()` (interacción `votanteDuplicado`) |
| Contrato: votante menor de edad | pacto id=902 | interacción verificada | Pact | `RegistraduriaProviderPactIT.verificarPacto()` (interacción `votanteMenorDeEdad`) |

Ver el detalle de cada grupo en [[Pruebas-de-Integracion]], [[Pruebas-con-Mockito]] y
[[Pruebas-de-Sistema]].
