package nl.tudelft.sem.template.example.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentConversionRequestTest {

    private DocumentConversionRequest sut;

    @BeforeEach
    public void setup() {
        sut = new DocumentConversionRequest(7);
    }

    @Test
    public void testGetter() {
        assertEquals(7, sut.getDocumentID());
    }

    @Test
    public void testSetter() {
        sut.setDocumentID(10);
        assertEquals(10, sut.getDocumentID());
    }

    @Test
    public void testEqualsSame() {
        assertEquals(sut, sut);
    }

    @Test
    public void testEqualsDifferentClasses() {
        DocumentConversionRequest o = null;
        assertNotEquals(sut, o);
        String test = "string";
        assertNotEquals(sut, test);
    }

    @Test
    public void testEqualsEquality() {
        DocumentConversionRequest o = new DocumentConversionRequest(100);
        assertNotEquals(sut, o);
        o.setDocumentID(7);
        assertEquals(sut, o);
    }

    @Test
    public void testHashCode() {
        DocumentConversionRequest o = new DocumentConversionRequest(7);
        assertEquals(sut.hashCode(), o.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("DocumentConversionRequest{documentID=7}", sut.toString());
    }
}
