# Tipos de pruebas

| | Unitaria | Integración | Sistema | Contrato (Pact) |
|---|---|---|---|---|
| Qué verifica | Una clase aislada, con sus colaboradores simulados | Que dos o más módulos reales trabajen juntos (ej. `Registry` + BD real) | El comportamiento de punta a punta, por la interfaz pública (HTTP) | Que proveedor y consumidor sigan de acuerdo, sin levantar ambos a la vez |
| Colaboradores | Mocks (Mockito) | Reales (H2 o PostgreSQL) | Reales, aplicación completa levantada | Servidor simulado (consumidor) / aplicación real (proveedor) |
| Ejemplo en este proyecto | `RegistryWithMockTest` | `RegistryIT`, `RegistryRepositoryPostgresIT` | `RegistryControllerIT` | `CertificadoServicePactTest` + `RegistraduriaProviderPactIT` |
| Convención de nombre | `*Test.java` | `*IT.java` | `*IT.java` | consumidor `*Test.java`, proveedor `*IT.java` |
| Ejecutor / fase Maven | Surefire / `mvn test` | Failsafe / `mvn verify` | Failsafe / `mvn verify` | consumidor: Surefire (`test`); proveedor: Failsafe (`verify`) |
| Velocidad | Milisegundos | Décimas de segundo (H2) a segundos (Testcontainers) | Segundos (levanta el contexto Spring completo) | Milisegundos (consumidor, mock server); segundos (proveedor) |
| Qué detecta | Errores de lógica de negocio | Errores de SQL, esquema, tipos, transacciones | Errores de serialización JSON, códigos HTTP, cableado completo | Cambios de contrato que rompen al otro lado, sin que ninguna prueba local lo note |
| Qué NO detecta | Que el `INSERT` esté mal escrito | Divergencias de dialecto SQL específicas de un motor distinto (ver `RegistryRepositoryPostgresIT`) | Que el consumidor real siga esperando el mismo formato de respuesta | Errores de lógica de negocio interna |

## Por qué se necesitan todas

Un mock siempre responde lo que se le dijo que respondiera, incluso si la base de datos real
haría otra cosa distinta. Una prueba de sistema contra el propio proveedor no se entera de qué
espera el consumidor, porque valida al proveedor **contra sí mismo**. Y H2, aunque rápida, no es
PostgreSQL: el mismo `SELECT "name"` resuelve en un motor y falla en el otro por cómo cada uno
pliega los identificadores sin comillas (ver `RegistryRepositoryPostgresIT.shouldResolveQuotedLowercaseIdentifier`).

Cada capa de prueba cierra un hueco que las demás dejan abierto.
