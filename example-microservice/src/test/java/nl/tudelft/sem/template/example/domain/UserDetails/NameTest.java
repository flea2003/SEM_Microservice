package nl.tudelft.sem.template.example.domain.UserDetails;

import nl.tudelft.sem.template.example.domain.user.Username;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NameTest {

    @Test
    void testBadNames(){
        Name u1 = new Name("Jabba The Hut");
        assertEquals("",u1.toString());
        Name u2 = new Name("Han   Solo");
        assertEquals("",u2.toString());
        Name u3 = new Name("Steve");
        assertEquals("",u3.toString());
        Name u4 = new Name("R2 D2");
        assertEquals("",u4.toString());
    }

    @Test
    void testGoodNames(){
        Name u1 = new Name("Luke Skywalker");
        assertEquals("Luke Skywalker",u1.toString());
        Name u2 = new Name("A B");
        assertEquals("A B",u2.toString());
    }

}
