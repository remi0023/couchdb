package org.apache.couchdb.nouveau.core.lucene9;

import org.apache.lucene.index.IndexableField;

import com.fasterxml.jackson.core.Version;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class Lucene9Module extends SimpleModule {

    public Lucene9Module() {
        super("lucene9", Version.unknownVersion());
        addDeserializer(IndexableField.class, new IndexableFieldDeserializer());
    }

}
