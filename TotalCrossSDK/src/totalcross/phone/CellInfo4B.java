/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.phone;

import totalcross.io.*;
import totalcross.sys.*;

import net.rim.device.api.system.*;
import net.rim.device.api.system.GPRSInfo.*;

public class CellInfo4B
{
   public static String cellId;
   public static String mnc;
   public static String mcc;
   public static String lac;
   public static int signal;
   static CellInfo instance;
   
   public static void update()
   {
      GPRSCellInfo cellInfo = GPRSInfo.getCellInfo();
      int id = cellInfo.getCellId();
      if (id <= 0)
         cellId = mnc = mcc = lac = null;
      else
      {
         cellId = String.valueOf(cellId);
         lac = String.valueOf(cellInfo.getLAC());
         mcc = String.valueOf(cellInfo.getMCC());
         mnc = String.valueOf(cellInfo.getMNC());
      }
   }
   
   protected void finalize()
   {
   }      

   public static double[] toCoordinates() throws Exception
   {
      double []ret = null;
      if (cellId == null || lac == null)
         return null;
      
      ByteArrayStream bas = new ByteArrayStream(256);
      DataStream ds = new DataStream(bas);

      ds.writeBytes("POST /glm/mmap HTTP/1.1\r\n");
      ds.writeBytes("Content-Type: application/binary\r\n");
      ds.writeBytes("Content-Length: 72\r\n");
      ds.writeBytes("\r\n");
      ds.writeShort(21);
      ds.writeLong(0);
      ds.writeShort(2); ds.writeBytes("en");
      ds.writeShort(7); ds.writeBytes("Android");
      ds.writeShort(3); ds.writeBytes("1.0");
      ds.writeShort(3); ds.writeBytes("Web");
      ds.writeByte(27);
      ds.writeInt(0);
      ds.writeInt(0);
      ds.writeInt(3);
      ds.writeShort(0);
      ds.writeInt(Convert.toInt(cellId));
      ds.writeInt(Convert.toInt(lac));  
      ds.writeInt(0);
      ds.writeInt(0);
      ds.writeInt(0);
      ds.writeInt(0);

      totalcross.net.Socket sock = new totalcross.net.Socket("www.google.com",80,20000);
      sock.writeBytes(bas.toByteArray());

      Vm.sleep(250); // wait for a response
      
      // read the whole answer into a byte array
      byte[] buf = new byte[1024];
      int total = sock.readBytes(buf);
      bas = new ByteArrayStream(buf, total);
      ds = new DataStream(bas);
      
      LineReader lr = new LineReader(bas);
      lr.returnEmptyLines = true;
      String line = lr.readLine(); // first must be HTTP/1.1 200 OK
      if (line != null && line.indexOf(" 200 ") >= 0)
      {
         // find out the length of the data-chunk
         int len = 0;
         while ((line=lr.readLine()) != null) // skip all other header information
         {
            if (line.toLowerCase().startsWith("content-length:"))
            {
               len = Convert.toInt(line.substring(16).trim());
               break;
            }
            else
            if (line.equals(""))
               break;
         }
         if (len > 0)
         {
            // point to the data
            bas.setPos(total-len);
            ds.readShort();
            ds.readByte();
            int code = ds.readInt();
            if (code == 0) 
               ret = new double[]{ds.readInt()/1e6d,ds.readInt()/1e6d};
         }
      }
      
      sock.close();
      
      return ret;
   }
}
