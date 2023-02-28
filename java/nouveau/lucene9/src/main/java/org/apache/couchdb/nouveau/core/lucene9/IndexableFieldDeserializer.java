package org.apache.couchdb.nouveau.core.lucene9;

import java.io.IOException;

import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexableField;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class IndexableFieldDeserializer extends StdDeserializer<IndexableField> {

    public IndexableFieldDeserializer() {
        this(null);
    }

    public IndexableFieldDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public IndexableField deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JacksonException {
        return new TextField("foo9", "bar9", Store.NO);
    }

}
