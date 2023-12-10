package nl.tudelft.sem.template.example.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class DocumentConversionRequest {
    private Integer documentID;

    /**
     * Constructor to initialize a DocumentConversionRequest.
     *
     * @param documentID the ID of the document
     */
    public DocumentConversionRequest(Integer documentID) {
        this.documentID = documentID;
    }

    public DocumentConversionRequest() {
        this.documentID = null;
    }

    @JsonCreator
    public static DocumentConversionRequest create(@JsonProperty("documentID") Integer documentID) {
        return new DocumentConversionRequest(documentID);
    }

    /**
     * Getter for the documentID.
     *
     * @return current documentID
     */
    @JsonProperty("documentID")
    public Integer getDocumentID() {
        return this.documentID;
    }

    /**
     * Setter for the documentID.
     *
     * @param newId new documentID
     */
    public void setDocumentID(Integer newId) {
        this.documentID = newId;
    }

    /**
     * Equals method for the DocumentConversionRequest class.
     *
     * @param o Object to be compared with
     * @return true iff objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DocumentConversionRequest that = (DocumentConversionRequest) o;
        return Objects.equals(documentID, that.documentID);
    }

    /**
     * hashCode method for the DocumentConversionRequest class.
     *
     * @return hash of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(documentID);
    }

    /**
     * toString method for the DocumentConversionRequest class.
     *
     * @return String representation of object
     */
    @Override
    public String toString() {
        return "DocumentConversionRequest{" + "documentID=" + documentID + '}';
    }
}
