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

package org.apache.couchdb.nouveau.api;

import org.apache.couchdb.nouveau.core.ser.AfterDeserializer;
import org.apache.couchdb.nouveau.core.ser.AfterSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
@JsonSerialize(using = AfterSerializer.class)
@JsonDeserialize(using = AfterDeserializer.class)
public class After {

    private Object[] fields;

    public After() {
    }

    public After(final Object... fields) {
        this.fields = fields;
    }

    public Object[] getFields() {
        return fields;
    }

    public void setFields(Object[] fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append("After [fields=");
        for (final Object o : fields) {
            result.append(o);
            result.append('(');
            result.append(o.getClass().getSimpleName());
            result.append(") ");
        }
        return result.toString();
    }

}
