/* $Id: snmpv1trap.java,v 1.4.2.4 2004/03/17 10:05:30 vivek Exp $ */
/*
 * @(#)snmpv1trap.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */
/**
 * This is an example program to explain how to write an application to send a
 * v1 Trap message using com.adventnet.snmp.snmp2 package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv1trap hostname enterprise-oid agent-addr generic-trap specific-trap 
 *      timeticks [OID type value] ...
 *
 * java snmpv1trap [-d] [-c community] [-p port] host enterprise agent-addr generic-trap specific-trap timeticks [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER} value] ...
 * e.g. 
 * java snmpv1trap -p 162 -c public adventnet 1.2.0 adventnet 0 6 1000 1.5.0 STRING advent
 * 
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 * * Options: 
 * [-d] option     Debug output. By default off.
 * [-c] option     community String. By default "public".
 * [-p] option     remote port no. By default 162.
 * enterprise      Object Identifier (sysObjectID for generic traps)
 * agent-addr      the IP address of the agent sending the trap
 * generic-trap    generic trap type INTEGER (0..6)
 * specific-trap   specific code INTEGER(0..2147483647)
 * timeticks       the value of object sysUpTime when the event occurred
 * host mandatory  The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * OID  option     Give multiple no. of Object Identifiers with type and value.
 * type            Type of the object
 * value           object-instance value
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;


public class snmpv1trap {

  public static void main(String args[]) {
      SnmpAPI api;
      // Take care of getting options
      String usage = "snmpv1trap [-d] [-c community] [-p port] host enterprise agent-addr generic-trap specific-trap timeticks [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER} value] ..."; 
      String options[] = { "-d", "-c", "-p"};
      String values[] = { "None", null, null};
      
      ParseOptions opt = new ParseOptions(args,options,values, usage);
      if (opt.remArgs.length<6) opt.usage_error();

      // Start SNMP API
      api = new SnmpAPI();
      if (values[0].equals("Set")) api.setDebug( true );

      // Open session
      SnmpSession session = new SnmpSession(api);

      // set community
      if (values[1] != null) session.setCommunity( values[1] );

      UDPProtocolOptions udpOpt = new UDPProtocolOptions(opt.remArgs[0]);
      try
      {
          if (values[2] != null)
          {
              udpOpt.setRemotePort(Integer.parseInt(values[2]));
          }
          else 
          {
              udpOpt.setRemotePort(162);
          }
      }
      catch(Exception ex)
      {
          System.err.println("Invalid port: " + values[2]);
          System.exit(0);
      }

      // Build SNMPv1 Trap PDU
      SnmpPDU pdu = new SnmpPDU();
      pdu.setProtocolOptions(udpOpt);
      pdu.setCommand( api.TRP_REQ_MSG );
    
      // fill in v1 trap PDU fields
      try { 
          // set enterprise OID 
          pdu.setEnterprise(new SnmpOID(opt.remArgs[1]));
          // set agent address
          pdu.setAgentAddress(InetAddress.getByName(opt.remArgs[2]));
          // set generic trap type
          pdu.setTrapType(Integer.parseInt(opt.remArgs[3]));
           // set specific code
          if (opt.remArgs.length>4) 
              pdu.setSpecificType(Integer.parseInt(opt.remArgs[4]));
          // set time-stamp
          if (opt.remArgs.length>5) 
              pdu.setUpTime(Integer.parseInt(opt.remArgs[5]));
      } catch (Exception ex) { 
          System.err.println("error in one or more required fields: "+ex);
          opt.usage_error();
      }

      // add Variable Bindings
      for (int i=6;i<opt.remArgs.length;) { 

          if (opt.remArgs.length < i+3) opt.usage_error(); //need "{OID type value}"
          SnmpOID oid = new SnmpOID(opt.remArgs[i++]);

          if (oid.toValue() == null) {
              System.err.println("Invalid OID argument: " + opt.remArgs[i]);
              System.exit(0);
          }
          else {
              addVarBind(pdu, oid, opt.remArgs[i++], opt.remArgs[i++]);
          }

      } // end of add variable bindings

      try { 
          // open session
          session.open(); 
          // Send PDU
          session.send(pdu); 
      } catch (SnmpException e) {
          System.err.println("Sending PDU"+e.getMessage());
      }

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
