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

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value=Include.NON_DEFAULT)
public final class TextField extends Field {

    private final String value;

    private final boolean store;

    private final boolean facet;

    private final boolean sortable;

    @JsonCreator
    public TextField(
        @NotNull @JsonProperty("name") final String name,
        @NotNull @JsonProperty("value") final String value,
        @JsonProperty("store") final boolean store,
        @JsonProperty("facet") final boolean facet,
        @JsonProperty("sortable") final boolean sortable) {
        super(name);
        this.value = value;
        this.store = store;
        this.facet = facet;
        this.sortable = sortable;
    }

    @JsonProperty
    public String getValue() {
        return value;
    }

    @JsonProperty
    public boolean isStore() {
        return store;
    }

    @JsonProperty
    public boolean isFacet() {
        return facet;
    }

    @JsonProperty
    public boolean isSortable() {
        return sortable;
    }

    @Override
    public String toString() {
        return "TextField [name=" + name + ", value=" + value + ", store=" + store + ", facet=" + facet + ", sortable=" + sortable
                + "]";
    }

}
