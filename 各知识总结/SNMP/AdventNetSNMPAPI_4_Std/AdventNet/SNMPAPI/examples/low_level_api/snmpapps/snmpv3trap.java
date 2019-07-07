/* $Id: snmpv3trap.src,v 1.4.2.6 2006/07/25 17:36:44 rravikumar Exp $ */
/*
 * @(#)snmpv3trap.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to send a
 * v3 Trap message using com.adventnet.snmp.snmp2 package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv3trap [-d] [-p port] [-e engineID(0x....)] [-a auth_protocol] [-w auth_password] [-s priv_password] [-i context_id] userName host TimeTicksvalue OIDvalue [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER | COUNTER64 | UNSIGNED32} value] ...
 * e.g.
 * java snmpv3trap -e 0x000012141516171819202121 -a MD5 -w initial2Pass -i initial initial2 10.3.2.120 16352 .1.3.6.1.4.1.2162.1000.2 .1.3.6.1.4.1.2162.1001.21.0 STRING TrapTest
 *
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 *
 * Options:
 * [-d]                  - Debug output. By default off.
 * [-p] <port>           - remote port no. By default 162.
 * [-e] <engineID>       - Engine ID.
 * [-a] <autProtocol>    - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword>   - The authentication password.
 * [-s] <privPassword>   - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>    - The contextName to be used for the v3 pdu.
 * [-i] <contextID>      - The contextID to be used for the v3 pdu.
 * <username>            - The v3 principal/userName. Mandatory.
 * <timeticks> Mandatory - the value of object sysUpTime when the event occurred
 * <OID-value> Mandatory - Object Identifier  
 * <host>      Mandatory - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <OID>       Mandatory - Object Identifier.
 * <value>     Mandatory - The object instance value to be set .
 */ 

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;

public class snmpv3trap {

  private static final int DEBUG = 0;
  private static final int PORT = 1;
  private static final int AUTH_PROTOCOL = 2;
  private static final int AUTH_PASSWORD = 3;
  private static final int PRIV_PASSWORD = 4;
  private static final int CONTEXT_NAME = 5;
  private static final int CONTEXT_ID = 6;
  private static final int ENGINEID = 7;
  static final int USM_SECURITY_MODEL = 3;
  private static final int PRIV_PROTOCOL=9;


  public static void main(String args[]) {

     // Take care of getting options
    String usage = "snmpv3trap [-d] [-p port][-g agent-address][-e engineID(0x....)] [-a auth_protocol] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID][-pp privProtocol(AES/DES)] \nuserName host TimeTicksvalue OIDvalue [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER | COUNTER64 | UNSIGNED32} value] ...";

    String options[] = { "-d", "-p", "-a", "-w", "-s", "-n", "-i", "-e" ,"-g", "-pp"};
    String values[] = { "None", null, null, null, null, null, null, null,null, null };
   
    String id = new String(""); 
    String userName = new String("");
    int authProtocol = USMUserEntry.NO_AUTH;
    int privProtocol=USMUserEntry.NO_PRIV;
    String authPassword = new String ("");
    String privPassword = new String ("");
    String contextName = new String ("");
    String contextID = new String ("");

    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length<4) opt.usage_error();
    
    // Start SNMP API
    SnmpAPI api;
    api = new SnmpAPI();
    if (values[0].equals("Set")) api.setDebug( true );
    
    SnmpPDU pdu = new SnmpPDU(); 
    Snmp3Message msg = (Snmp3Message)(pdu.getMsg());
    pdu.setCommand( api.TRP2_REQ_MSG );
                       
    // Open session
    SnmpSession session = new SnmpSession(api);

    // set remoteHost
    UDPProtocolOptions ses_opt = new UDPProtocolOptions(opt.remArgs[1]);

    // set version
    session.setVersion( SnmpAPI.SNMP_VERSION_3 ) ;
    
