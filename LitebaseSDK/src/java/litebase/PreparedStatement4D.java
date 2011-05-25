/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

package litebase;

import totalcross.sys.*;
import totalcross.util.*;

/**
 * Native class that represents a SQL Statement that can be prepared (compiled) once and executed many times with different parameter values.
 */
public class PreparedStatement4D
{
   /**
    * The SQL command expression.
    */
   String sqlExpression;

   /**
    * The Litebase connection.
    */
   Object driver;

   /**
    * The type of the statement. It can be one out of <code>SQLElement.STMT_INSERT</code>,<code> SQLElement.STMT_UPDATE</code>, 
    * <code>SQLElement.STMT_DELETE</code>, <code>SQLElement.STMT_DROP</code>, <code>SQLElement.STMT_ALTER</code>, 
    * and <code>SQLElement.STMT_CREATE</code>.
    */
   int type;

   /**
    * The parameters for the prepared statement in string format.
    */
   long paramsAsStrs; // guich@566_15

   /**
    * The positions of the '?' in the sql string.
    */
   long paramsPos;
   
   /**
    * The length of the parameters as strings;
    */
   long paramsLength;

   /**
    * Indicates how many parameters are in the SQL command.
    */
   int storedParams; // guich@566_15

   /**
    * The statement.
    */
   long statement;
   
   /**
    * A flag that indicates that this class has already been finalized.
    */
   boolean dontFinalize;
   
   // juliana@222_8: corrected a bug that could collect strings and blobs passed to a prepared statement, causing invalid data insertion/update on 
   // Palm, Windows 32, Windows CE, iPhone, and Android.
   /**
    * An array of objects hooked in the prepared statement record.
    */
   Object[] ObjParams;
   
   // juliana@230_11: Litebase public class constructors are now not public any more. 
   /**
    * The constructor.
    */
   private PreparedStatement4D() {}

   /**
    * This method executes a prepared SQL query and returns its <code>ResultSet</code>.
    *
    * @return The <code>ResultSet</code> of the SQL statement.
    * @throws DriverException If the statement to be execute is not a select, there are undefined parameters or the driver is closed.
    * @throws OutOfMemoryError If a memory allocation fails.
    */
   public native ResultSet executeQuery() throws DriverException, OutOfMemoryError;

   /**
    * This method executes a SQL <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code> statement. SQL statements that return nothing such 
    * as SQL DDL statements can also be executed.
    *
    * @return The result is either the row count for <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code> statements; or 0 for SQL 
    * statements that return nothing.
    * @throws DriverException If the query does not update the table, there are undefined parameters, the driver is closed or the table was dropped.
    */
   public native int executeUpdate() throws DriverException;

   /**
    * This method sets the specified parameter from the given Java <code>short</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public native void setShort(int index, short value);

   /**
    * This method sets the specified parameter from the given Java <code>int</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.   
    */
   public native void setInt(int index, int value);

   /**
    * This method sets the specified parameter from the given Java <code>long</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public native void setLong(int index, long value);
   
   /**
    * This method sets the specified parameter from the given Java <code>float</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public native void setFloat(int index, double value);

   /**
    * This method sets the specified parameter from the given Java <code>double</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    */
   public native void setDouble(int index, double value);
  
   /**
    * This method sets the specified parameter from the given Java <code>String</code> value.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter. DO NOT SURROUND IT WITH '!.
    * @throws DriverException If the driver is closed or the table was dropped.
    * @throws OutOfMemoryError If a memory allocation fails.
    */
   public native void setString(int index, String value) throws DriverException, OutOfMemoryError;

   /**
    * This method sets the specified parameter from the given array of bytes as a blob.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param value The value of the parameter.
    * @throws SQLParseException If the parameter to be set is in the where clause.
    * @throws DriverException If the driver is closed or the table was dropped.
    */
   public native void setBlob(int index, byte[] value) throws SQLParseException, DriverException;

   /**
    * This method sets the specified parameter from the given Java <code>Date</code> value formated as "YYYY/MM/DD" <br>
    * <b>IMPORTANT</b>: The constructor <code>new Date(string_date)</code> must be used with care. Some devices can construct different dates, 
    * according to the device's date format. For example, the constructor <code>new Date("12/09/2006")</code>, depending on the device's date format,
    * can generate a date like "12 of September of 2006" or "09 of December of 2006". To avoid this, use the constructor
    * <code>new Date(string_date, totalcross.sys.Settings.DATE_XXX)</code> instead, where <code>totalcross.sys.Settings.DATE_XXX</code> is a date 
    * format parameter that must be one of the <code>totalcross.sys.Settings.DATE_XXX</code> constants.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param date The value of the parameter.
    * @throws DriverException If the driver is closed or the table was dropped.
    * @throws OutOfMemoryError If a memory allocation fails.
    */
   public native void setDate(int index, Date date) throws DriverException, OutOfMemoryError;

   /**
    * This method sets the specified parameter from the given Java <code>DateTime</code> value formated as "YYYY/MM/DD HH:MM:SS:ZZZ". <br>
    * <b>IMPORTANT</b>: The constructor <code>new Date(string_date)</code> must be used with care. Some devices can construct different dates, 
    * according to the device's date format. For example, the constructor <code>new Date("12/09/2006")</code>, depending on the device's date format,
    * can generate a date like "12 of September of 2006" or "09 of December of 2006". To avoid this, use the constructor 
    * <code>new Date(string_date, totalcross.sys.Settings.DATE_XXX)</code> instead, where <code>totalcross.sys.Settings.DATE_XXX</code> is a date 
    * format parameter that must be one of the <code>totalcross.sys.Settings.DATE_XXX</code> constants.
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param date The value of the parameter. 
    */
   public native void setDateTime(int index, Date date);

   /**
    * Formats the <code>Time</code> t into a string "YYYY/MM/DD HH:MM:SS:ZZZ"
    *
    * @param index The index of the parameter value to be set, starting from 0.
    * @param time The value of the parameter.
    * @throws DriverException If the driver is closed or the table was dropped.
    * @throws OutOfMemoryError If a memory allocation fails.
    */
   public native void setDateTime(int index, Time time) throws DriverException, OutOfMemoryError;

   // juliana@223_3: PreparedStatement.setNull() now works for blobs.
   /**
    * Sets null in a given field. This can be used to set any column type as null. It must be just remembered that a parameter in a where clause 
    * can't be set to null. 
    *
    * @param index The index of the parameter value to be set as null, starting from 0.
    * @throws DriverException If the driver is closed or the table was dropped.
    * @throws SQLParseException If the parameter to be set as null is in the where clause.
    */
   public native void setNull(int index) throws DriverException, SQLParseException;

   /**
    * This method clears all of the input parameters that have been set on this statement.
    * 
    * @throws DriverException If the driver is closed or the table was dropped.
    */
   public native void clearParameters() throws DriverException;

   /**
    * Returns the sql used in this statement. If logging is disabled, returns the sql without the arguments. If logging is enabled, returns the real 
    * sql, filled with the arguments.
    *
    * @returns the sql used in this statement.
    */
   public native String toString();
   
   /**
    * Finalizes the <code>PreparedStatement</code> object.
    */
   protected void finalize()
   {
      psClose();
   }

   /**
    * Finalizes the <code>PreparedStatement</code> object.
    */
   private native void psClose();
}
