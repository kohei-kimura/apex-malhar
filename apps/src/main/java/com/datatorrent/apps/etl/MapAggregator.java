/*
 * Copyright (c) 2014 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.apps.etl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import com.datatorrent.lib.statistics.DimensionsComputation;

public class MapAggregator implements DimensionsComputation.Aggregator<Map<String, Object>, MapAggregator.MapAggregateEvent>
{

  private TimeUnit time;
  private Set<String> dimensionKeys;
  List<Metric> metrics;

  public void init(String dimension, List<Metric> operations)
  {
    this.metrics = Preconditions.checkNotNull(operations, "metrics");
    this.dimensionKeys = Sets.newHashSet();
    String[] attributes = dimension.split(":");

    for (String attribute : attributes) {
      String[] keyval = attribute.split("=", 2);
      String key = keyval[0];
      if (key.equals(Constants.TIME_ATTR)) {
        time = TimeUnit.valueOf(keyval[1]);
      }
      else {
        dimensionKeys.add(key);
      }
    }

    dimensionKeys.add(Constants.LOG_TYPE);
  }

  @Override
  public MapAggregateEvent getGroup(Map<String, Object> src, int aggregatorIndex)
  {
    MapAggregateEvent aggregateEvent = new MapAggregateEvent(aggregatorIndex, computeHashCode(src));

    if (time != null) {
      Long srcTime = (Long) src.get(Constants.TIME_ATTR);
      aggregateEvent.put(Constants.TIME_ATTR, TimeUnit.MILLISECONDS.convert(time.convert(srcTime, TimeUnit.MILLISECONDS), time));

    }
    for (String aDimension : dimensionKeys) {
      Object srcDimension = src.get(aDimension);
      if (srcDimension == null) {
        aggregateEvent.put(aDimension, Constants.RESERVED_DIMENSION.NOT_PRESENT);
      }
      else {
        aggregateEvent.put(aDimension, srcDimension);
      }
    }

    return aggregateEvent;
  }

  @Override
  public void aggregate(MapAggregateEvent dest, Map<String, Object> src)
  {
    for (Metric metric : metrics) {
      Object result = metric.operation.compute(dest.get(metric.destinationKey), src.get(metric.sourceKey));
      dest.put(metric.destinationKey, result);
    }

    if (time != null) {
      Long destTime = (Long) dest.get(Constants.TIME_ATTR);
      Long srcTime = (Long) src.get(Constants.TIME_ATTR);
      if (destTime < srcTime) {
        dest.put(Constants.TIME_ATTR, srcTime);
      }
    }
  }

  @Override
  public void aggregate(MapAggregateEvent dest, MapAggregateEvent src)
  {
    for (Metric metric : metrics) {
      Object result = metric.operation.compute(dest.get(metric.destinationKey), src.get(metric.sourceKey));
      dest.put(metric.destinationKey, result);
    }

    if (time != null) {
      Long destTime = (Long) dest.get(Constants.TIME_ATTR);
      Long srcTime = (Long) src.get(Constants.TIME_ATTR);
      if (destTime < srcTime) {
        dest.put(Constants.TIME_ATTR, srcTime);
      }
    }
  }

  @Override
  public int computeHashCode(Map<String, Object> event)
  {
    int hash = 7;
    if (time != null) {
      long ltime = time.convert((Long) event.get(Constants.TIME_ATTR), TimeUnit.MILLISECONDS);
      hash = 43 * hash + (int) (ltime ^ (ltime >>> 32));
    }

    for (String aDimension : dimensionKeys) {
      Object dimensionVal = event.get(aDimension);
      hash = 43 * hash + (dimensionVal != null ? dimensionVal.hashCode() : 0);
    }
    return hash;
  }

  @Override
  public boolean equals(Map<String, Object> event1, Map<String, Object> event2)
  {
    if (event1 == event2) {
      return true;
    }

    if (event2 == null) {
      return false;
    }

    if (event1.getClass() != event2.getClass()) {
      return false;
    }

    if (time != null && time.convert((Long) event1.get(Constants.TIME_ATTR), TimeUnit.MILLISECONDS) != time.convert((Long) event2.get(Constants.TIME_ATTR), TimeUnit.MILLISECONDS)) {
      return false;
    }

    for (String aDimension : dimensionKeys) {
      Object event1Dimension = event1.get(aDimension);
      Object event2Dimension = event2.get(aDimension);

      if (!Objects.equal(event1Dimension, event2Dimension)) {
        return false;
      }
    }
    return true;
  }

  public static class MapAggregateEvent extends HashMap<String, Object> implements DimensionsComputation.AggregateEvent
  {
    int hash;
    int aggregatorIndex;

    private MapAggregateEvent()
    {
      //Used for kryo serialization
    }

    MapAggregateEvent(int aggregatorIndex, int hash)
    {
      this.aggregatorIndex = aggregatorIndex;
      this.hash = hash;
    }

    @Override
    public boolean equals(Object o)
    {
      if (this == o) {
        return true;
      }
      if (!(o instanceof MapAggregateEvent)) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }

      MapAggregateEvent that = (MapAggregateEvent) o;

      return aggregatorIndex == that.aggregatorIndex && hash == that.hash;

    }

    @Override
    public int hashCode()
    {
      int result = super.hashCode();
      result = 31 * result + hash;
      result = 31 * result + aggregatorIndex;
      return result;
    }

    @Override
    public int getAggregatorIndex()
    {
      return aggregatorIndex;
    }
  }
}
