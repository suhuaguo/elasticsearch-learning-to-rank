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

package com.o19s.es.ltr.ranker.dectree;

import com.o19s.es.ltr.ranker.ArrayDataPoint;
import com.o19s.es.ltr.ranker.LtrRanker;

import java.util.Objects;

/**
 * Naive implementation of additive decision tree.
 * May be slow when the number of trees and tree complexity if high comparatively to the number of features.
 */
public class NaiveAdditiveDecisionTree implements LtrRanker {
    private final Node[] trees;
    private final float[] weights;
    private final int modelSize;

    /**
     * TODO: Constructor for these classes are strict and not really
     * designed for a fluent building process. We might consider
     * changing this according to model parsers we implement.
     *
     * @param trees an array of trees
     * @param weights the respective weights
     * @param modelSize the modelSize in number of feature used
     */
    public NaiveAdditiveDecisionTree(Node[] trees, float[] weights, int modelSize) {
        assert trees.length == weights.length;
        this.trees = trees;
        this.weights = weights;
        this.modelSize = modelSize;
    }

    @Override
    public String name() {
        return "naive_additive_decision_tree";
    }

    @Override
    public ArrayDataPoint newDataPoint() {
        return new ArrayDataPoint(modelSize);
    }

    @Override
    public float score(DataPoint point) {
        assert point instanceof ArrayDataPoint;
        float sum = 0;
        float[] scores = ((ArrayDataPoint) point).scores;
        for (int i = 0; i < trees.length; i++) {
            sum += weights[i]*trees[i].eval(scores);
        }
        return sum;
    }

    @Override
    public int size() {
        return modelSize;
    }

    public interface Node {
         boolean isLeaf();
         float eval(float[] scores);
    }

    public static class Split implements Node {
        private final Node left;
        private final Node right;
        private final int feature;
        private final float threshold;

        public Split(Node left, Node right, int feature, float threshold) {
            this.left = Objects.requireNonNull(left);
            this.right = Objects.requireNonNull(right);
            this.feature = feature;
            this.threshold = threshold;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public float eval(float[] scores) {
            Node n = this;
            while (!n.isLeaf()) {
                assert n instanceof Split;
                Split s = (Split) n;
                if (s.threshold >= scores[feature]) {
                    n = s.left;
                } else {
                    n = s.right;
                }
            }
            assert n instanceof Leaf;
            return n.eval(scores);
        }
    }

    public static class Leaf implements Node {
        private final float output;

        public Leaf(float output) {
            this.output = output;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public float eval(float[] scores) {
            return output;
        }
    }
}
