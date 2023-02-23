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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class IOUtils {

    // From https://www.baeldung.com/java-delete-directory
    public static void rm(final Path path) throws IOException {
        File[] allContents = path.toFile().listFiles();
        if (allContents != null) {
            for (final File file : allContents) {
                rm(file.toPath());
            }
        }
        if (!path.toFile().delete()) {
            throw new IOException("failed to delete " + path);
        }
    }
}
