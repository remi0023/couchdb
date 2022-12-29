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

package org.apache.couchdb.nouveau;

import org.apache.couchdb.nouveau.core.FileAlreadyExistsExceptionMapper;
import org.apache.couchdb.nouveau.core.FileNotFoundExceptionMapper;
import org.apache.couchdb.nouveau.core.IndexManager;
import org.apache.couchdb.nouveau.core.Lucene9AnalyzerFactory;
import org.apache.couchdb.nouveau.core.Lucene9IndexFactory;
import org.apache.couchdb.nouveau.core.Lucene9ParallelSearcherFactory;
import org.apache.couchdb.nouveau.core.UpdatesOutOfOrderExceptionMapper;
import org.apache.couchdb.nouveau.core.ser.LuceneModule;
import org.apache.couchdb.nouveau.health.AnalyzeHealthCheck;
import org.apache.couchdb.nouveau.health.IndexManagerHealthCheck;
import org.apache.couchdb.nouveau.resources.AnalyzeResource;
import org.apache.couchdb.nouveau.resources.IndexResource;
import org.apache.couchdb.nouveau.resources.SearchResource;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class NouveauApplication extends Application<NouveauApplicationConfiguration> {

    public static void main(String[] args) throws Exception {
        new NouveauApplication().run(args);
    }

    @Override
    public String getName() {
        return "Nouveau";
    }

    @Override
    public void run(NouveauApplicationConfiguration configuration, Environment environment) throws Exception {
        final MetricRegistry metricsRegistry = new MetricRegistry();
        environment.jersey().register(new InstrumentedResourceMethodApplicationListener(metricsRegistry));

        final Lucene9AnalyzerFactory analyzerFactory = new Lucene9AnalyzerFactory();

        final Lucene9ParallelSearcherFactory searcherFactory = new Lucene9ParallelSearcherFactory();
        searcherFactory.setExecutor(environment.lifecycle().executorService("nouveau-search-thread-%d").build());

        final Lucene9IndexFactory indexFactory = new Lucene9IndexFactory();
        indexFactory.setAnalyzerFactory(analyzerFactory);
        indexFactory.setSearcherFactory(searcherFactory);

        final ObjectMapper objectMapper = environment.getObjectMapper();
        objectMapper.registerModule(new LuceneModule());

        final IndexManager indexManager = new IndexManager();
        indexManager.setMetricRegistry(metricsRegistry);
        indexManager.setRootDir(configuration.getRootDir());
        indexManager.setMaxIndexesOpen(configuration.getMaxIndexesOpen());
        indexManager.setCommitIntervalSeconds(configuration.getCommitIntervalSeconds());
        indexManager.setIdleSeconds(configuration.getIdleSeconds());
        indexManager.setObjectMapper(objectMapper);
        indexManager.setAnalyzerFactory(analyzerFactory);
        indexManager.setIndexFactory(indexFactory);
        environment.lifecycle().manage(indexManager);

        environment.jersey().register(new FileNotFoundExceptionMapper());
        environment.jersey().register(new FileAlreadyExistsExceptionMapper());
        environment.jersey().register(new UpdatesOutOfOrderExceptionMapper());

        final AnalyzeResource analyzeResource = new AnalyzeResource(analyzerFactory);
        environment.jersey().register(analyzeResource);
        environment.jersey().register(new IndexResource(indexManager));
        environment.jersey().register(new SearchResource(indexManager));

        // health checks
        environment.healthChecks().register("analyzeResource", new AnalyzeHealthCheck(analyzeResource));
        environment.healthChecks().register("indexManager", new IndexManagerHealthCheck(indexManager));
    }

}
