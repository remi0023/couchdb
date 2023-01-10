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

package org.apache.couchdb.nouveau.core.lucene9;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.couchdb.nouveau.api.DocumentDeleteRequest;
import org.apache.couchdb.nouveau.api.DocumentUpdateRequest;
import org.apache.couchdb.nouveau.api.SearchHit;
import org.apache.couchdb.nouveau.api.SearchRequest;
import org.apache.couchdb.nouveau.api.SearchResults;
import org.apache.couchdb.nouveau.api.document.DocField;
import org.apache.couchdb.nouveau.api.document.DoublePointDocField;
import org.apache.couchdb.nouveau.api.document.StoredDocField;
import org.apache.couchdb.nouveau.api.document.StringDocField;
import org.apache.couchdb.nouveau.api.document.TextDocField;
import org.apache.couchdb.nouveau.api.facet.range.DoubleRange;
import org.apache.couchdb.nouveau.core.Index;
import org.apache.couchdb.nouveau.core.QueryParser;
import org.apache.couchdb.nouveau.core.QueryParserException;
import org.apache.couchdb.nouveau.l9x.lucene.analysis.Analyzer;
import org.apache.couchdb.nouveau.l9x.lucene.document.Document;
import org.apache.couchdb.nouveau.l9x.lucene.document.DoublePoint;
import org.apache.couchdb.nouveau.l9x.lucene.document.SortedDocValuesField;
import org.apache.couchdb.nouveau.l9x.lucene.document.StoredField;
import org.apache.couchdb.nouveau.l9x.lucene.document.StringField;
import org.apache.couchdb.nouveau.l9x.lucene.document.TextField;
import org.apache.couchdb.nouveau.l9x.lucene.document.Field.Store;
import org.apache.couchdb.nouveau.l9x.lucene.facet.FacetResult;
import org.apache.couchdb.nouveau.l9x.lucene.facet.Facets;
import org.apache.couchdb.nouveau.l9x.lucene.facet.FacetsCollector;
import org.apache.couchdb.nouveau.l9x.lucene.facet.FacetsCollectorManager;
import org.apache.couchdb.nouveau.l9x.lucene.facet.LabelAndValue;
import org.apache.couchdb.nouveau.l9x.lucene.facet.StringDocValuesReaderState;
import org.apache.couchdb.nouveau.l9x.lucene.facet.StringValueFacetCounts;
import org.apache.couchdb.nouveau.l9x.lucene.facet.range.DoubleRangeFacetCounts;
import org.apache.couchdb.nouveau.l9x.lucene.index.IndexWriter;
import org.apache.couchdb.nouveau.l9x.lucene.index.IndexWriterConfig;
import org.apache.couchdb.nouveau.l9x.lucene.index.IndexableField;
import org.apache.couchdb.nouveau.l9x.lucene.index.Term;
import org.apache.couchdb.nouveau.l9x.lucene.misc.store.DirectIODirectory;
import org.apache.couchdb.nouveau.l9x.lucene.search.CollectorManager;
import org.apache.couchdb.nouveau.l9x.lucene.search.FieldDoc;
import org.apache.couchdb.nouveau.l9x.lucene.search.IndexSearcher;
import org.apache.couchdb.nouveau.l9x.lucene.search.MultiCollectorManager;
import org.apache.couchdb.nouveau.l9x.lucene.search.Query;
import org.apache.couchdb.nouveau.l9x.lucene.search.ScoreDoc;
import org.apache.couchdb.nouveau.l9x.lucene.search.SearcherFactory;
import org.apache.couchdb.nouveau.l9x.lucene.search.SearcherManager;
import org.apache.couchdb.nouveau.l9x.lucene.search.Sort;
import org.apache.couchdb.nouveau.l9x.lucene.search.SortField;
import org.apache.couchdb.nouveau.l9x.lucene.search.TermQuery;
import org.apache.couchdb.nouveau.l9x.lucene.search.TopDocs;
import org.apache.couchdb.nouveau.l9x.lucene.search.TopFieldCollector;
import org.apache.couchdb.nouveau.l9x.lucene.store.Directory;
import org.apache.couchdb.nouveau.l9x.lucene.store.FSDirectory;
import org.apache.couchdb.nouveau.l9x.lucene.util.BytesRef;

class Lucene9Index extends Index {

