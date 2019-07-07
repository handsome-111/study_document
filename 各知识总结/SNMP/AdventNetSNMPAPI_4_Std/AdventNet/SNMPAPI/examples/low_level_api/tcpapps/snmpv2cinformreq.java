/* $Id: snmpv2cinformreq.src,v 1.4.2.2 2004/03/17 10:08:06 vivek Exp $ */
/*
 * @(#)snmpv2cinformreq.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to send a
 * SNMP v2c INFORM_REQ message using com.adventnet.snmp.snmp2 package of 
 * AdventNetSNMP2 api. The user could run this application by giving any one of
 * the following usage.
 *  
 * java snmpv2cinformreq [options] hostname timeTicks snmpTrapOid-value [OID type 
 * value] ...
 *
 * java snmpv2cinformreq [-d] [-c community] [-p port] host TimeTicksvalue 
 * OIDvalue [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | 
 * COUNTER | COUNTER64 | UNSIGNED32} value] ...
 * e.g. 
 * java snmpv2cinformreq -p 162 -c public adventnet 1000 1.2.0 1.5.0 STRING advent
 * 
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 *
 * Options: 
 * [-d]                  - Debug output. By default off.
 * [-c] <community>      - community String. By default "public".
 * [-p] <port>           - remote port no. By default 162.
 * <host>      Mandatory - The RemoteHost (agent).Format (string without double
 * qoutes/IpAddress).
 * <timeticks> Mandatory - the value of object sysUpTime when the event occurred
 * <OID-value> Mandatory - Object Identifier  
 * <OID>                 - Give multiple no. of Object Identifiers with value.
 * <type>                - Type of the object
 * <value>               - object-instance value
 *
 * Note: Thi uses synchronous send mechanism for sending the PDU.
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;

public class snmpv2cinformreq {

  public static void main(String args[]) {

    SnmpAPI api;
            
    // Take care of getting options
    String usage = "snmpv2cinformreq [-d] [-c community] [-p port] \nhost TimeTicksvalue OIDvalue [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER | COUNTER64 | UNSIGNED32} value] ...";
    String options[] = { "-d", "-c", "-p"};
    String values[] = { "None", null, null};

        // Parse the input for correct format.
    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length<3) 
    {   
        opt.usage_error();
    }
                     
    // Start the SNMP API
    api = new SnmpAPI();

        // Check if the debug flag is set or not.
    if (values[0].equals("Set")) 
    {
        api.setDebug (true);
    }
                
    // Create an SNMP session 
    SnmpSession session = new SnmpSession(api);
    session.setTransportProvider("com.adventnet.snmp.snmp2.TcpTransportImpl");
    
    // Set v2c version
    session.setVersion(SnmpAPI.SNMP_VERSION_2C) ;

    // Set community
    if (values[1] != null) session.setCommunity( values[1] );
        
    // Options that will be used for communication. This can be modified by the user 
    // according to his need.
    ProtocolOptions params = null;
    // Set default remote port to 162 if port is not specified by the user 
    if(values[2] != null)   {
        params = new TcpProtocolOptionsImpl(opt.remArgs[0], Integer.parseInt(values[2]), -1);
    }
    else    {
        params = new TcpProtocolOptionsImpl(opt.remArgs[0], 162, -1);
    }
        session.setProtocolOptions(params);

    // Build v2c SNMP INFORM_REQ PDU
    SnmpPDU pdu = new SnmpPDU();
    pdu.setCommand( api.INFORM_REQ_MSG );
    
    // Add the sysUpTime variable binding
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

    //Add snmpTrapOid variable bindings
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
      catch (SnmpException e) 
      {
        System.err.println("Cannot create variable: " + oid 
                                    +" with value: "+opt.remArgs[2]);
      }
            
      SnmpVarBind varbind = new SnmpVarBind(oid, var);
      pdu.addVariableBinding(varbind);
    }    
    
    // Add the Variable Bindings
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

      // Send the INFORM_REQ PDU using synchronous send
      pdu = session.syncSend(pdu);
    
            // Process PDU on receiving response
            if (pdu == null)    
            {
                // Request timed out.
                System.out.println ("Request timed out.\n");
                        session.close();
                        api.close();        
                System.exit(-1);
            }
    
            // Response received
        // Check for error in response
        if (pdu.getErrstat() != 0)
        {
            System.err.println(pdu.getError());
        }
        else    
        {
            // print the response pdu varbinds
            System.out.println(pdu.printVarBinds());
        }
        
            // Print the response
        System.out.println("Response PDU received from " +((TcpProtocolOptionsImpl)(pdu.getProtocolOptions())).getRemoteHost()+
                   ", community: " + pdu.getCommunity());
            
            // Close the session
            session.close();

            // Close the api thread.
            api.close();

            System.exit(0);
        } 
        catch (SnmpException e) 
        {
        System.err.println("Caught exception in sending PDU :"+e.getMessage());
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
            // Invalid variable type
      System.err.println("Invalid variable type: " + type);
      return;
    }

    SnmpVar var = null;
    try {
      // create variable
      var = SnmpVar.createVariable( value, dataType );
    }
    catch(SnmpException e)
        {
      System.err.println("Cannot create variable: " + oid 
                                    + " with value: " + value);
      return;
    }
        
    // create varbind
    SnmpVarBind varbind = new SnmpVarBind(oid, var);

    // add variable binding
    pdu.addVariableBinding(varbind);
  }
}
