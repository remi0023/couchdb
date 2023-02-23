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

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.couchdb.nouveau.api.IndexDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
public class IndexManagerTest {

    private static final int LUCENE_MAJOR = 9;

    @TempDir
    static Path tempDir;

    private IndexManager manager;

    private ScheduledExecutorService scheduler;

    @BeforeEach
    public void setup() throws Exception {
        scheduler = Executors.newScheduledThreadPool(1);
        manager = new IndexManager();
        manager.setScheduler(scheduler);
        manager.setLucenes(Map.of(LUCENE_MAJOR, mock(Lucene.class)));
        manager.setCommitIntervalSeconds(5);
        manager.setObjectMapper(new ObjectMapper());
        manager.setRootDir(tempDir);
        manager.start();
    }

    @AfterEach
    public void cleanup() throws Exception {
        manager.stop();
        scheduler.shutdown();
    }

    @Test
    public void testCreate() throws Exception {
        final IndexDefinition def = new IndexDefinition(LUCENE_MAJOR, "standard", null);
        manager.create("foo", def);
    }

}