    try {        
        if(values[PORT] != null)
            ses_opt.setRemotePort( Integer.parseInt(values[PORT]) );
        else
            ses_opt.setRemotePort(162);

        if (values[ENGINEID]!=null) {
            id =  values[ENGINEID];
            if(id.startsWith("0x") || id.startsWith("0X"))
                id = new String(gethexValue(values[ENGINEID]));
        }
    }
    catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Arg");
    }
    catch (StringIndexOutOfBoundsException sie){
        System.err.println("Invalid engineID. Please specify proper" +
                                        " hex value. Exception = " + sie);
        opt.usage_error();
    }

    session.setProtocolOptions(ses_opt);

    userName = opt.remArgs[0];
                
    if ((values[AUTH_PROTOCOL] != null) && (values[AUTH_PASSWORD] != null)) {
        if(values[AUTH_PROTOCOL].equals("SHA"))
            authProtocol = USMUserEntry.SHA_AUTH;
        else 
            authProtocol = USMUserEntry.MD5_AUTH;                   
        if(authProtocol==USMUserEntry.NO_AUTH){
            System.err.println("Enter authentication protocol");
            opt.usage_error();
        }
                    
        authPassword = values[AUTH_PASSWORD];
          if (values[PRIV_PASSWORD] != null) 
	{
              privPassword = values[PRIV_PASSWORD];
	    if(values[PRIV_PROTOCOL] !=null)
	    {
		  if(values[PRIV_PROTOCOL].equals("DES"))
		  {  
		    
		     privProtocol=USMUserEntry.CBC_DES;
		  }
		  else if(values[PRIV_PROTOCOL].equals("AES"))
		  {  
		    
		     privProtocol=USMUserEntry.CFB_AES_128;
		  }
		  else
		  {
			  System.out.println(" Invalid privProtocol ");
			   opt.usage_error();
		  }
	
	    }
	    else
	    {
		    System.out.println(" Please specify the privProtocol value ");
                     opt.usage_error();
		    
	    }	    
		
	}		              
    }
    else if ((values[AUTH_PROTOCOL] != null) 
                || (values[AUTH_PASSWORD] != null) 
                || (values[PRIV_PASSWORD] != null)) {
        opt.usage_error();
    }
    if (values[CONTEXT_NAME] != null)
        contextName = values[CONTEXT_NAME];
    if (values[CONTEXT_ID] != null) 
        contextID = values[CONTEXT_ID];

    createUSMTable(userName.getBytes(), id.getBytes(), authProtocol,
                                    authPassword, privPassword, api,privProtocol);
    pdu.setUserName(userName.getBytes());

    // Build trap request PDU    
    // Adding the sysUpTime variable binding 
    SnmpOID oid = new SnmpOID(".1.3.6.1.2.1.1.3.0");
    if (oid.toValue() == null) 
        System.err.println("Invalid OID argument: .1.3.6.1.2.1.1.3.0");
    else {
        SnmpVar var = null ; 
        try {
            var = SnmpVar.createVariable(opt.remArgs[2], SnmpAPI.TIMETICKS);
        }
        catch (SnmpException e) {
            System.err.println("Cannot create variable: " + oid 
                                +" with value: "+opt.remArgs[1]);
        }
        SnmpVarBind varbind = new SnmpVarBind(oid, var);
        pdu.addVariableBinding(varbind);
        
    }        

    // Adding the snmpTrapOID variable binding
     oid = new SnmpOID(".1.3.6.1.6.3.1.1.4.1.0");
    if (oid.toValue() == null) System.err.println("Invalid OID argument: "
                                                + ".1.3.6.1.6.3.1.1.4.1.0");
    else {
        SnmpVar var = null ;
        try {
            var = SnmpVar.createVariable(opt.remArgs[3], SnmpAPI.OBJID);
        }
        catch (SnmpException e) {
            System.err.println("Cannot create variable: " + oid 
                                +" with value: "+opt.remArgs[2]);
        }
        SnmpVarBind varbind = new SnmpVarBind(oid, var);
        pdu.addVariableBinding(varbind);
        
    }    
    String agentAddress="";
    int otherVarBinds=0;

    for (int i=4;i<opt.remArgs.length;) { // add Variable Bindings

      if (opt.remArgs.length < i+3) opt.usage_error(); //need "{OID type value}"
     
        oid = new SnmpOID(opt.remArgs[i++]);

      if (oid.toValue() == null) 
        System.err.println("Invalid OID argument: " + opt.remArgs[i]);
      else 
        addVarBind(pdu, oid, opt.remArgs[i++], opt.remArgs[i++]);

    } // end of add variable bindings
   

    if(values[8]!=null)
    {
        oid = new SnmpOID(".1.3.6.1.6.3.18.1.4");
        addVarBind(pdu, oid,"STRING", values[8]);
    }


    try {
        // Opening session
        session.open();
        // Send PDU
         session.send(pdu);
    
    } 
    catch (SnmpException e) {

      System.err.println("Sending PDU"+e.getMessage());

    }
    // close session    
    session.close();
    // stop api thread
    api.close();

    System.exit(0);

  }


