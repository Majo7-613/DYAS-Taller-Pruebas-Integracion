# Reflexión final

## ¿Qué capas fueron más difíciles de probar y por qué?

La capa de infraestructura (`RegistryRepository`) fue la más difícil, no por escribir las
pruebas en sí, sino por decidir **contra qué motor** probarlas. H2 es rápida pero no es
PostgreSQL: el mismo `SELECT "name"` resuelve distinto en cada uno porque el estándar SQL no fija
a qué caja se pliega un identificador sin comillas. Sin Testcontainers, esa divergencia habría
sido invisible hasta producción. La segunda dificultad fue la capa de entrega (`delivery`): ahí
aparecieron los dos defectos reales del taller (ver [[Defectos-y-Diseno]]), y no porque el código
"se viera mal" a simple vista, sino porque ninguna prueba anterior ejercitaba el camino de un
campo ausente o vacío — un caso que no genera ninguna excepción visible (`name=""` se aceptaba
silenciosamente como `VALID`) es más difícil de encontrar que uno que sí falla.

## ¿Qué beneficios se observan al usar mocks frente a H2 o base real?

Los mocks permiten aislar exactamente la pregunta que se quiere responder: "¿`Registry` toma la
decisión correcta y colabora bien con su puerto?", sin que un detalle de SQL, de transacciones o
de arranque de una base de datos interfiera con esa pregunta. Son también donde se puede simular
lo que una base de datos real casi nunca deja provocar a propósito — un `SQLException` a mitad de
una operación (`shouldWrapPersistenceFailure`). El costo es que un mock siempre responde lo que
se le dijo que respondiera: si la implementación real de `RegistryRepository` tuviera un `INSERT`
mal escrito, ningún mock lo notaría jamás. Por eso ambas pruebas (`RegistryWithMockTest` y
`RegistryIT`) prueban la misma clase por razones distintas, y ninguna reemplaza a la otra.

## ¿Cómo se mejoraría el diseño de `RegistryController` o `RegistryRepository`?

En `RegistryController`, la validación de forma (`@NotBlank`) y la validación de negocio
(`Registry.registerVoter`) conviven hoy en dos mecanismos distintos (Bean Validation vs. lógica
de dominio) que fue necesario coordinar a mano para que no se pisaran (ver la nota de diseño en
[[Defectos-y-Diseno]] sobre por qué `id`/`age` quedaron fuera de `@Valid`). Un DTO más rico —por
ejemplo, con su propio `Gender` tipado en lugar de un `String` libre que se convierte con
`Gender.valueOf(...)` dentro del controlador— movería esa conversión (y su posible
`IllegalArgumentException`) fuera de la lógica de negocio del controlador y la haría declarativa.
En `RegistryRepository`, separar la conexión (`getConnection`) detrás de un `DataSource`
inyectado en lugar de construir la conexión desde `jdbcUrl/username/password` en cada llamado
simplificaría las pruebas y acercaría el código a cómo Spring maneja normalmente la persistencia.

## ¿Qué se aprendió sobre integración continua al ejecutar las pruebas con Maven y JaCoCo?

Que "todo pasa" y "está probado" no son lo mismo: `mvn test` reporta éxito sin haber ejecutado
ni una sola prueba de integración, porque Surefire ignora los `*IT.java` por convención — el
`BUILD SUCCESS` de un `mvn test` en este proyecto no dice nada sobre H2, PostgreSQL, HTTP ni Pact.
Y un número de cobertura tampoco es automáticamente "el" número: el `pom.xml` original generaba
dos reportes de JaCoCo separados (unitario e integración) que, tomados por separado, nunca
alcanzaban el 80 % exigido aunque las pruebas subyacentes estuvieran completas — hizo falta
fusionar explícitamente los dos archivos `.exec` para obtener el número real combinado (ver
[[Resultados-cobertura]]). En un pipeline de CI, cualquiera de estos dos detalles (fase
equivocada, o reporte sin fusionar) puede dar una falsa sensación de seguridad durante meses.
