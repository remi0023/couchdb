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

package org.apache.couchdb.nouveau.api.facet.range;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public final class DoubleRange {

    private String label;

    private double min;

    private boolean minInclusive = true;

    private double max;

    private boolean maxInclusive = true;

    public DoubleRange() {
        // Jackson serialization
    }

    public DoubleRange(String label, double min, boolean minInclusive, double max, boolean maxInclusive) {
        this.label = label;
        this.min = min;
        this.minInclusive = minInclusive;
        this.max = max;
        this.maxInclusive = maxInclusive;
    }

    @JsonProperty
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty
    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    @JsonProperty
    public boolean getMinInclusive() {
        return minInclusive;
    }

    public void setMinInclusive(boolean minInclusive) {
        this.minInclusive = minInclusive;
    }

    @JsonProperty
    public double getMax() 
    {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    @JsonProperty
    public boolean getMaxInclusive() {
        return maxInclusive;
    }

    public void setMaxInclusive(boolean maxInclusive) {
        this.maxInclusive = maxInclusive;
    }

    @Override
    public String toString() {
        return "DoubleRange [label=" + label + ", min=" + min + ", minInclusive=" + minInclusive + ", max=" + max
                + ", maxInclusive=" + maxInclusive + "]";
    }
        
}

