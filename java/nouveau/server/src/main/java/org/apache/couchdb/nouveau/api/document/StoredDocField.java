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

package org.apache.couchdb.nouveau.api.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public final class StoredDocField extends DocField {

    private Object value;

    public StoredDocField() {
    }

    public StoredDocField(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public StoredDocField(final String name, final byte[] value) {
        this.name = name;
        this.value = value;
    }

    public StoredDocField(final String name, final Number value) {
        this.name = name;
        this.value = value;
    }

    @JsonProperty
    public String getStringValue() {
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    public void setStringValue(String stringValue) {
        this.value = stringValue;
    }

    @JsonProperty
    public byte[] getBinaryValue() {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        return null;
    }

    public void setBinaryValue(byte[] binaryValue) {
        this.value = binaryValue;
    }

    @JsonProperty
    public Number getNumericValue() {
        if (value instanceof Number) {
            return (Number) value;
        }
        return null;
    }

    public void setNumericValue(Number numericValue) {
        this.value = numericValue;
    }

    @Override
    public String toString() {
        return "StoredDocField [name=" + name + ", value=" + value + "]";
    }

}
