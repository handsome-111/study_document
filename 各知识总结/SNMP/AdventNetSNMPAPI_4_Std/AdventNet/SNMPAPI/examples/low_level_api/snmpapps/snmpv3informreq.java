/* $Id: snmpv3informreq.src,v 1.4.2.4 2004/10/18 10:34:51 amaresh Exp $ */
/*
 * @(#)snmpv3informreq.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to send a
 * v3 Inform request message using com.adventnet.snmp.snmp2 package of 
 * AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv3informreq [-d] [-p port] [-u user] 
 * [-a auth_protocol] [-w auth_password] [-s priv_password] [-i context_id] 
 * host TimeTicksvalue OIDvalue [OID {INTEGER | STRING | GAUGE | TIMETICKS | 
 * OPAQUE | IPADDRESS | COUNTER | COUNTER64 | UNSIGNED32} value] ...
 * e.g.
 * java snmpv3informreq -u intial2 -a MD5 -w initial2Pass -i initial 
 * 10.3.2.120 16352 .1.3.6.1.4.1.2162.1000.2 .1.3.6.1.4.1.2162.1001.21.0 STRING 
 * InformReqTest
 *
 * If the oid is not starting with a dot(.) it will be prefixed by .1.3.6.1.2.1.
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 *
 * Options:
 * [-d]                  - Debug output. By default off.
 * [-p] <port>           - remote port no. By default 162.
 * [-u] <username>       - The v3 principal/userName
 * [-a] <autProtocol>    - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-pp] <privProtocol>  - The privProtocol(DES/AES).
 * [-w] <authPassword>   - The authentication password.
 * [-s] <privPassword>   - The privacy protocol password. Must be accompanied 
 * with auth password and authProtocol fields.
 * [-n] <contextName>    - The contextName to be used for the v3 pdu.
 * [-i] <contextID>      - The contextID to be used for the v3 pdu.
 * <timeticks> Mandatory - the value of object sysUpTime when the event occurred
 * <OID-value> Mandatory - Object Identifier
 * <host>      Mandatory - The RemoteHost.Format (string without double
 * quotes/IpAddress).
 * <OID>       Mandatory - Object Identifier.
 * <value>     Mandatory - The object instance value to be set.
 *
 * Note: Here the Engine Id need not be specified since this application will do
 * a discovery initially.
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;

public class snmpv3informreq {

  public static void main(String args[]) {

    // Take care of getting options
    String usage = "snmpv3informreq [-d] [-p port] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] [-pp privProtocol(AES/DES)] host TimeTicksvalue OIDvalue [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER | COUNTER64 | UNSIGNED32} value] ...";

		String options[] =
		{ "-d", "-c",  "-wc", "-p", "-r", "-t", "-m",
			"-v", "-u", "-a", "-w", "-s", "-n", "-i",
			"-DB_driver", "-DB_url", "-DB_username", "-DB_password", "-pp"
		};

		String values[] =
		{
			"None", null, null, null, null, null, "None",
		   null, null, null, null, null, null, null,
			 null, null, null, null, null
		};

    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length<3) 
    {
        opt.usage_error();
    }

    //Just set the SNMP version so that the generic SetValues works properly
    values[7]="v3";
    
    // Start SNMP API
    SnmpAPI api = new SnmpAPI();
    
        // Check if the debug flag is set or not.
    if (values[0].equals("Set")) 
    {
        api.setDebug( true );
    }

    // Create an SNMP PDU. 
    SnmpPDU pdu = new SnmpPDU(); 

    // Build v3 SNMP INFORM_REQ message
    Snmp3Message msg = (Snmp3Message)(pdu.getMsg());
    pdu.setCommand (api.INFORM_REQ_MSG);
                       
    // Create an SNMP session. 
    SnmpSession session = new SnmpSession(api);

    // Set the values
    SetValues setVal = new SetValues(session, values);
    if(setVal.usage_error) 
    {
        opt.usage_error(); 
    }

        // set remote Host 
        UDPProtocolOptions ses_opt = (UDPProtocolOptions)session.getProtocolOptions();
        if(ses_opt == null)
        {
            ses_opt = new UDPProtocolOptions(opt.remArgs[0]);
        }
        else
        {
            ses_opt.setRemoteHost(opt.remArgs[0]);
        }

    // Set the SNMP version
    session.setVersion(SnmpAPI.SNMP_VERSION_3) ;
    
    // Set default remote port to 162 if port is not specified by the user 
    if (values[3] == null) 
    {
      ses_opt.setRemotePort(162);
    }
    else
    {
        try
        {
            ses_opt.setRemotePort(Integer.parseInt(values[3]));
        }
        catch(Exception exp)
        {
            System.out.println("Invalid port: " + values[3]);
            System.exit(1);
        }
    }

    session.setProtocolOptions(ses_opt);

        // Set the user name
    pdu.setUserName(setVal.userName.getBytes());

    // Build the INFORM REQUEST PDU    
    // Adding the sysUpTime variable binding 
    SnmpOID oid = new SnmpOID(".1.3.6.1.2.1.1.3.0");

    if (oid.toValue() == null) 
    {
      System.err.println("Invalid OID argument: .1.3.6.1.2.1.1.3.0");
    }
    else 
    {
      SnmpVar var = null ; 

      try {
        var = SnmpVar.createVariable(opt.remArgs[1], SnmpAPI.TIMETICKS);
      }
      catch (SnmpException e)
      {
        System.err.println("Cannot create variable: " + oid 
                                    +" with value: "+opt.remArgs[1]);
      }

      SnmpVarBind varbind = new SnmpVarBind(oid, var);
      pdu.addVariableBinding(varbind);
    }        

    // Adding the snmpTrapOID variable binding
    oid = new SnmpOID(".1.3.6.1.6.3.1.1.4.1.0");

    if (oid.toValue() == null) 
    {
            System.err.println("Invalid OID argument: .1.3.6.1.6.3.1.1.4.1.0");
    }
    else 
    {
      SnmpVar var = null ;

      try {
        var = SnmpVar.createVariable(opt.remArgs[2], SnmpAPI.OBJID);
      }
      catch (SnmpException e) {
        System.err.println("Cannot create variable: " + oid 
                                    +" with value: "+opt.remArgs[2]);
      }

      SnmpVarBind varbind = new SnmpVarBind(oid, var);
      pdu.addVariableBinding(varbind);
    }    
    
        // Add Variable Bindings
    for (int i=3;i<opt.remArgs.length;) 
    { 
      if (opt.remArgs.length < i+3) 
      {
        opt.usage_error(); //need "{OID type value}"
      }
     
      oid = new SnmpOID(opt.remArgs[i++]);

      if (oid.toValue() == null)
      {
        System.err.println("Invalid OID argument: " + opt.remArgs[i]);
      }
      else
      {
        addVarBind(pdu, oid, opt.remArgs[i++], opt.remArgs[i++]);
      }
    }

    try {
      // Open the SNMP Session
      session.open();

            // do a dicovery
        try
        {
            USMUtils.init_v3_parameters(
                setVal.userName,
								null,
                setVal.authProtocol,
                setVal.authPassword,
                setVal.privPassword,
                ses_opt,
                session,
								false,
								setVal.privProtocol);
        }
        catch(Exception exp)
        {
            System.out.println(exp.getMessage());
            System.exit(1);
        }

      // Send the INFORM_REQ PDU using synchronous send
     SnmpPDU res_pdu = session.syncSend(pdu);

            // Process PDU on receiving response
            if (res_pdu == null)    
            {
                // Request timed out.
                System.out.println ("Request timed out.\n");
                System.exit(1);
            }

            // Response received
        // Check for error in response
        if (res_pdu.getErrstat() != 0)
        {
            System.err.println(res_pdu.getError());
        }
        else    
        {
            // print the response pdu varbinds
            System.out.println(res_pdu.printVarBinds());
        }
    } 
    catch (SnmpException e) {

      System.err.println("Sending PDU"+e.getMessage());
    }

        // Close the session
        session.close();

        // Close the api thread.
        api.close();

        // Exit the application.
        System.exit(0);
  }

    /* 
     * Adds the varbind  with specified oid, type and value to the pdu 
     */
  static void addVarBind(SnmpPDU pdu, SnmpOID oid, String type, String value)
  {
    byte dataType ;

    if (type.equals("INTEGER")) 
    {
      dataType = SnmpAPI.INTEGER;
    } 
    else if (type.equals("STRING")) 
    {
      dataType = SnmpAPI.STRING;
    } 
    else if (type.equals("GAUGE")) 
    {
      dataType = SnmpAPI.GAUGE;
    } 
    else if (type.equals("TIMETICKS")) 
    {
      dataType = SnmpAPI.TIMETICKS;
    } 
    else if (type.equals("OPAQUE")) 
    {
      dataType = SnmpAPI.OPAQUE;
    } 
    else if (type.equals("IPADDRESS")) 
    {
      dataType = SnmpAPI.IPADDRESS;
    } 
    else if (type.equals("COUNTER")) 
    {
      dataType = SnmpAPI.COUNTER;
    } 
    else if (type.equals("OID")) 
    {
      dataType = SnmpAPI.OBJID;
    } 
    else 
    { 
            // Invalid variable type.
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
}
