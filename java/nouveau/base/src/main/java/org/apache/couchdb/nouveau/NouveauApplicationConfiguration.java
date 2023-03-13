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

import java.nio.file.Path;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class NouveauApplicationConfiguration extends Configuration {

    @Min(10)
    private int maxIndexesOpen = 10;

    @Min(10)
    private int commitIntervalSeconds = 10;

    @Min(30)
    private int idleSeconds = 30;

    @Min(-1)
    private int lockCount = -1;

    @NotNull
    private Path rootDir = null;

    @JsonProperty
    public void setMaxIndexesOpen(int maxIndexesOpen) {
        this.maxIndexesOpen = maxIndexesOpen;
    }

    public int getMaxIndexesOpen() {
        return maxIndexesOpen;
    }

    @JsonProperty
    public void setCommitIntervalSeconds(int commitIntervalSeconds) {
        this.commitIntervalSeconds = commitIntervalSeconds;
    }

    public int getCommitIntervalSeconds() {
        return commitIntervalSeconds;
    }

    @JsonProperty
    public void setIdleSeconds(int idleSeconds) {
        this.idleSeconds = idleSeconds;
    }

    public int getIdleSeconds() {
        return idleSeconds;
    }

    public int getLockCount() {
        return lockCount;
    }

    public void setLockCount(int lockCount) {
        this.lockCount = lockCount;
    }

    @JsonProperty
    public void setRootDir(Path rootDir) {
        this.rootDir = rootDir;
    }

    public Path getRootDir() {
        return rootDir;
    }

}
