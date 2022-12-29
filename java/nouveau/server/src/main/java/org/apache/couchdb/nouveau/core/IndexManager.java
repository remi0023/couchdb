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

package org.apache.couchdb.nouveau.core;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.couchdb.nouveau.api.IndexDefinition;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.caffeine.MetricsStatsCounter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.github.benmanes.caffeine.cache.Scheduler;

import io.dropwizard.lifecycle.Managed;

public class IndexManager implements Managed {

    private static final int RETRY_LIMIT = 500;
    private static final int RETRY_SLEEP_MS = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexManager.class);

    private class IndexLoader implements CacheLoader<String, Index> {

        @Override
        public @Nullable Index load(@NonNull String name) throws Exception {
            return openExistingIndex(name);
        }

        @Override
        public @Nullable Index reload(@NonNull String name, @NonNull Index index) throws Exception {
            try {
                if (index.commit()) {
                    LOGGER.info("{} committed.", index);
                }
            } catch (final IOException e) {
                LOGGER.error(index + " threw exception when committing.", e);
                index.close();
                return openExistingIndex(name);
            }
            return index;
        }

    }

    private static class IndexCloser implements RemovalListener<String, Index> {

        public void onRemoval(String name, Index index, RemovalCause cause) {
            try {
                index.close();
            } catch (IOException e) {
                LOGGER.error(index + " threw exception when closing", e);
            }
        }
    }

    private static final IndexCloser INDEX_CLOSER = new IndexCloser();


    @Min(1)
    private int maxIndexesOpen;

    @Min(1)
    private int commitIntervalSeconds;

    @Min(1)
    private int idleSeconds;

    @NotEmpty
    private Path rootDir;

    @NotNull
    private Lucene9AnalyzerFactory analyzerFactory;

    @NotNull
    private ObjectMapper objectMapper;

    private IndexFactory indexFactory;

    private MetricRegistry metricRegistry;

    private LoadingCache<String, Index> cache;

    public Index acquire(final String name) throws IOException {
        for (int i = 0; i < RETRY_LIMIT; i++) {
            final Index result = getFromCache(name);

            // Check if we're in the middle of closing.
            result.lock();
            if (!result.isClosed()) {
                return result;
            }
            result.unlock();

            // Retry after a short sleep.
            try {
                Thread.sleep(RETRY_SLEEP_MS);
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }
        }
        throw new IOException("Failed to acquire " + name);
    }

    public void release(final Index index) throws IOException {
        index.unlock();
    }

    public void create(final String name, IndexDefinition indexDefinition) throws IOException {
        createNewIndex(name, indexDefinition);
    }

    public void deleteAll(final String path) throws IOException {
        final Path rootPath = indexRootPath(path);
        if (!rootPath.toFile().exists()) {
            return;
        }
        Stream<Path> stream = Files.find(rootPath, 100,
            (p, attr) -> attr.isDirectory() && isIndex(p));
        try {
            stream.forEach((p) -> {
                try {
                    deleteIndex(rootDir.relativize(p).toString());
                } catch (Exception e) {
                    LOGGER.error("I/O exception deleting " + p, e);
                }
            });
        } finally {
            stream.close();
        }
    }

    private void deleteIndex(final String name) throws IOException {
        final Index index = acquire(name);
        try {
            index.setDeleteOnClose(true);
            cache.invalidate(name);
        } finally {
            release(index);
        }
    }

    @JsonProperty
    public int getMaxIndexesOpen() {
        return maxIndexesOpen;
    }

    public void setMaxIndexesOpen(int maxIndexesOpen) {
        this.maxIndexesOpen = maxIndexesOpen;
    }

    @JsonProperty
    public int getCommitIntervalSeconds() {
        return commitIntervalSeconds;
    }

    public void setCommitIntervalSeconds(int commitIntervalSeconds) {
        this.commitIntervalSeconds = commitIntervalSeconds;
    }

    @JsonProperty
    public int getIdleSeconds() {
        return idleSeconds;
    }

    public void setIdleSeconds(int idleSeconds) {
        this.idleSeconds = idleSeconds;
    }

    @JsonProperty
    public Path getRootDir() {
        return rootDir;
    }

    public void setRootDir(Path rootDir) {
        this.rootDir = rootDir;
    }

    public void setAnalyzerFactory(final Lucene9AnalyzerFactory analyzerFactory) {
        this.analyzerFactory = analyzerFactory;
    }

    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setIndexFactory(final IndexFactory indexFactory) {
        this.indexFactory = indexFactory;
    }

    public void setMetricRegistry(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void start() throws IOException {
        cache = Caffeine.newBuilder()
            .recordStats(() -> new MetricsStatsCounter(metricRegistry, "IndexManager"))
            .initialCapacity(maxIndexesOpen)
            .maximumSize(maxIndexesOpen)
            .expireAfterAccess(Duration.ofSeconds(idleSeconds))
            .expireAfterWrite(Duration.ofSeconds(idleSeconds))
            .refreshAfterWrite(Duration.ofSeconds(commitIntervalSeconds))
            .scheduler(Scheduler.systemScheduler())
            .removalListener(INDEX_CLOSER)
            .evictionListener(INDEX_CLOSER)
            .build(new IndexLoader());
    }

    @Override
    public void stop() {
        cache.invalidateAll();
    }

    private Index getFromCache(final String name) throws IOException {
        try {
            return cache.get(name);
        } catch (CompletionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }

    private void createNewIndex(final String name, final IndexDefinition indexDefinition) throws IOException {
        // Validate index definiton
        analyzerFactory.fromDefinition(indexDefinition);

        // Persist definition
        final Path path = indexDefinitionPath(name);
        if (Files.exists(path)) {
            throw new FileAlreadyExistsException(name + " already exists");
        }
        Files.createDirectories(path.getParent());
        objectMapper.writeValue(path.toFile(), indexDefinition);
    }

    private Index openExistingIndex(final String name) throws IOException {
        final Path path = indexPath(name);
        final IndexDefinition indexDefinition = objectMapper.readValue(indexDefinitionPath(name).toFile(), IndexDefinition.class);
        return indexFactory.open(path, indexDefinition);
    }

    private boolean isIndex(final Path path) {
        return path.resolve("index_definition.json").toFile().exists();
    }

    private Path indexDefinitionPath(final String name) {
        return indexRootPath(name).resolve("index_definition.json");
    }

    private Path indexPath(final String name) {
        return indexRootPath(name).resolve("index");
    }

    private Path indexRootPath(final String name) {
        final Path result = rootDir.resolve(name).normalize();
        if (result.startsWith(rootDir)) {
            return result;
        }
        throw new WebApplicationException(name + " attempts to escape from index root directory",
                Status.BAD_REQUEST);
    }

}
