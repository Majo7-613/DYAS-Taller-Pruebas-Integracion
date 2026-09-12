# Arquitectura limpia

```
edu/unisabana/tyvs/registry/
 ├─ RegistryApplication.java          # arranque de Spring Boot
 ├─ config/RegistryConfig.java        # cableado: que implementacion se inyecta
 ├─ domain/model/                     # Person, Gender, RegisterResult (reglas puras)
 ├─ application/
 │   ├─ usecase/                      # Registry, RegistryPersistenceException
 │   └─ port/out/                     # RegistryRepositoryPort (interfaz, no implementacion)
 ├─ infrastructure/persistence/       # RegistryRepository (JDBC), RegistryRecord
 └─ delivery/rest/                    # RegistryController, RegistryExceptionHandler

edu/unisabana/tyvs/certificados/      # consumidor Pact: CertificadoService, RegistraduriaClient
```

## Dirección de las dependencias

`delivery` → `application` → `domain`, y `infrastructure` implementa el puerto que
`application` define (`RegistryRepositoryPort`). El caso de uso (`Registry`) nunca conoce JDBC,
SQL ni HTTP: solo conoce la interfaz `RegistryRepositoryPort`. Eso es lo que permite:

- Sustituir la implementación real (`RegistryRepository` sobre H2) por un mock de Mockito sin
  tocar `Registry` — de ahí que `RegistryWithMockTest` y `RegistryIT` prueben la **misma** clase
  de dos formas distintas.
- Cambiar de H2 a PostgreSQL (`RegistryRepositoryPostgresIT`) sin cambiar una sola línea de
  `Registry` ni de `RegistryController`.
- Traducir errores en la frontera correcta: `RegistryRepository` lanza excepciones de bajo nivel
  (`SQLException`), `Registry` las envuelve en `RegistryPersistenceException` (una excepción de
  aplicación), y `RegistryExceptionHandler` decide el código HTTP — cada capa solo sabe traducir
  su propio nivel de abstracción, nunca el de dos capas más abajo.

## Capa de entrega (`delivery`)

`RegistryController` construye un `Person` de dominio a partir de un `PersonDTO` (el contrato
HTTP), y solo entonces llama al caso de uso. El DTO tiene sus propias reglas de validación de
forma (`@NotBlank` en `name`/`gender`, ver [[Pruebas-de-Sistema]]), separadas de las reglas de
negocio que vive en `Registry` (edad, estado civil, duplicados). Esa separación evita que una
regla se valide dos veces con resultados distintos según la puerta de entrada.
