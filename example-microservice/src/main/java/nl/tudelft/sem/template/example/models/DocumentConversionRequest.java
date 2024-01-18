package nl.tudelft.sem.template.example.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class DocumentConversionRequest {
    private Integer documentId;

    /**
     * Constructor to initialize a DocumentConversionRequest.
     *
     * @param documentId the ID of the document
     */
    public DocumentConversionRequest(Integer documentId) {
        this.documentId = documentId;
    }

    @SuppressWarnings("PMD.NullAssignment")
    public DocumentConversionRequest() {
        this.documentId = null;
    }

    @JsonCreator
    public static DocumentConversionRequest create(@JsonProperty("documentID") Integer documentId) {
        return new DocumentConversionRequest(documentId);
    }

    /**
     * Getter for the documentID.
     *
     * @return current documentID
     */
    @JsonProperty("documentID")
    public Integer getDocumentId() {
        return this.documentId;
    }

    /**
     * Setter for the documentID.
     *
     * @param newId new documentID
     */
    public void setDocumentId(Integer newId) {
        this.documentId = newId;
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

        return Objects.equals(documentId, ((DocumentConversionRequest) o).documentId);
    }

    /**
     * hashCode method for the DocumentConversionRequest class.
     *
     * @return hash of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(documentId);
    }

    /**
     * toString method for the DocumentConversionRequest class.
     *
     * @return String representation of object
     */
    @Override
    public String toString() {
        return "DocumentConversionRequest{" + "documentID=" + documentId + '}';
    }
}
