# Registraduría — Pruebas de Integración y Sistema

**Integrante:** María José Almanza Caviedes — mariaalmca@unisabana.edu.co (ver [`integrantes.txt`](integrantes.txt))

 El caso de uso central es `Registry.registerVoter(Person p)`, con esta regla de negocio, en este orden:

1. Persona nula, o `id <= 0` → `INVALID`
2. Persona fallecida (`alive = false`) → `DEAD`
3. Edad imposible (`age < 0` o `age > 120`) → `INVALID_AGE`
4. Edad menor a 18 → `UNDERAGE`
5. `id` ya registrado → `DUPLICATED`
6. En cualquier otro caso, se guarda el votante → `VALID`

## Arquitectura

```
registraduria/src/main/java/edu/unisabana/tyvs/
 ├─ registry/
 │   ├─ RegistryApplication.java          # arranque de Spring Boot
 │   ├─ config/RegistryConfig.java        # cableado: que implementacion se inyecta
 │   ├─ domain/model/                     # Person, Gender, RegisterResult (reglas puras)
 │   ├─ application/
 │   │   ├─ usecase/                      # Registry, RegistryPersistenceException
 │   │   └─ port/out/                     # RegistryRepositoryPort
 │   ├─ infrastructure/persistence/       # RegistryRepository (JDBC), RegistryRecord
 │   └─ delivery/rest/                    # RegistryController, RegistryExceptionHandler
 └─ certificados/                         # consumidor Pact: CertificadoService, RegistraduriaClient
```

## Cómo ejecutar

```bash
cd registraduria
mvn test      # solo unitarias (Surefire): *Test.java
mvn verify    # unitarias + integracion + sistema (Surefire + Failsafe): *Test.java + *IT.java
```

## Pruebas implementadas

| Clase | Tipo | Casos | Qué cubre |
|---|---|---|---|
| `RegistryWithMockTest` | Unitaria (Mockito) | 11 | Todas las ramas de `Registry.registerVoter`, incluidos ambos límites de edad (0 y 120) |
| `RegistryIT` | Integración (H2 real) | 6 | `VALID`, `DUPLICATED`, `UNDERAGE`, `INVALID_AGE`, `DEAD`, `INVALID` contra base de datos real |
| `RegistryControllerIT` | Sistema (HTTP) | 10 | Los 6 resultados de negocio vía HTTP + género inválido, JSON malformado, y 2 casos de validación |
| `RegistryRepositoryPostgresIT` | Integración (Testcontainers) | 5 | Divergencia real H2 vs. PostgreSQL en el plegado de identificadores, entre otros |
| `CertificadoServicePactTest` + `RegistraduriaProviderPactIT` | Contract testing (Pact) | 3 interacciones | Votante válido, duplicado y menor de edad, verificadas en ambos lados del contrato |
