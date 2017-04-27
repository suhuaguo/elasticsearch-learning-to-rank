/*
 * Copyright [2017] Wikimedia Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.o19s.es.ltr.feature;

import org.elasticsearch.common.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class PrebuiltFeatureSet implements FeatureSet {
    private final List<PrebuiltFeature> features;
    private final String name;

    public PrebuiltFeatureSet(@Nullable String name, List<PrebuiltFeature> features) {
        this.name = name;
        this.features = Objects.requireNonNull(features);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public PrebuiltFeature[] asArray() {
        return features.toArray(new PrebuiltFeature[features.size()]);
    }

    @Override
    public int featureOrdinal(String featureName) {
        int ord = findFeatureIndexByName(featureName);
        if (ord < 0) {
            throw new IllegalArgumentException("Unknown feature [" + featureName + "]");
        }
        return ord;
    }

    @Override
    public Feature feature(int ord) {
        return features.get(ord);
    }

    @Override
    public PrebuiltFeature feature(String name) {
        return features.get(featureOrdinal(name));
    }

    @Override
    public boolean hasFeature(String name) {
        return findFeatureIndexByName(name) >= 0;
    }

    @Override
    public int size() {
        return features.size();
    }

    private int findFeatureIndexByName(String featureName) {
        // slow, not meant for runtime usage, mostly needed for tests
        // would make sense to implement a Map to do this once
        // feature names are mandatory and unique.
        return IntStream.range(0, features.size())
                .filter(i -> Objects.equals(features.get(i).name(), featureName))
                .findFirst()
                .orElse(-1);
    }
}
