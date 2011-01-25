/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.mrunit;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mrunit.types.Pair;

/**
 * Harness that allows you to test a Mapper and a Reducer instance together
 * You provide the input key and value that should be sent to the Mapper, and
 * outputs you expect to be sent by the Reducer to the collector for those
 * inputs. By calling runTest(), the harness will deliver the input to the
 * Mapper, feed the intermediate results to the Reducer (without checking
 * them), and will check the Reducer's outputs against the expected results.
 * This is designed to handle a single (k, v)* -> (k, v)* case from the
 * Mapper/Reducer pair, representing a single unit test.
 */
public class MapReduceDriver<K1, V1, K2, V2, K3, V3>
    extends TestDriver<K1, V1, K3, V3> {

  public static final Log LOG = LogFactory.getLog(MapReduceDriver.class);

  private Mapper<K1, V1, K2, V2> myMapper;
  private Reducer<K2, V2, K3, V3> myReducer;

  private List<Pair<K1, V1>> inputList;

  public MapReduceDriver(final Mapper<K1, V1, K2, V2> m,
                         final Reducer<K2, V2, K3, V3> r) {
    myMapper = m;
    myReducer = r;
    inputList = new ArrayList<Pair<K1, V1>>();
  }

  public MapReduceDriver() {
    inputList = new ArrayList<Pair<K1, V1>>();
  }

  /** Set the Mapper instance to use with this test driver
   * @param m the Mapper instance to use */
  public void setMapper(Mapper<K1, V1, K2, V2> m) {
    myMapper = m;
  }

  /** Sets the Mapper instance to use and returns self for fluent style */
  public MapReduceDriver<K1, V1, K2, V2, K3, V3> withMapper(
          Mapper<K1, V1, K2, V2> m) {
    setMapper(m);
    return this;
  }

  /**
   * @return the Mapper object being used by this test
   */
  public Mapper<K1, V1, K2, V2> getMapper() {
    return myMapper;
  }

  /**
   * Sets the reducer object to use for this test
   * @param r The reducer object to use
   */
  public void setReducer(Reducer<K2, V2, K3, V3> r) {
    myReducer = r;
  }

  /**
   * Identical to setReducer(), but with fluent programming style
   * @param r The Reducer to use
   * @return this
   */
  public MapReduceDriver<K1, V1, K2, V2, K3, V3> withReducer(
          Reducer<K2, V2, K3, V3> r) {
    setReducer(r);
    return this;
  }

  /**
   * @return the Reducer object being used for this test
   */
  public Reducer<K2, V2, K3, V3> getReducer() {
    return myReducer;
  }

  /**
   * Adds an input to send to the mapper
   * @param key
   * @param val
   */
  public void addInput(K1 key, V1 val) {
    inputList.add(new Pair<K1, V1>(key, val));
  }

  /**
   * Identical to addInput() but returns self for fluent programming style
   * @param key
   * @param val
   * @return this
   */
  public MapReduceDriver<K1, V1, K2, V2, K3, V3> withInput(K1 key, V1 val) {
    addInput(key, val);
    return this;
  }

  /**
   * Adds an input to send to the Mapper
   * @param input The (k, v) pair to add to the input list.
   */
  public void addInput(Pair<K1, V1> input) {
    if (null == input) {
      throw new IllegalArgumentException("Null input in addInput()");
    }

    inputList.add(input);
  }

  /**
   * Identical to addInput() but returns self for fluent programming style
   * @param input The (k, v) pair to add
   * @return this
   */
  public MapReduceDriver<K1, V1, K2, V2, K3, V3> withInput(
      Pair<K1, V1> input) {
    addInput(input);
    return this;
  }

  /**
   * Adds an output (k, v) pair we expect from the Reducer
   * @param outputRecord The (k, v) pair to add
   */
  public void addOutput(Pair<K3, V3> outputRecord) {
    if (null != outputRecord) {
      expectedOutputs.add(outputRecord);
    } else {
      throw new IllegalArgumentException("Tried to add null outputRecord");
    }
  }

  /**
   * Works like addOutput(), but returns self for fluent style
   * @param outputRecord
   * @return this
   */
  public MapReduceDriver<K1, V1, K2, V2, K3, V3> withOutput(
          Pair<K3, V3> outputRecord) {
    addOutput(outputRecord);
    return this;
  }

  /**
   * Adds a (k, v) pair we expect as output from the Reducer
   * @param key
   * @param val
   */
  public void addOutput(K3 key, V3 val) {
    addOutput(new Pair<K3, V3>(key, val));
  }

  /**
   * Functions like addOutput() but returns self for fluent programming style
   * @param key
   * @param val
   * @return this
   */
  public MapReduceDriver<K1, V1, K2, V2, K3, V3> withOutput(K3 key, V3 val) {
    addOutput(key, val);
    return this;
  }

  /**
   * Expects an input of the form "key \t val"
   * Forces the Mapper input types to Text.
   * @param input A string of the form "key \t val". Trims any whitespace.
   */
  public void addInputFromString(String input) {
    if (null == input) {
      throw new IllegalArgumentException("null input given to setInput");
    } else {
      Pair<Text, Text> inputPair = parseTabbedPair(input);
      if (null != inputPair) {
        // I know this is not type-safe, but I don't
        // know a better way to do this.
        addInput((Pair<K1, V1>) inputPair);
      } else {
        throw new IllegalArgumentException("Could not parse input pair in addInput");
      }
    }
  }

  /**
   * Identical to addInputFromString, but with a fluent programming style
   * @param input A string of the form "key \t val". Trims any whitespace.
   * @return this
   */
  public MapReduceDriver<K1, V1, K2, V2, K3, V3> withInputFromString(String input) {
    addInputFromString(input);
    return this;
  }

  /**
   * Expects an input of the form "key \t val"
   * Forces the Reducer output types to Text.
   * @param output A string of the form "key \t val". Trims any whitespace.
   */
  public void addOutputFromString(String output) {
    if (null == output) {
      throw new IllegalArgumentException("null input given to setOutput");
    } else {
      Pair<Text, Text> outputPair = parseTabbedPair(output);
      if (null != outputPair) {
        // I know this is not type-safe,
        // but I don't know a better way to do this.
        addOutput((Pair<K3, V3>) outputPair);
      } else {
        throw new IllegalArgumentException(
            "Could not parse output pair in setOutput");
      }
    }
  }

  /**
   * Identical to addOutputFromString, but with a fluent programming style
   * @param output A string of the form "key \t val". Trims any whitespace.
   * @return this
   */
  public MapReduceDriver<K1, V1, K2, V2, K3, V3> withOutputFromString(String output) {
    addOutputFromString(output);
    return this;
  }

  public List<Pair<K3, V3>> run() throws IOException {

    List<Pair<K2, V2>> mapOutputs = new ArrayList<Pair<K2, V2>>();

    // run map component
    for (Pair<K1, V1> input : inputList) {
      LOG.debug("Mapping input " + input.toString() + ")");

      mapOutputs.addAll(new MapDriver<K1, V1, K2, V2>(myMapper).withInput(
              input).run());
    }

    List<Pair<K2, List<V2>>> reduceInputs = shuffle(mapOutputs);
    List<Pair<K3, V3>> reduceOutputs = new ArrayList<Pair<K3, V3>>();

    for (Pair<K2, List<V2>> input : reduceInputs) {
      K2 inputKey = input.getFirst();
      List<V2> inputValues = input.getSecond();
      StringBuilder sb = new StringBuilder();
      formatValueList(inputValues, sb);
      LOG.debug("Reducing input (" + inputKey.toString() + ", "
          + sb.toString() + ")");

      reduceOutputs.addAll(new ReduceDriver<K2, V2, K3, V3>(myReducer)
              .withInputKey(inputKey).withInputValues(inputValues).run());
    }

    return reduceOutputs;
  }

  @Override
  public void runTest() throws RuntimeException {
    List<Pair<K3, V3>> reduceOutputs = null;
    boolean succeeded;

    try {
      reduceOutputs = run();
      validate(reduceOutputs);
    } catch (IOException ioe) {
      LOG.error("IOException: " + ioe.toString());
      LOG.debug("Setting success to false based on IOException");
      throw new RuntimeException();
    }
  }

  /** Take the outputs from the Mapper, combine all values for the
   *  same key, and sort them by key.
   * @param mapOutputs An unordered list of (key, val) pairs from the mapper
   * @return the sorted list of (key, list(val))'s to present to the reducer
   */
  List<Pair<K2, List<V2>>> shuffle(List<Pair<K2, V2>> mapOutputs) {
    HashMap<K2, List<V2>> reducerInputs = new HashMap<K2, List<V2>>();

    // step 1: condense all values with the same key into a list.
    for (Pair<K2, V2> mapOutput : mapOutputs) {
      List<V2> valuesForKey = reducerInputs.get(mapOutput.getFirst());

      if (null == valuesForKey) {
        // this is the first (k, v) pair for this key. Add it to the list.
        valuesForKey = new ArrayList<V2>();
        valuesForKey.add(mapOutput.getSecond());
        reducerInputs.put(mapOutput.getFirst(), valuesForKey);
      } else {
        // add this value to the existing list for this key
        valuesForKey.add(mapOutput.getSecond());
      }
    }

    // build a list out of these (k, list(v)) pairs
    List<Pair<K2, List<V2>>> finalInputs = new ArrayList<Pair<K2, List<V2>>>();
    Set<Map.Entry<K2, List<V2>>> entries = reducerInputs.entrySet();
    for (Map.Entry<K2, List<V2>> entry : entries) {
      K2 key = entry.getKey();
      List<V2> vals = entry.getValue();
      finalInputs.add(new Pair<K2, List<V2>>(key, vals));
    }

    // and then sort the output list by key
    if (finalInputs.size() > 0) {
      Collections.sort(finalInputs,
              finalInputs.get(0).new FirstElemComparator());
    }

    return finalInputs;
  }
}
