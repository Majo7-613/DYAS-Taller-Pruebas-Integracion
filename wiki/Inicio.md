# Inicio

## Dominio

Este taller implementa una **Registraduría** simplificada: un servicio que recibe los datos de
una persona y decide si puede registrarse como votante. El caso de uso central es
`Registry.registerVoter(Person p)`, que aplica las siguientes reglas de negocio, en este orden:

1. Persona nula, o `id <= 0` → `INVALID`
2. Persona fallecida (`alive = false`) → `DEAD`
3. Edad imposible (`age < 0` o `age > 120`) → `INVALID_AGE`
4. Edad menor a 18 → `UNDERAGE`
5. Si el `id` ya existe en la base de datos → `DUPLICATED`
6. En cualquier otro caso, se guarda el votante → `VALID`

El orden importa: una edad imposible (`-1`) se descarta **antes** de preguntar si es menor de
edad, para no confundir un dato mal capturado con un menor real.

## Propósito del taller

Diseñar, implementar y ejecutar **pruebas de integración** y **pruebas de sistema** sobre esta
aplicación, usando JUnit, Mockito, H2, Testcontainers, Spring Boot Test y Pact, dentro de una
arquitectura limpia (`domain`, `application`, `infrastructure`, `delivery`).

## Integrante

María José Almanza Caviedes — mariaalmca@unisabana.edu.co

## Cómo ejecutar

```bash
cd registraduria
mvn test      # solo unitarias (Surefire): *Test.java
mvn verify    # unitarias + integración + sistema (Surefire + Failsafe): *Test.java + *IT.java
```

`RegistryRepositoryPostgresIT` (Testcontainers) requiere Docker corriendo; si Docker no está
disponible, esa clase se **salta** limpiamente (`@EnabledIf`), no falla el build.
