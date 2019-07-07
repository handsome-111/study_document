/* $Id: snmpv2cinformreqd.src,v 1.4.2.2 2004/03/17 10:05:30 vivek Exp $ */
/*
 * @(#)snmpv2cinformreqd.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program for receiving INFORM REQUESTs using the
 * com.adventnet.snmp.snmp2 package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv2cinformreqd [options]
 *
 * java snmpv2cinformreqd [-d] [-p port][-c community]
 * e.g. 
 * java snmpv2cinformreqd -p 162 -c public 
 *
 * Options:
 * [-d]                - Debug output. By default off.
 * [-p] <port>         - remote port no. By default 162.
 * [-c] <community>    - community String. By default "public".               
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;

public class snmpv2cinformreqd implements SnmpClient {

  static SnmpAPI api;
  public static void main(String args[]) {
       
    // Take care of getting options
    String usage = "snmpv2cinformreqd [-d] [-p port][-c community]";
    String options[] = { "-d", "-p", "-c"};
    String values[] = { "None", null, null};

    ParseOptions opt = new ParseOptions(args,options,values, usage);

    // Start SNMP API
    api = new SnmpAPI();

    // Enable the debug mode.
    api.setDebug(true);

    if (values[0].equals("Set")) 
    {
        api.setDebug( true );
    }
      
    if (opt.remArgs.length>0) opt.usage_error();

    // Open session 
    SnmpSession session = new SnmpSession(api);
    session.addSnmpClient(new snmpv2cinformreqd());

    UDPProtocolOptions ses_opt = new UDPProtocolOptions();
    // set local port
    try {
        if (values[1] != null)
        {
            ses_opt.setLocalPort( Integer.parseInt(values[1]) );        
        }
        else
        {
            ses_opt.setLocalPort(162);
        }
    }
    catch (NumberFormatException ex) {
      System.err.println("Invalid Integer Arg");
    }

    session.setProtocolOptions(ses_opt);
      
    // set community
    if(values[2] != null)
    {
      session.setCommunity(values[2]);
    }    

    // Open the session
    try { 
        session.open();
        System.out.println ("Waiting to receive SNMP Inform requests\n");
    }
    catch (SnmpException e) {
      System.err.println(e);
      System.exit(1);
    }
  }
    
  public boolean authenticate(SnmpPDU pdu, String community){
      return (pdu.getCommunity().equals(community));
  }
  
    /*
     * Callback method that is invoked on receiving an SNMP Inform request message
     */
  public boolean callback(SnmpSession session,SnmpPDU pdu, int requestID) {

    // Validate SNMP command type
    if (pdu.getCommand() == api.INFORM_REQ_MSG) 
    {
      System.out.println("inform request received from: " + 
                        pdu.getProtocolOptions().getSessionId() +
                        ", community: " + pdu.getCommunity());
      System.out.println("VARBINDS:");

      // print varbinds
      for (Enumeration e = pdu.getVariableBindings().elements();
                                                e.hasMoreElements();)
      {
        System.out.println(((SnmpVarBind) e.nextElement()).toTagString());
      }
    }
    else
    {
      System.err.println("Unexpected SNMP message recieved.");
    }

    System.out.println(""); 

    return true;
  }
 
  public void debugPrint(String debugOutput){

    System.out.println(debugOutput);
    return;    
  }
}
