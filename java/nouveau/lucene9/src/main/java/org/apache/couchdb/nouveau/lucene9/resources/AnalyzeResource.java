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

package org.apache.couchdb.nouveau.lucene9.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.MediaType;

import org.apache.couchdb.nouveau.api.AnalyzeRequest;
import org.apache.couchdb.nouveau.api.AnalyzeResponse;
import org.apache.couchdb.nouveau.lucene9.core.Lucene9AnalyzerFactory;
import org.apache.couchdb.nouveau.resources.BaseAnalyzeResource;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.ResponseMetered;

@Path("/9/analyze")
@Metered
@ResponseMetered
@ExceptionMetered(cause = IOException.class)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AnalyzeResource extends BaseAnalyzeResource {

    @Override
    @POST
    public AnalyzeResponse analyzeText(@NotNull @Valid AnalyzeRequest request) throws IOException {
        try {
            final List<String> tokens = tokenize(Lucene9AnalyzerFactory.newAnalyzer(request.getAnalyzer()),
                    request.getText());
            return new AnalyzeResponse(tokens);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(request.getAnalyzer() + " not a valid analyzer",
                    Status.BAD_REQUEST);
        }
    }

    private List<String> tokenize(final Analyzer analyzer, final String text) throws IOException {
        final List<String> result = new ArrayList<String>(10);
        try (final TokenStream tokenStream = analyzer.tokenStream("default", text)) {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                final CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
                result.add(term.toString());
            }
            tokenStream.end();
        }
        return result;
    }

}
