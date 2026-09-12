package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.domain.model.Gender;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;
import edu.unisabana.tyvs.registry.infrastructure.persistence.RegistryRepository;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * PRUEBA DE INTEGRACION: el caso de uso {@link Registry} contra una base de
 * datos H2 real (no un mock). Verifica que la persistencia realmente funciona.
 *
 * Por que el nombre termina en IT y no en Test:
 * en este taller *Test.java son pruebas UNITARIAS (las ejecuta Surefire en
 * "mvn test") y *IT.java son de INTEGRACION o sistema (las ejecuta Failsafe en
 * "mvn verify"). Esta clase toca una base de datos, asi que no es unitaria.
 * Compare con {@link RegistryWithMockTest}, que prueba la misma clase sin BD.
 *
 * Cada prueba usa su propia base (regdb_usecase_it) y limpia en el @Before:
 * H2 con DB_CLOSE_DELAY=-1 sobrevive mientras viva la JVM, y las JVM se
 * reutilizan entre clases de prueba.
 */
public class RegistryIT {

    private static final String JDBC_URL = "jdbc:h2:mem:regdb_usecase_it;DB_CLOSE_DELAY=-1";

    private RegistryRepositoryPort repo;
    private Registry registry;

    @Before
    public void setup() throws Exception {
        RegistryRepository repository = new RegistryRepository(JDBC_URL);
        repository.initSchema(); // Arrange: crear tabla
        repository.deleteAll(); // Arrange: estado limpio para cada prueba

        repo = repository;
        registry = new Registry(repo); // Arrange: inyectar dependencia
    }

    @Test
    public void shouldRegisterValidPerson() throws Exception {
        // Arrange
        Person p1 = new Person("Ana", 100, 30, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p1);

        // Assert: el resultado Y el efecto real en la base de datos
        assertEquals(RegisterResult.VALID, result);
        assertTrue(repo.existsById(100));
    }

    @Test
    public void shouldPersistValidVoterAndRejectDuplicates() throws Exception {
        // Arrange
        Person p1 = new Person("Ana", 100, 30, Gender.FEMALE, true);
        Person p2 = new Person("AnaDos", 100, 40, Gender.FEMALE, true);

        // Act (primer registro)
        RegisterResult result1 = registry.registerVoter(p1);

        // Assert primer registro
        assertEquals(RegisterResult.VALID, result1);
        assertTrue(repo.existsById(100));

        // Act (segundo registro con el mismo id)
        RegisterResult result2 = registry.registerVoter(p2);

        // Assert: la unicidad la garantiza la base de datos, no el mock
        assertEquals(RegisterResult.DUPLICATED, result2);
    }

    @Test
    public void shouldReturnUnderageWhenPersonIsMinor() throws Exception {
        // Arrange
        Person p = new Person("Sara", 105, 17, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert: ni el resultado ni la persistencia deben ocurrir
        assertEquals(RegisterResult.UNDERAGE, result);
        assertFalse(repo.existsById(105));
    }

    @Test
    public void shouldReturnInvalidAgeWhenAgeIsImpossible() throws Exception {
        // Arrange
        Person p = new Person("Pedro", 106, 121, Gender.MALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert
        assertEquals(RegisterResult.INVALID_AGE, result);
        assertFalse(repo.existsById(106));
    }

    @Test
    public void shouldReturnDeadWhenPersonIsNotAlive() throws Exception {
        // Arrange
        Person p = new Person("Laura", 107, 50, Gender.FEMALE, false);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert
        assertEquals(RegisterResult.DEAD, result);
        assertFalse(repo.existsById(107));
    }

    @Test
    public void shouldReturnInvalidWhenIdIsNotPositive() throws Exception {
        // Arrange
        Person p = new Person("Eva", 0, 30, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert
        assertEquals(RegisterResult.INVALID, result);
    }
}
