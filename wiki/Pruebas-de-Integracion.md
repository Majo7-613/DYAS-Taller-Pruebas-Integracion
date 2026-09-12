# Pruebas de Integración

## Con base de datos H2 real — `RegistryIT`

Instancia un `RegistryRepository` real sobre `jdbc:h2:mem:regdb_usecase_it`, inicializa el
esquema y limpia el estado antes de cada prueba (`@Before`). Cada prueba verifica tanto el
`RegisterResult` devuelto como el efecto real en la base de datos (`repo.existsById(...)`).

| Método | Entrada | Verifica |
|---|---|---|
| `shouldRegisterValidPerson` | id=100, edad=30, viva | `VALID` + persistido en H2 |
| `shouldPersistValidVoterAndRejectDuplicates` | mismo id dos veces | 1ª `VALID`, 2ª `DUPLICATED` |
| `shouldReturnUnderageWhenPersonIsMinor` | id=105, edad=17 | `UNDERAGE`, no persiste |
| `shouldReturnInvalidAgeWhenAgeIsImpossible` | id=106, edad=121 | `INVALID_AGE`, no persiste |
| `shouldReturnDeadWhenPersonIsNotAlive` | id=107, `alive=false` | `DEAD`, no persiste |
| `shouldReturnInvalidWhenIdIsNotPositive` | id=0 | `INVALID` |

## Con PostgreSQL real (Testcontainers) — `RegistryRepositoryPostgresIT`

Levanta un contenedor Docker `postgres:16-alpine` (estático, una vez por clase) y corre el mismo
código de producción contra un motor real, no una aproximación. Requiere Docker; si no está
disponible, `@EnabledIf("hayDocker")` **salta** la clase en vez de fallar el build.

| Método | Qué demuestra |
|---|---|
| `shouldPersistAndRejectDuplicate` | mismo comportamiento VALID/DUPLICATED que en H2 |
| `shouldRoundTripRecord` | `save` + `findById` devuelven exactamente los mismos datos |
| `shouldRejectOversizedName` | un nombre de 150 caracteres contra `VARCHAR(100)` falla en tiempo de ejecución |
| `shouldResolveQuotedLowercaseIdentifier` | **la divergencia central**: `SELECT "name"` resuelve en PostgreSQL (pliega a minúsculas) y fallaría en H2 (pliega a MAYÚSCULAS) contra el mismo `CREATE TABLE` |
| `shouldBeRunningOnPostgres` | sanity check: `getMetaData().getDatabaseProductName()` |

## Con Mockito — ver [[Pruebas-con-Mockito]]

## De sistema (HTTP) — ver [[Pruebas-de-Sistema]]

## Contract testing (Pact) — ver [[Pruebas-de-Sistema]]
