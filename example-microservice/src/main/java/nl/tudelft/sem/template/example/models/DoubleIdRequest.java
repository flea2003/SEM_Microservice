package nl.tudelft.sem.template.example.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoubleIdRequest {

    private Integer idUserFrom;
    private Integer idUserTo;

    public DoubleIdRequest(Integer idUserFrom, Integer idUserTo) {
        this.idUserFrom = idUserFrom;
        this.idUserTo = idUserTo;
    }
}
