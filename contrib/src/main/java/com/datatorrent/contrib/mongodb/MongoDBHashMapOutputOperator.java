/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.datatorrent.contrib.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import org.bson.types.ObjectId;

/**
 * MongoDB HashMap output adapter operator, which send insertion data to database.<p><br>
 *
 * <br>
 * Ports:<br>
 * <b>Input</b>: Can have one input port, derived from base class <br>
 * <b>Output</b>: no output port<br>
 * <br>
 * Properties:<br>
 * <b>propTableMap</b>:prop-Table mapping, used for insertion tuple mapping<br>
 * <b>propColumnMap</b>:prop-Column mapping, used for insertion tuple mapping<br>
 * <br>
 * Compile time checks:<br>
 * None<br>
 * <br>
 * Run time checks:<br>
 * None<br>
 * <br>
 * <b>Benchmarks</b>:
 * <br>
 *
 * @author Zhongjian Wang <zhongjian@malhar-inc.com>
 */
public class MongoDBHashMapOutputOperator<T> extends MongoDBOutputOperator<HashMap<String, T>>
{
  public transient HashMap<String, String> propTableMap = new HashMap<String, String>();  // prop-table mapping for HashMap
  public transient HashMap<String, String> propColumnMap = new HashMap<String, String>();  // prop-column mapping for HashMap

  /**
   * set Column mapping information at operator creation time
   * @param mapping
   */
  @Override
  public void setColumnMapping(String[] mapping)
  {
    for (String str : mapping) {
      String[] tokens = str.split("[:]");
      String[] subtok = tokens[1].split("[.]");
      String table = subtok[0];
      String column = subtok[1];
      String prop = tokens[0];
      String type = tokens[2];
      if (tableList.contains(table) == false) {
        tableList.add(table);
      }
      propTableMap.put(prop, table);
      propColumnMap.put(prop, column);
    }
  }

  /**
   * process tuple based on HashMap tuple
   * @param tuple
   */
  @Override
  public void processTuple(HashMap<String, T> tuple)
  {
    tableToDocument.clear();
    BasicDBObject doc = null;
    for (Map.Entry<String, T> entry : tuple.entrySet()) {
      String prop = entry.getKey();
      String table = propTableMap.get(prop);
      String column = propColumnMap.get(prop);
      if ((doc = tableToDocument.get(table)) == null) {
        doc = new BasicDBObject();
        doc.put(column, entry.getValue());
      }
      else {
        doc.put(column, entry.getValue());
      }
      tableToDocument.put(table, doc);
    }

    processTupleCommon();
  }
}
