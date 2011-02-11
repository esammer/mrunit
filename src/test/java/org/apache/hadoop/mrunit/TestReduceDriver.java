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

import static org.apache.hadoop.mrunit.testutil.ExtendedAssert.assertListEquals;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.LongSumReducer;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.Before;
import org.junit.Test;

public class TestReduceDriver extends TestCase {

  private static final int IN_A = 4;
  private static final int IN_B = 6;
  private static final int OUT_VAL = 10;
  private static final int INCORRECT_OUT = 12;
  private static final int OUT_EMPTY = 0;

  private Reducer<Text, LongWritable, Text, LongWritable> reducer;
  private ReduceDriver<Text, LongWritable, Text, LongWritable> driver;

  @Before
  public void setUp() throws Exception {
    reducer = new LongSumReducer<Text>();
    driver = new ReduceDriver<Text, LongWritable, Text, LongWritable>(
                   reducer);
  }

  @Test
  public void testRun() {
    List<Pair<Text, LongWritable>> out = null;

    try {
      out = driver.withInputKey(new Text("foo"))
                  .withInputValue(new LongWritable(IN_A))
                  .withInputValue(new LongWritable(IN_B))
                  .run();
    } catch (IOException ioe) {
      fail();
    }

    List<Pair<Text, LongWritable>> expected =
        new ArrayList<Pair<Text, LongWritable>>();
    expected.add(new Pair<Text, LongWritable>(new Text("foo"),
            new LongWritable(OUT_VAL)));

    assertListEquals(out, expected);

  }

  @Test
  public void testTestRun1() {
    driver
            .withInputKey(new Text("foo"))
            .withOutput(new Text("foo"), new LongWritable(0))
            .runTest();
  }

  @Test
  public void testTestRun2() {
    driver
            .withInputKey(new Text("foo"))
            .withInputValue(new LongWritable(IN_A))
            .withInputValue(new LongWritable(IN_B))
            .withOutput(new Text("foo"), new LongWritable(OUT_VAL))
            .runTest();
  }

  @Test
  public void testTestRun3() {
    try {
      driver
            .withInputKey(new Text("foo"))
            .withInputValue(new LongWritable(IN_A))
            .withInputValue(new LongWritable(IN_B))
            .withOutput(new Text("bar"), new LongWritable(OUT_VAL))
            .runTest();
      fail();
    } catch (RuntimeException re) {
      // expected.
    }

  }

  @Test
  public void testTestRun4() {
    try {
      driver
            .withInputKey(new Text("foo"))
            .withInputValue(new LongWritable(IN_A))
            .withInputValue(new LongWritable(IN_B))
            .withOutput(new Text("foo"), new LongWritable(INCORRECT_OUT))
            .runTest();
      fail();
    } catch (RuntimeException re) {
      // expected.
    }
  }

  @Test
  public void testTestRun5() {
    try {
      driver
            .withInputKey(new Text("foo"))
            .withInputValue(new LongWritable(IN_A))
            .withInputValue(new LongWritable(IN_B))
            .withOutput(new Text("foo"), new LongWritable(IN_A))
            .runTest();
      fail();
    } catch (RuntimeException re) {
      // expected.
    }
  }

  @Test
  public void testTestRun6() {
    try {
      driver
            .withInputKey(new Text("foo"))
            .withInputValue(new LongWritable(IN_A))
            .withInputValue(new LongWritable(IN_B))
            .withOutput(new Text("foo"), new LongWritable(IN_A))
            .withOutput(new Text("foo"), new LongWritable(IN_B))
            .runTest();
      fail();
    } catch (RuntimeException re) {
      // expected.
    }
  }

  @Test
  public void testTestRun7() {
    try {
      driver
              .withInputKey(new Text("foo"))
              .withInputValue(new LongWritable(IN_A))
              .withInputValue(new LongWritable(IN_B))
              .withOutput(new Text("foo"), new LongWritable(OUT_VAL))
              .withOutput(new Text("foo"), new LongWritable(OUT_VAL))
              .runTest();
      fail();
    } catch (RuntimeException re) {
      // expected.
    }
  }

  @Test
  public void testTestRun8() {
    try {
      driver
            .withInputKey(new Text("foo"))
            .withInputValue(new LongWritable(IN_A))
            .withInputValue(new LongWritable(IN_B))
            .withOutput(new Text("bar"), new LongWritable(OUT_VAL))
            .withOutput(new Text("foo"), new LongWritable(OUT_VAL))
            .runTest();
            fail();
    } catch (RuntimeException re) {
      // expected.
    }
  }

  @Test
  public void testTestRun9() {
    try {
      driver
            .withInputKey(new Text("foo"))
            .withInputValue(new LongWritable(IN_A))
            .withInputValue(new LongWritable(IN_B))
            .withOutput(new Text("foo"), new LongWritable(OUT_VAL))
            .withOutput(new Text("bar"), new LongWritable(OUT_VAL))
            .runTest();
      fail();
    } catch (RuntimeException re) {
      // expected.
    }
  }

  @Test
  public void testEmptyInput() {
    // (null, <empty>) will be forcibly fed as input
    // since we use LongSumReducer, expect (null, 0) out.
    driver
            .withOutput(null, new LongWritable(OUT_EMPTY))
            .runTest();
  }

  @Test
  public void testEmptyInput2() {
    // because a null key with zero inputs will be fed as input
    // to this reducer, do not accept no outputs.
    try {
      driver.runTest();
      fail();
    } catch (RuntimeException re) {
      // expected.
    }
  }

  /**
   * Reducer that counts its values twice; the second iteration
   * according to mapreduce semantics should be empty.
   */
  private static class DoubleIterReducer<K, V>
      extends MapReduceBase implements Reducer<K, V, K, LongWritable> {
    public void reduce(K key, Iterator<V> values,
        OutputCollector<K, LongWritable> out, Reporter r) throws IOException {
      long count = 0;

      while (values.hasNext()) {
        count++;
        values.next();
      }

      // This time around, iteration should yield no values.
      while (values.hasNext()) {
        count++;
        values.next();
      }
      out.collect(key, new LongWritable(count));
    }
  }

  @Test
  public void testDoubleIteration() {
    reducer = new DoubleIterReducer<Text, LongWritable>();
    driver = new ReduceDriver<Text, LongWritable, Text, LongWritable>(
        reducer);

    driver
        .withInputKey(new Text("foo"))
        .withInputValue(new LongWritable(1))
        .withInputValue(new LongWritable(1))
        .withInputValue(new LongWritable(1))
        .withInputValue(new LongWritable(1))
        .withOutput(new Text("foo"), new LongWritable(4))
        .runTest();
  }
}