/** adds the varbind  with specified oid, type and value to the pdu */
    static void addVarBind(SnmpPDU pdu, SnmpOID oid, String type, String value)
    {        
        byte dataType ;
        if (type.equals("INTEGER")) {
        dataType = SnmpAPI.INTEGER;
        } else if (type.equals("STRING")) {
        dataType = SnmpAPI.STRING;
        } else if (type.equals("GAUGE")) {
        dataType = SnmpAPI.GAUGE;
        } else if (type.equals("TIMETICKS")) {
        dataType = SnmpAPI.TIMETICKS;
        } else if (type.equals("OPAQUE")) {
        dataType = SnmpAPI.OPAQUE;
        } else if (type.equals("IPADDRESS")) {
        dataType = SnmpAPI.IPADDRESS;
        } else if (type.equals("COUNTER")) {
        dataType = SnmpAPI.COUNTER;
        } else if (type.equals("OID")) { 
        dataType = SnmpAPI.OBJID;
        }else if (type.equals("COUNTER64")) {
        dataType = SnmpAPI.COUNTER64;
        }
         
        else { // unknown type
        System.err.println("Invalid variable type: " + type);
        return;
        }
        
        SnmpVar var = null;
        try {
        var = SnmpVar.createVariable( value, dataType );
        }
        catch(SnmpException e){
        System.err.println("Cannot create variable: " + oid 
                            + " with value: " + value);
        return;
        }
        SnmpVarBind varbind = new SnmpVarBind(oid, var);
        pdu.addVariableBinding(varbind);
        
    }
  
  
    private static byte[] gethexValue(String value)
    {
        byte temp;
        byte[] Key=new byte[value.length()/2 - 1];
        String ss,str;

        ss = value.substring(2);
        for(int i = 0; i < ss.length(); i+=2)
        {
            str = ss.substring(i,i+2);
            temp = (byte)Integer.parseInt(str,16);
            Key[i/2] = temp;
        }
        return Key;    
    }

    public static void createUSMTable(byte[] name, byte[] engineID, 
                                    int authProtocol, String authPassword,
                                    String privPassword, SnmpAPI api, int privProtocol)
    {
    byte level = 0;
    
    USMUserTable uut = (USMUserTable)api.getSecurityProvider().
                                            getTable(USM_SECURITY_MODEL);
    USMUserEntry entry = new USMUserEntry(name, engineID);
    entry.setAuthProtocol(authProtocol);

    if ((authProtocol != USMUserEntry.NO_AUTH) && (authPassword != null))
    {
        byte[] authKey = USMUtils.password_to_key(authProtocol, 
                                            authPassword.getBytes(), 
                                            authPassword.getBytes().length,
                                            engineID);
            entry.setAuthKey(authKey);
            level = 1;
            
            if ((privPassword != null)&&(privPassword.length()>0))
            {
                byte[] tempKey = USMUtils.password_to_key(authProtocol, 
                                            privPassword.getBytes(),
                                            privPassword.getBytes().length,
                                            engineID);
               	entry.setPrivProtocol(privProtocol);
                byte privKey[]=new byte[16];
                System.arraycopy(tempKey,0,privKey,0,16);
                entry.setPrivKey(privKey);
                level |= 2;
            }
    }
    
    entry.setSecurityLevel(level);
    uut.addEntry(entry);

    api.setSnmpEngineID(engineID);
    }
    
}
