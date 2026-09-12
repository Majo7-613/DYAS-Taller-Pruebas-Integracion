# Pruebas con Mockito — `RegistryWithMockTest`

Prueba **unitaria** (no de integración: si todo colaborador está simulado, no se integra nada)
de `Registry.registerVoter`, con `RegistryRepositoryPort` mockeado. Cubre exhaustivamente todas
las ramas de la regla de negocio, incluidos ambos límites de edad.

| Método | `when(...)` / entrada | `verify(...)` |
|---|---|---|
| `shouldReturnDuplicatedWhenRepoSaysExists` | `existsById(7)=true` | `DUPLICATED`, `never().save(...)` |
| `shouldSaveWhenPersonIsValid` | `existsById(8)=false` | `VALID`, `save(8,"Luis",30,true)` una vez |
| `shouldWrapPersistenceFailure` | `existsById(9)` lanza `SQLException` | `RegistryPersistenceException` con esa causa |
| `shouldRejectUnderageWithoutTouchingRepository` | edad=17 | `UNDERAGE`, `verifyNoInteractions(repo)` |
| `shouldReturnInvalidWhenPersonIsNull` | `null` | `INVALID`, sin interacciones |
| `shouldReturnInvalidWhenIdIsNotPositive` | id=0 | `INVALID`, sin interacciones |
| `shouldReturnDeadWhenPersonIsNotAlive` | `alive=false` | `DEAD`, sin interacciones |
| `shouldReturnInvalidAgeWhenAgeIsNegative` | edad=-1 | `INVALID_AGE`, sin interacciones |
| `shouldReturnInvalidAgeWhenAgeExceedsMaximum` | edad=121 | `INVALID_AGE`, sin interacciones |
| `shouldReturnUnderageWhenAgeIsZero` | edad=0 (límite) | `UNDERAGE`, sin interacciones |
| `shouldAcceptTheMaximumAge` | edad=120 (límite) | `VALID`, `save(...)` |

## Por qué importan los dos límites (0 y 120)

La frontera entre `INVALID_AGE` y `UNDERAGE` es la edad 0; la frontera entre `UNDERAGE` y `VALID`
en el extremo superior es 120. Sin una prueba en cada límite exacto, cambiar `<` por `<=` (o
viceversa) no rompería ninguna prueba: la mutación sobreviviría. Con estas dos pruebas, sí.

## Comparación con `RegistryIT`

| | `RegistryWithMockTest` | `RegistryIT` |
|---|---|---|
| Colaborador | Mock del puerto | `RegistryRepository` real sobre H2 |
| Verifica | Que `Registry` **colabora** bien (llamó a `save`, no lo llamó) | Que los datos **quedaron** realmente guardados |
| Detecta | Errores de lógica de negocio | Errores de SQL, esquema, tipos |
| No detecta | Que el `INSERT` esté mal escrito | Poco; pero es más lenta |

Se necesitan ambas: un mock siempre responde lo que se le dijo que respondiera, incluso si la
base de datos real haría otra cosa.
