package nl.tudelft.sem.template.example.domain.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttributeConvertersTest {

    @Test
    void convertEmailToDatabaseColumn() {
        EmailConverter emailConverter = new EmailConverter();
        Email e = new Email("ab@c.com");
        assertEquals(e.toString(),emailConverter.convertToDatabaseColumn(e));
    }

    @Test
    void convertEmailToEntityAttribute() {
        EmailConverter emailConverter = new EmailConverter();
        Email e = new Email("ab@c.com");
        assertEquals(e,emailConverter.convertToEntityAttribute("ab@c.com"));
    }

    @Test
    void convertHashToDatabaseColumn() {
        HashedPasswordAttributeConverter hashConverter = new HashedPasswordAttributeConverter();
        HashedPassword e = PasswordHashingService.hash("pass123");
        assertEquals(e.toString(),hashConverter.convertToDatabaseColumn(e));
    }

    @Test
    void convertHashToEntityAttribute() {
        HashedPasswordAttributeConverter hashConverter = new HashedPasswordAttributeConverter();
        HashedPassword e = PasswordHashingService.hash("pass123");
        assertEquals(e,hashConverter.convertToEntityAttribute(PasswordHashingService.hash("pass123").toString()));
    }

    @Test
    void convertUsernameToDatabaseColumn() {
        UsernameConverter usernameConverter = new UsernameConverter();
        Username e = new Username("a");
        assertEquals(e.toString(),usernameConverter.convertToDatabaseColumn(e));
    }

    @Test
    void convertUsernameToEntityAttribute() {
        UsernameConverter usernameConverter = new UsernameConverter();
        Username e = new Username("a");
        assertEquals(e,usernameConverter.convertToEntityAttribute("a"));
    }
}