    private static final Sort DEFAULT_SORT = new Sort(SortField.FIELD_SCORE,
            new SortField("_id", SortField.Type.STRING));
    private static final Pattern SORT_FIELD_RE = Pattern.compile("^([-+])?([\\.\\w]+)(?:<(\\w+)>)?$");

    private final Analyzer analyzer;
    private final IndexWriter writer;
    private final SearcherManager searcherManager;

    static Index open(final Path path, final Analyzer analyzer, final SearcherFactory searcherFactory)
            throws IOException {
        final Directory dir = new DirectIODirectory(FSDirectory.open(path));
        final IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setUseCompoundFile(false);
        final IndexWriter writer = new IndexWriter(dir, config);
        final long updateSeq = getUpdateSeq(writer);
        final SearcherManager searcherManager = new SearcherManager(writer, searcherFactory);
        return new Lucene9Index(analyzer, writer, updateSeq, searcherManager);
    }

    private Lucene9Index(final Analyzer analyzer, final IndexWriter writer, final long updateSeq,
            final SearcherManager searcherManager) {
        super(updateSeq);
        this.analyzer = analyzer;
        this.writer = writer;
        this.searcherManager = searcherManager;
    }

    @Override
    public int doNumDocs() throws IOException {
        return writer.getDocStats().numDocs;
    }

    @Override
    public void doUpdate(final String docId, final DocumentUpdateRequest request) throws IOException {
        final Term docIdTerm = docIdTerm(docId);
        final Document doc = toDocument(docId, request);
        writer.updateDocument(docIdTerm, doc);
    }

    @Override
    public void doDelete(final String docId, final DocumentDeleteRequest request) throws IOException {
        final Query query = docIdQuery(docId);
        writer.deleteDocuments(query);
    }

    @Override
    public boolean doCommit(final long updateSeq) throws IOException {
        writer.setLiveCommitData(Collections.singletonMap("update_seq", Long.toString(updateSeq)).entrySet());
        return writer.commit() != -1;
    }

    @Override
    public void doClose() throws IOException {
        writer.close();
    }

    @Override
    public boolean isOpen() {
        return writer.isOpen();
    }

    @Override
    public SearchResults doSearch(final SearchRequest request) throws IOException, QueryParserException {
        final Query query = newQueryParser().parse(request);

        // Construct CollectorManagers.
        final MultiCollectorManager cm;
        final CollectorManager<?, ? extends TopDocs> hits = hitCollector(request);

        searcherManager.maybeRefreshBlocking();

        final IndexSearcher searcher = searcherManager.acquire();
        try {
            if (request.hasCounts() || request.hasRanges()) {
                cm = new MultiCollectorManager(hits, new FacetsCollectorManager());
            } else {
                cm = new MultiCollectorManager(hits);
            }
            final Object[] reduces = searcher.search(query, cm);
            return toSearchResults(request, searcher, reduces);
        } catch (IllegalStateException e) {
            throw new WebApplicationException(e.getMessage(), e, Status.BAD_REQUEST);
        } finally {
            searcherManager.release(searcher);
        }
    }

    private CollectorManager<?, ? extends TopDocs> hitCollector(final SearchRequest searchRequest) {
        final Sort sort = toSort(searchRequest);

        final FieldDoc after = searchRequest.getAfter();
        if (after != null) {
            if (getLastSortField(sort).getReverse()) {
                after.doc = 0;
            } else {
                after.doc = Integer.MAX_VALUE;
            }
        }

        return TopFieldCollector.createSharedManager(
                sort,
                searchRequest.getLimit(),
                after,
                1000);
    }

    private SortField getLastSortField(final Sort sort) {
        final SortField[] sortFields = sort.getSort();
        return sortFields[sortFields.length - 1];
    }

    private SearchResults toSearchResults(final SearchRequest searchRequest, final IndexSearcher searcher,
            final Object[] reduces) throws IOException {
        final SearchResults result = new SearchResults();
        collectHits(searcher, (TopDocs) reduces[0], result);
        if (reduces.length == 2) {
            collectFacets(searchRequest, searcher, (FacetsCollector) reduces[1], result);
        }
        return result;
    }

