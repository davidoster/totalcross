package tc.tools;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import tc.tools.deployer.*;

import totalcross.sys.*;

public final class RegisterSDK
{
   public class SDKRegistrationException extends RuntimeException {SDKRegistrationException(String s) {super(s);}}
   
   private static final String MAGIC = "T0T@LCR0$$";
   private static final int DATE_MASK = 0xBADCFE;

   private static final int INVALID_DATE             = -1000;
   private static final int INVALID_REGISTRATION_KEY = -1001;
   private static final int EXPIRED_CONTRACT         = -1002;
   private static final int NO_MORE_DEVELOPERS       = -1003;
   private static final int CONTRACT_NOT_ACTIVE      = -1004;

   private String mac, user, home, key;
   private int today;
   private File flicense;
   
   public RegisterSDK(String newkey) throws Exception
   {
      this(newkey, false);
   }
   
   private RegisterSDK(String key, boolean force) throws Exception
   {
      this.key = key;
      today = Utils.getToday();
      mac = Utils.getMAC();
      user = Settings.userName;
      home = System.getProperty("user.home");
      flicense = new File(home+"/tc_license.dat");
      if (force || !flicense.exists()) 
         updateLicense();
      if (!checkLicense())
      {
         updateLicense();
         if (!checkLicense())
            throw new SDKRegistrationException("The license is expired");
      }         
   }
   
   public static void main(String[] args)
   {
      if (args.length != 1)
         System.out.println("Format: tc.tools.RegisterSDK <activation key>");
      else
         try
         {
            new RegisterSDK(args[0], true);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
   }
   
   private byte[] doCrypto(boolean encrypt, byte[] in) throws Exception 
   {
      char[] keystr = (key+"CAFEBABE").toCharArray();
      byte[] keybytes = new byte[16];
      for (int i = 0, n = keystr.length; i < n; i+=2)
         keybytes[i/2] = (byte)Integer.valueOf(""+keystr[i]+keystr[i+1],16).intValue();
      
      Key secretKey = new SecretKeySpec(keybytes, "AES");
      Cipher cipher = Cipher.getInstance("AES");
      cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, secretKey);
      return cipher.doFinal(in);
   }

   private boolean checkLicense() throws Exception
   {
      // read license file
      InputStream in = new FileInputStream(flicense);
      byte[] fin = new byte[in.available()];
      in.read(fin);
      in.close();
      // use the key passed to launcher
      byte[] bin = doCrypto(false, fin);
      DataInputStream ds = new DataInputStream(new ByteArrayInputStream(bin));
      // skip trash
      int xdataLen = Math.abs(key.hashCode() % 1000);
      ds.skip(xdataLen);
      // checks if the magic equals
      boolean magicEquals = false;
      try
      {
         String magic     = ds.readUTF();
         magicEquals = magic.equals(MAGIC);
      } catch (Exception e) {}
      if (!magicEquals)
         throw new SDKRegistrationException("This license key does not correspond to the stored key!");
      // read the rest of stored data and compare with current values
      HashMap<String,String> kv = Utils.pipeSplit(ds.readUTF());
      String storedMac  = kv.get("mac");
      String storedUser = kv.get("user");
      String storedFolder = kv.get("home");
      int iexp = ds.readInt();
      boolean expired = today >= iexp;
      
      int diffM = storedMac.equals(mac) ? 0 : 1;
      int diffU = storedUser.equals(user) ? 0 : 2;
      int diffF = storedFolder.equals(home) ? 0 : 4;
      int diff = diffM | diffU | diffF;
      
      if (diff != 0)
         throw new SDKRegistrationException("Invalid license file. Error #"+diff);
      
      System.out.println("Next SDK expiration date: "+showDate(iexp));
      return !expired;
   }

   private void updateLicense() throws Exception
   {
      // connect to the registration service and validate the key and mac.
      int expdate = getExpdateFromServer();
      if (expdate <= 0)
         switch (expdate)
         {
            case INVALID_DATE            : throw new SDKRegistrationException("Please update your computer's date.");
            case INVALID_REGISTRATION_KEY: throw new SDKRegistrationException("The registration key is invalid.");
            case EXPIRED_CONTRACT        : throw new SDKRegistrationException("The contract is EXPIRED.");
            case NO_MORE_DEVELOPERS      : throw new SDKRegistrationException("The number of active developers has been reached.");
            case CONTRACT_NOT_ACTIVE     : throw new SDKRegistrationException("This contract is not yet active.");
         }
      else
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
         DataOutputStream ds = new DataOutputStream(baos);
         // generate fake data to put before and at end of important data
         java.util.Random r = new java.util.Random(expdate);
         int xdataLen = Math.abs(key.hashCode() % 1000);
         byte[] xdata = new byte[xdataLen]; r.nextBytes(xdata);
         ds.write(xdata);
         // write important data
         ds.writeUTF(MAGIC);
         ds.writeUTF(Utils.pipeConcat("home",home,"mac",mac,"user",user)); // MUST BE ALPHABETHICAL ORDER!
         ds.writeInt(expdate);
         // write fake data at end
         r.nextBytes(xdata);
         ds.write(xdata);
         ds.close();
         byte[] bytes = baos.toByteArray();
         byte[] cript = doCrypto(true, bytes);
         OutputStream out = new FileOutputStream(flicense);
         out.write(cript);
         out.close();
      }
   }
   
   private int getExpdateFromServer() throws Exception
   {
      // zip data
      ByteArrayOutputStream bas = new ByteArrayOutputStream(256);
      Deflater dd = new Deflater(9, false);
      DeflaterOutputStream def = new DeflaterOutputStream(bas, dd);
      DataOutputStream ds = new DataOutputStream(def);
      ds.writeUTF(key);
      ds.writeUTF(Utils.pipeConcat("home",home,"mac",mac,"user",user)); // MUST BE ALPHABETHICAL ORDER!
      ds.writeInt(today);
      def.finish();
      dd.end();
      byte[] bytes = bas.toByteArray();
      
      // prepare connection
      URLConnection con = new URL("http://www.superwaba.net/SDKRegistrationService/SDKRegistration").openConnection();
      con.setRequestProperty("Request-Method", "POST");
      con.setUseCaches(false);
      con.setDoOutput(true);
      con.setDoInput(true);

      // send data
      OutputStream os = con.getOutputStream();
      os.write(bytes);
      os.close();
      
      // get response - the expiration date or a negative value for error codes
      InputStream in = con.getInputStream();
      DataInputStream dis = new DataInputStream(in);
      int expdate = dis.readInt() ^ DATE_MASK;
      int expcontract = dis.readInt() ^ DATE_MASK;
      if (expcontract != 0)
         System.out.println("INFO: the contract will expire at "+showDate(expcontract));
      dis.close();
      in.close();
      
      return expdate;
   }
   
   private String showDate(int d)
   {
      int day   = d % 100;
      int month = d / 100 % 100;
      int year  = d / 10000;
      return day+" "+totalcross.util.Date.monthNames[month]+", "+year;
   }
}