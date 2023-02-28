package org.apache.couchdb.nouveau.core.lucene4;

import org.apache.lucene.index.IndexableField;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class Lucene4Module extends SimpleModule {

    public Lucene4Module() {
        super("lucene4", Version.unknownVersion());
        addDeserializer(IndexableField.class, new IndexableFieldDeserializer());
    }

}
