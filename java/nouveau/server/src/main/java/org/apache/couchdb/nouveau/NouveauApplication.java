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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.couchdb.nouveau.core.IndexManager;
import org.apache.couchdb.nouveau.core.Lucene;
import org.apache.couchdb.nouveau.core.LuceneBundle;
import org.apache.couchdb.nouveau.core.UpdatesOutOfOrderExceptionMapper;
import org.apache.couchdb.nouveau.health.AnalyzeHealthCheck;
import org.apache.couchdb.nouveau.health.IndexManagerHealthCheck;
import org.apache.couchdb.nouveau.resources.AnalyzeResource;
import org.apache.couchdb.nouveau.resources.IndexResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class NouveauApplication extends Application<NouveauApplicationConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NouveauApplication.class);

    private Collection<LuceneBundle> bundles = new HashSet<LuceneBundle>();

    public static void main(String[] args) throws Exception {
        new NouveauApplication().run(args);
    }

    @Override
    public String getName() {
        return "Nouveau";
    }

    @Override
    public void initialize(Bootstrap<NouveauApplicationConfiguration> bootstrap) {
        // Find Lucene bundles
        for (String name : System.getProperties().stringPropertyNames()) {
            if (name.startsWith("nouveau.bundle.")) {
                try {
                    ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new URL(System.getProperty(name))});
                    final ServiceLoader<ConfiguredBundle> bundleLoader = ServiceLoader.load(ConfiguredBundle.class, classLoader);
                for (final ConfiguredBundle<Configuration> bundle : bundleLoader) {
                    if (bundle instanceof LuceneBundle) {
                        bootstrap.addBundle(bundle);
                        bundles.add((LuceneBundle<?>)bundle);
                    }
                }
                } catch (final MalformedURLException e) {
                    throw new Error(e);
                }
            }
        }
    }

    @Override
    public void run(NouveauApplicationConfiguration configuration, Environment environment) throws Exception {
        final MetricRegistry metricsRegistry = new MetricRegistry();
        environment.jersey().register(new InstrumentedResourceMethodApplicationListener(metricsRegistry));

        final ObjectMapper objectMapper = environment.getObjectMapper();

        final Map<Integer, Lucene> lucenes = new HashMap<Integer, Lucene>();
        for (final LuceneBundle bundle : bundles) {
            final Lucene lucene = ((LuceneBundle)bundle).getLucene();
            lucenes.put(lucene.getMajor(), lucene);
            LOGGER.info("Loaded bundle for Lucene {}", lucene.getMajor());
        }

        if (lucenes.isEmpty()) {
            throw new IllegalStateException("No Lucene bundles configured");
        }

        final ScheduledExecutorService indexManagerScheduler =
            environment.lifecycle()
            .scheduledExecutorService("index-manager-scheduler-%d")
            .threads(10)
            .build();

        final IndexManager indexManager = new IndexManager();
        indexManager.setScheduler(indexManagerScheduler);
        indexManager.setRootDir(configuration.getRootDir());
        indexManager.setMaxIndexesOpen(configuration.getMaxIndexesOpen());
        indexManager.setCommitIntervalSeconds(configuration.getCommitIntervalSeconds());
        indexManager.setIdleSeconds(configuration.getIdleSeconds());
        indexManager.setObjectMapper(objectMapper);
        indexManager.setLucenes(lucenes);
        environment.lifecycle().manage(indexManager);

        environment.jersey().register(new UpdatesOutOfOrderExceptionMapper());

        final AnalyzeResource analyzeResource = new AnalyzeResource(lucenes);
        environment.jersey().register(analyzeResource);
        environment.jersey().register(new IndexResource(indexManager));

        // health checks
        environment.healthChecks().register("analyzeResource", new AnalyzeHealthCheck(analyzeResource));
        environment.healthChecks().register("indexManager", new IndexManagerHealthCheck(indexManager));
    }

}
