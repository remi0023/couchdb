package org.apache.couchdb.nouveau.core.lucene9;

import org.apache.lucene.index.IndexableField;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Dummy9 {

    private IndexableField field;

    @JsonProperty
    public IndexableField getField() {
        return field;
    }

    public void setField(IndexableField field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "Dummy9 [field=" + field + "]";
    }

}
