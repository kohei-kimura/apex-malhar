/**
 * Copyright (c) 2012-2012 Malhar, Inc. All rights reserved.
 */
package com.datatorrent.lib.script;

import com.datatorrent.lib.algo.AllAfterMatchMapBenchmark;
import com.datatorrent.lib.script.BashOperator;
import com.malhartech.engine.TestSink;
import java.util.HashMap;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performance tests for {@link com.datatorrent.lib.script.BashOperator}. <p>
 * Testing with 1M tuples.
 * @author Dinesh Prasad (dinesh@malhar-inc.com)
 *
 */
public class BashOperatorBanchmark
{
	private static Logger log = LoggerFactory.getLogger(AllAfterMatchMapBenchmark.class);

  /**
   * Test node logic emits correct results
   */
  @Test
  @Category(com.malhartech.annotation.PerformanceTestCategory.class)
  public void testNodeProcessing() throws Exception
  {
		// Create bash operator instance (calculate suqare).
		BashOperator oper = new BashOperator();
		StringBuilder builder = new StringBuilder();
		builder.append("val = val * val;");
		oper.setScript(builder.toString());
		oper.setPassThru(true);
		TestSink sink = new TestSink();
		oper.result.setSink(sink);
 		
	  // generate process tuples  
		long startTime = System.nanoTime();
		oper.beginWindow(0);
		int numTuples = 10000000;
		for (int i = 0; i < numTuples; i++) 
		{
			HashMap<String, Object> tuple = new HashMap<String, Object>();
			tuple.put("val", new Integer(i));
		}
		oper.endWindow();
		long endTime = System.nanoTime();
		long total = (startTime - endTime)/1000; 
		log.debug(String.format("\nBenchmarked %d tuples in %d ms", numTuples, total));
  }
}
