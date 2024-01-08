package nl.tudelft.sem.template.example.domain.user;

import nl.tudelft.sem.template.example.domain.UserDetails.Name;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA Converter for the Name value object.
 */
@Converter
public class NameConverter implements AttributeConverter<Name, String> {

    @Override
    public String convertToDatabaseColumn(Name attribute) {
        return attribute.toString();
    }

    @Override
    public Name convertToEntityAttribute(String dbData) {
        return new Name(dbData);
    }

}
