/* $Id: snmpv2ctrap.src,v 1.5.2.2 2004/03/17 10:05:30 vivek Exp $ */
/*
 * @(#)snmpv2ctrap.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to send a
 * v2c Trap message using com.adventnet.snmp.snmp2 package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv2ctrap [options] hostname timeTicks snmpTrapOid-value [OID type value] ...
 *
 * java snmpv2ctrap [-d] [-c community] [-p port] host TimeTicksvalue TrapOIDvalue [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER | COUNTER64 | UNSIGNED32} value] ...
 * e.g. 
 * java snmpv2ctrap -p 162 -c public adventnet 1000 1.2.0 1.5.0 STRING advent
 * 
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 *
 * Options: 
 * [-d]                  - Debug output. By default off.
 * [-c] <community>      - community String. By default "public".
 * [-p] <port>           - remote port no. By default 162.
 * <host>      Mandatory - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <timeticks> Mandatory - the value of object sysUpTime when the event occurred
 * <OID-value> Mandatory - Object Identifier  
 * <OID>                 - Give multiple no. of Object Identifiers with value.
 * <type>                - Type of the object
 * <value>               - object-instance value
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;

public class snmpv2ctrap {

  public static void main(String args[]) {

    SnmpAPI api;
            
    // Take care of getting options
    String usage =
    "snmpv2ctrap [-d] [-c community] [-p port] [-g agent-address]\n" +
    "host TimeTicksvalue TrapOIDvalue [OID {INTEGER | STRING | \n" +
    "GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER | \n" +
    "COUNTER64 | UNSIGNED32} value] ...";
    String options[] = { "-d", "-c", "-p","-g"};
    String values[] = { "None", null, null,null};

    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length<3) opt.usage_error();
                     
    // Start SNMP API
    api = new SnmpAPI();
    if (values[0].equals("Set")) api.setDebug( true );
                
    // Open session 
    SnmpSession session = new SnmpSession(api);
    
    // set v2c version
    session.setVersion( SnmpAPI.SNMP_VERSION_2C ) ;

    // set remote Host
    UDPProtocolOptions ses_opt = new UDPProtocolOptions(opt.remArgs[0]);

    // set community
    if (values[1] != null) session.setCommunity( values[1] );
    // set remote Port
    try {
        if (values[2] != null) 
            ses_opt.setRemotePort( Integer.parseInt(values[2]) );        
        else 
            ses_opt.setRemotePort(162);
    }
    catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Arg");
    }

    session.setProtocolOptions(ses_opt);

    // Build v2 trap PDU
    SnmpPDU pdu = new SnmpPDU();
    pdu.setCommand( api.TRP2_REQ_MSG );
    
    // Add the sysUpTime variable binding
    SnmpOID oid = new SnmpOID(".1.3.6.1.2.1.1.3.0");
    if (oid.toValue() == null) 
        System.err.println("Invalid OID argument: .1.3.6.1.2.1.1.3.0");
    else {
        SnmpVar var = null ; 
        try {
            var = SnmpVar.createVariable(opt.remArgs[1], SnmpAPI.TIMETICKS);
        }
        catch (SnmpException e) {
            System.err.println("Cannot create variable: " +oid+" with value: "+opt.remArgs[1]);
        }
        SnmpVarBind varbind = new SnmpVarBind(oid, var);
        pdu.addVariableBinding(varbind);
        
    }        



    //Add snmpTrapOid variable bindings
     oid = new SnmpOID(".1.3.6.1.6.3.1.1.4.1.0");
    if (oid.toValue() == null) System.err.println("Invalid OID argument: .1.3.6.1.6.3.1.1.4.1.0");
    else {
        SnmpVar var = null ;
        try {
            var = SnmpVar.createVariable(opt.remArgs[2], SnmpAPI.OBJID);
        }
        catch (SnmpException e) {
            System.err.println("Cannot create variable: " +oid+" with value: "+opt.remArgs[2]);
        }
        SnmpVarBind varbind = new SnmpVarBind(oid, var);
        pdu.addVariableBinding(varbind);
        
    }    

    String agentAddress="";
    String community="";
    int otherVarBinds=0;
    
    
    // add Variable Bindings
    for (int i=3;i<opt.remArgs.length;) {

      if (opt.remArgs.length < i+3) opt.usage_error(); //need "{OID type value}"
     
        oid = new SnmpOID(opt.remArgs[i++]);

      if (oid.toValue() == null) 
        System.err.println("Invalid OID argument: " + opt.remArgs[i]);
      else 
        addVarBind(pdu, oid, opt.remArgs[i++], opt.remArgs[i++]);

    } // end of add variable bindings


    if(values[3]!=null)
    {
        oid = new SnmpOID(".1.3.6.1.6.3.18.1.3.0");
        addVarBind(pdu, oid,"STRING" , values[3]);
    }

    if(values[1]!=null)
    {
        oid = new SnmpOID(".1.3.6.1.6.3.18.1.4.0");
        addVarBind(pdu, oid,"STRING", values[1]);
    }


    try {
        // open session
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
      } else { // unknown type
          System.err.println("Invalid variable type: " + type);
          return;
      }

      SnmpVar var = null;
      try {
          // create variable
          var = SnmpVar.createVariable( value, dataType );
      }
      catch(SnmpException e){
          System.err.println("Cannot create variable: " + oid + " with value: " + value);
          return;
      }
      // create varbind
      SnmpVarBind varbind = new SnmpVarBind(oid, var);
      // add variable binding
      pdu.addVariableBinding(varbind);

    }
  
}

