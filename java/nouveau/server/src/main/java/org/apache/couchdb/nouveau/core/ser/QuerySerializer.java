//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.couchdb.nouveau.core.ser;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class QuerySerializer extends StdSerializer<Query> {

    public QuerySerializer() {
        this(null);
    }

    public QuerySerializer(Class<Query> vc) {
        super(vc);
    }

    @Override
    public void serialize(Query query, JsonGenerator gen, SerializerProvider provider) throws IOException {
        writeQuery(query, gen, provider);
    }

    private void writeQuery(Query query, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (query instanceof TermQuery) {
            writeTermQuery((TermQuery) query, gen, provider);
        } else if (query instanceof BooleanQuery) {
            writeBooleanQuery((BooleanQuery) query, gen, provider);
        } else if (query instanceof PrefixQuery) {
            writePrefixQuery((PrefixQuery) query, gen, provider);
        } else if (query instanceof WildcardQuery) {
            writeWildcardQuery((WildcardQuery) query, gen, provider);
        } else if (query instanceof MatchAllDocsQuery) {
            writeMatchAllDocsQuery((MatchAllDocsQuery) query, gen, provider);
        } else {
            throw new IOException(query.getClass() + " not supported");
        }
    }

    private void writeBooleanQuery(BooleanQuery query, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField("@type", "boolean");
        gen.writeArrayFieldStart("clauses");
        for (BooleanClause clause : query.clauses()) {
            writeBooleanClause(clause, gen, provider);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private void writeBooleanClause(BooleanClause clause, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("query");
        writeQuery(clause.getQuery(), gen, provider);
        gen.writeBooleanField("prohibited", clause.isProhibited());
        gen.writeBooleanField("required", clause.isRequired());
        gen.writeBooleanField("scoring", clause.isScoring());
        gen.writeEndObject();
    }

    private void writeTermQuery(TermQuery query, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("@type", "term");
        writeTerm(query.getTerm(), gen, provider);
        gen.writeEndObject();
    }

    private void writePrefixQuery(PrefixQuery query, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("@type", "prefix");
        writeTerm(query.getPrefix(), gen, provider);
        gen.writeEndObject();
    }

    private void writeWildcardQuery(WildcardQuery query, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("@type", "wild");
        writeTerm(query.getTerm(), gen, provider);
        gen.writeEndObject();
    }

    private void writeMatchAllDocsQuery(MatchAllDocsQuery query, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("@type", "all");
        gen.writeEndObject();
    }

    private void writeTerm(Term term, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStringField("field", term.field());
        gen.writeStringField("text", term.text());
    }

}
