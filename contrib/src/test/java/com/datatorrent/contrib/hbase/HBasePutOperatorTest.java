/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.datatorrent.contrib.hbase;

import com.datatorrent.contrib.hbase.HBasePutOperator;
import com.datatorrent.contrib.hbase.HBaseRowStatePersistence;
import com.datatorrent.contrib.hbase.HBaseStatePersistenceStrategy;
import com.malhartech.api.DAG;
import com.malhartech.api.LocalMode;
import junit.framework.Assert;
import org.apache.hadoop.hbase.client.Put;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pramod Immaneni <pramod@malhar-inc.com>
 */
public class HBasePutOperatorTest
{
  private static final Logger logger = LoggerFactory.getLogger(HBasePutOperatorTest.class);

  public HBasePutOperatorTest()
  {
  }

  @Test
  public void testPut()
  {
    try {
      HBaseTestHelper.clearHBase();
      LocalMode lma = LocalMode.newInstance();
      DAG dag = lma.getDAG();

      dag.setAttribute(DAG.APPLICATION_NAME, "HBasePutOperatorTest");
      HBaseRowTupleGenerator rtg = dag.addOperator("tuplegenerator", HBaseRowTupleGenerator.class);
      TestHBasePutOperator thop = dag.addOperator("testhbaseput", TestHBasePutOperator.class);
      dag.addStream("ss", rtg.outputPort, thop.inputPort);

      thop.setTableName("table1");
      thop.setZookeeperQuorum("127.0.0.1");
      thop.setZookeeperClientPort(2181);

      LocalMode.Controller lc = lma.getController();
      lc.setHeartbeatMonitoringEnabled(false);
      lc.run(30000);
      /*
      tuples = new ArrayList<HBaseTuple>();
      TestHBaseScanOperator thop = new TestHBaseScanOperator();
           thop.setTableName("table1");
      thop.setZookeeperQuorum("127.0.0.1");
      thop.setZookeeperClientPort(2822);
      thop.setupConfiguration();

      thop.emitTuples();
      */

      // TODO review the generated test code and remove the default call to fail.
      //fail("The test case is a prototype.");
      // Check total number
      HBaseTuple tuple = HBaseTestHelper.getHBaseTuple("row0", "colfam0", "col-0");
      Assert.assertNotNull("Tuple", tuple);
      Assert.assertEquals("Tuple row", tuple.getRow(), "row0");
      Assert.assertEquals("Tuple column family", tuple.getColFamily(), "colfam0");
      Assert.assertEquals("Tuple column name", tuple.getColName(), "col-0");
      Assert.assertEquals("Tuple column value", tuple.getColValue(), "val-0-0");
      tuple = HBaseTestHelper.getHBaseTuple("row499", "colfam0", "col-0");
      Assert.assertNotNull("Tuple", tuple);
      Assert.assertEquals("Tuple row", tuple.getRow(), "row499");
      Assert.assertEquals("Tuple column family", tuple.getColFamily(), "colfam0");
      Assert.assertEquals("Tuple column name", tuple.getColName(), "col-0");
      Assert.assertEquals("Tuple column value", tuple.getColValue(), "val-499-0");
    } catch (Exception ex) {
      ex.printStackTrace();
      logger.error(ex.getMessage());
      assert false;
    }
  }

  public static class TestHBasePutOperator extends HBasePutOperator<HBaseTuple> {

    @Override
    public Put operationPut(HBaseTuple t)
    {
      Put put = new Put(t.getRow().getBytes());
      put.add(t.getColFamily().getBytes(), t.getColName().getBytes(), t.getColValue().getBytes());
      return put;
    }

    @Override
    public HBaseStatePersistenceStrategy getPersistenceStrategy() {
      return new HBaseRowStatePersistence();
    }

  }
}
