/*
 *  Copyright (c) 2012 Malhar, Inc.
 *  All Rights Reserved.
 */
package com.datatorrent.lib.algo;

import com.datatorrent.lib.util.BaseMatchOperator;
import com.malhartech.api.annotation.InputPortFieldAnnotation;
import com.malhartech.api.annotation.OutputPortFieldAnnotation;
import com.malhartech.api.DefaultInputPort;
import com.malhartech.api.DefaultOutputPort;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * A compare operation on a Number tuple based on the property "key", "value", and "cmp"; the first match is emitted. The comparison is done by getting double
 * value from the Number.<p>
 * This module is a pass through<br>
 * The operators by default assumes immutable keys. If the key is mutable, use cloneKey to make a copy<br>
 * <br>
 * <b>Ports</b>:<br>
 * <b>data</b>: expects Map&lt;K,V extends Number&gt;<br>
 * <b>first</b>: emits HashMap&lt;K,V&gt;<br>
 * <br>
 * <b>Properties</b>:<br>
 * <b>key</b>: The key on which compare is done<br>
 * <b>value</b>: The value to compare with<br>
 * <b>cmp</b>: The compare function. Supported values are "lte", "lt", "eq", "neq", "gt", "gte". Default is "eq"<br>
 * <br>
 * <b>Specific compile time checks</b>:<br>
 * Key must be non empty<br>
 * Value must be able to convert to a "double"<br>
 * Compare string, if specified, must be one of "lte", "lt", "eq", "neq", "gt", "gte"<br>
 * <br>
 * <b>Specific run time checks</b>: None <br>
 * <br>
 * <b>Benchmarks</b>: Blast as many tuples as possible in inline mode<br>
 * <table border="1" cellspacing=1 cellpadding=1 summary="Benchmark table for FirstMatchMap&lt;K,V extends Number&gt; operator template">
 * <tr><th>In-Bound</th><th>Out-bound</th><th>Comments</th></tr>
 * <tr><td><b>&gt; 50 Million K,V pairs/s</b></td><td>Emits only 1 tuple per window</td><td>In-bound throughput and the occurrence of the
 * first match are the main determinant of performance.</td></tr>
 * </table><br>
 * <p>
 * <b>Function Table (K=String,V=Integer); keys=a; cmp=eq; value=3</b>:
 * <table border="1" cellspacing=1 cellpadding=1 summary="Function table for FirstMatchMap&lt;K,V extends Number&gt; operator template">
 * <tr><th rowspan=2>Tuple Type (api)</th><th>In-bound (process)</th><th>Out-bound (emit)</th></tr>
 * <tr><th><i>data</i>(Map&lt;K,V&gt;)</th><th><i>first</i>(HashMap&lt;K,V&gt;)</th></tr>
 * <tr><td>Begin Window (beginWindow())</td><td>N/A</td><td>N/A</td></tr>
 * <tr><td>Data (process())</td><td>{a=2,b=20,c=1000}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{a=-1}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{a=2,b=5}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{a=5,b=-5}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{a=3,h=20,c=1000,b=-5}</td><td>{a=3,h=20,c=1000,b=-5}</td></tr>
 * <tr><td>Data (process())</td><td>{d=55,b=5}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{d=14}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{d=55,e=2}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{d=1,d=5,d=55}</td><td></td></tr>
 * <tr><td>Data (process())</td><td>{d=1,a=3,e=2}</td><td></td></tr>
 * <tr><td>End Window (endWindow())</td><td>N/A</td><td>N/A</td></tr>
 * </table>
 * <br>
 * @author Amol Kekre (amol@malhar-inc.com)<br>
 * <br>
 *
 */
public class FirstMatchMap<K, V extends Number> extends BaseMatchOperator<K,V>
{
  @InputPortFieldAnnotation(name="data")
  public final transient DefaultInputPort<Map<K, V>> data = new DefaultInputPort<Map<K, V>>(this)
  {
    /**
     * Checks if required key,val pair exists in the HashMap. If so tuple is emitted, and emitted flag is set
     * to true
     */
    @Override
    public void process(Map<K, V> tuple)
    {
      if (emitted) {
        return;
      }
      V val = tuple.get(getKey());
      if (val == null) { // skip if key does not exist
        return;
      }
      if (compareValue(val.doubleValue())) {
        first.emit(cloneTuple(tuple));
        emitted = true;
      }
    }
  };

  @OutputPortFieldAnnotation(name="first")
  public final transient DefaultOutputPort<HashMap<K, V>> first = new DefaultOutputPort<HashMap<K, V>>(this);
  boolean emitted = false;

  /**
   * Resets emitted flag to false
   * @param windowId
   */
  @Override
  public void beginWindow(long windowId)
  {
    emitted = false;
  }
}