    private void collectHits(final IndexSearcher searcher, final TopDocs topDocs, final SearchResults searchResults)
            throws IOException {
        final List<SearchHit> hits = new ArrayList<SearchHit>(topDocs.scoreDocs.length);

        for (final ScoreDoc scoreDoc : topDocs.scoreDocs) {
            final Document doc = searcher.doc(scoreDoc.doc);

            final List<DocField> fields = new ArrayList<DocField>(doc.getFields().size());
            for (IndexableField field : doc.getFields()) {
                if (!field.name().startsWith("_")) {
                    fields.add(luceneFieldToDocField(field));
                }
            }

            hits.add(new SearchHit(doc.get("_id"), (FieldDoc) scoreDoc, fields));
        }

        searchResults.setTotalHits(topDocs.totalHits.value);
        searchResults.setTotalHitsRelation(topDocs.totalHits.relation.name());
        searchResults.setHits(hits);
    }

    private void collectFacets(final SearchRequest searchRequest, final IndexSearcher searcher,
            final FacetsCollector fc, final SearchResults searchResults) throws IOException {
        if (searchRequest.hasCounts()) {
            final Map<String, Map<String, Number>> countsMap = new HashMap<String, Map<String, Number>>(
                    searchRequest.getCounts().size());
            for (final String field : searchRequest.getCounts()) {
                final StringDocValuesReaderState state = new StringDocValuesReaderState(searcher.getIndexReader(),
                        field);
                final StringValueFacetCounts counts = new StringValueFacetCounts(state, fc);
                countsMap.put(field, collectFacets(counts, searchRequest.getTopN(), field));
            }
            searchResults.setCounts(countsMap);
        }

        if (searchRequest.hasRanges()) {
            final Map<String, Map<String, Number>> rangesMap = new HashMap<String, Map<String, Number>>(
                    searchRequest.getRanges().size());
            for (final Entry<String, List<DoubleRange>> entry : searchRequest.getRanges().entrySet()) {
                final DoubleRangeFacetCounts counts = new DoubleRangeFacetCounts(entry.getKey(), fc, convertDoubleRanges(entry.getValue()));
                rangesMap.put(entry.getKey(), collectFacets(counts, searchRequest.getTopN(), entry.getKey()));
            }
            searchResults.setRanges(rangesMap);
        }
    }

    private org.apache.couchdb.nouveau.l9x.lucene.facet.range.DoubleRange[] convertDoubleRanges(final List<DoubleRange> ranges) {
        final org.apache.couchdb.nouveau.l9x.lucene.facet.range.DoubleRange[] result = new org.apache.couchdb.nouveau.l9x.lucene.facet.range.DoubleRange[ranges.size()];
        for (int i = 0; i < ranges.size(); i++) {
            result[i] = convertDoubleRange(ranges.get(i));
        }
        return result;
    }

    private org.apache.couchdb.nouveau.l9x.lucene.facet.range.DoubleRange convertDoubleRange(final DoubleRange range) {
        return new org.apache.couchdb.nouveau.l9x.lucene.facet.range.DoubleRange(
            range.getLabel(),
            range.getMin(),
            range.getMinInclusive(),
            range.getMax(),
            range.getMaxInclusive()
        );
    }

    private Map<String, Number> collectFacets(final Facets facets, final int topN, final String dim)
            throws IOException {
        final FacetResult topChildren = facets.getTopChildren(topN, dim);
        final Map<String, Number> result = new HashMap<String, Number>(topChildren.childCount);
        for (final LabelAndValue lv : topChildren.labelValues) {
            result.put(lv.label, lv.value);
        }
        return result;
    }

    // Ensure _id is final sort field so we can paginate.
    private Sort toSort(final SearchRequest searchRequest) {
        if (!searchRequest.hasSort()) {
            return DEFAULT_SORT;
        }

        final List<String> sort = new ArrayList<String>(searchRequest.getSort());
        final String last = sort.get(sort.size() - 1);
        // Append _id field if not already present.
        switch (last) {
            case "-_id<string>":
            case "_id<string>":
                break;
            default:
                sort.add("_id<string>");
        }
        return convertSort(sort);
    }

    private Sort convertSort(final List<String> sort) {
        final SortField[] fields = new SortField[sort.size()];
        for (int i = 0; i < sort.size(); i++) {
            fields[i] = convertSortField(sort.get(i));
        }
        return new Sort(fields);
    }

    private SortField convertSortField(final String sortString) {
        final Matcher m = SORT_FIELD_RE.matcher(sortString);
        if (!m.matches()) {
            throw new WebApplicationException(
                    sortString + " is not a valid sort parameter", Status.BAD_REQUEST);
        }
        final boolean reverse = "-".equals(m.group(1));
        SortField.Type type = SortField.Type.DOUBLE;
        if ("string".equals(m.group(3))) {
            type = SortField.Type.STRING;
        }
        return new SortField(m.group(2), type, reverse);
    }

