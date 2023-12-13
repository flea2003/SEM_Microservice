package nl.tudelft.sem.template.example.domain.UserDetails;

import nl.tudelft.sem.template.example.domain.user.Email;
import nl.tudelft.sem.template.example.domain.user.EmailConverter;
import nl.tudelft.sem.template.example.domain.user.NameConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttributeConvertersTest {

    @Test
    void convertNameToDatabaseColumn() {
        NameConverter nameConverter = new NameConverter();
        Name e = new Name("Han Solo");

        assertEquals(e.toString(),nameConverter.convertToDatabaseColumn(e));
    }

    @Test
    void convertNameToEntityAttribute() {
        NameConverter nameConverter = new NameConverter();
        Name e = new Name("Han Solo");
        assertEquals(e,nameConverter.convertToEntityAttribute("Han Solo"));

    }

}