    private static Document toDocument(final String docId, final DocumentUpdateRequest request) throws IOException {
        final Document result = new Document();

        // id
        result.add(new StringField("_id", docId, Store.YES));
        result.add(new SortedDocValuesField("_id", new BytesRef(docId)));

        // partition (optional)
        if (request.hasPartition()) {
            result.add(new StringField("_partition", request.getPartition(), Store.NO));
        }

        for (DocField field : request.getFields()) {
            // Underscore-prefix is reserved.
            if (field.getName().startsWith("_")) {
                continue;
            }
            result.add(docFieldToLuceneField(field));
        }

        return result;
    }

    private static IndexableField docFieldToLuceneField(final DocField docField) {
        if (docField instanceof StringDocField) {
            final StringDocField field = (StringDocField) docField;
            return new StringField(field.getName(), field.getValue(), field.isStored() ? Store.YES : Store.NO);
        }
        if (docField instanceof TextDocField) {
            final TextDocField field = (TextDocField) docField;
            return new StringField(field.getName(), field.getValue(), field.isStored() ? Store.YES : Store.NO);
        }
        if (docField instanceof DoublePointDocField) {
            final DoublePointDocField field = (DoublePointDocField) docField;
            return new DoublePoint(field.getName(), field.getValue());
        }
        if (docField instanceof StoredDocField) {
            final StoredDocField field = (StoredDocField) docField;
            if (field.getStringValue() != null) {
                return new StoredField(field.getName(), field.getStringValue());
            }
            if (field.getBinaryValue() != null) {
                return new StoredField(field.getName(), field.getBinaryValue());
            }
            if (field.getNumericValue() != null) {
                final Number value = (Number) field.getNumericValue();
                if (value instanceof Long) {
                    return new StoredField(field.getName(), (long) value);
                }
                if (value instanceof Integer) {
                    return new StoredField(field.getName(), (int) value);
                }
                if (value instanceof Double) {
                    return new StoredField(field.getName(), (double) value);
                }
                if (value instanceof Float) {
                    return new StoredField(field.getName(), (float) value);
                }
            }
        }
        throw new IllegalArgumentException(docField.getClass() + " not valid");
    }

    private static DocField luceneFieldToDocField(final IndexableField indexableField) {
        if (indexableField instanceof StringField) {
            final StringField field = (StringField) indexableField;
            return new StringDocField(field.name(), field.stringValue(), field.fieldType().stored());
        }
        if (indexableField instanceof TextField) {
            final TextField field = (TextField) indexableField;
            return new TextDocField(field.name(), field.stringValue(), field.fieldType().stored());
        }
        if (indexableField instanceof DoublePoint) {
            final DoublePoint field = (DoublePoint) indexableField;
            return new DoublePointDocField(field.name(), (Double) field.numericValue());
        }
        if (indexableField instanceof StoredField) {
            final StoredField field = (StoredField) indexableField;
            if (field.stringValue() != null) {
                return new StoredDocField(field.name(), field.stringValue());
            }
            if (field.binaryValue() != null) {
                final BytesRef bytesRef = field.binaryValue();
                final byte[] bytes = Arrays.copyOfRange(bytesRef.bytes, bytesRef.offset, bytesRef.offset + bytesRef.length);
                return new StoredDocField(field.name(), bytes);
            }
            if (field.numericValue() != null) {
                return new StoredDocField(field.name(), field.numericValue());
            }
        }
        throw new IllegalArgumentException(indexableField.getClass() + " not valid");
    }

    private static Query docIdQuery(final String docId) {
        return new TermQuery(docIdTerm(docId));
    }

    private static Term docIdTerm(final String docId) {
        return new Term("_id", docId);
    }

    private static long getUpdateSeq(final IndexWriter writer) throws IOException {
        final Iterable<Map.Entry<String, String>> commitData = writer.getLiveCommitData();
        if (commitData == null) {
            return 0L;
        }
        for (Map.Entry<String, String> entry : commitData) {
            if (entry.getKey().equals("update_seq")) {
                return Long.parseLong(entry.getValue());
            }
        }
        return 0L;
    }

    public QueryParser newQueryParser() {
        return new Lucene9QueryParser("default", analyzer);
    }

}
