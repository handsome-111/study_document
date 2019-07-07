 /* $Id: trapreceiver.src,v 1.4.4.3 2006/08/30 17:58:43 srikrishnan Exp $ */
 /*
 * @(#)trapreceiver.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the COPYRIGHTS file for more details.
 *
 */

/** 
 *  Run command line trap receiver and print incoming PDUs.  Loads MIBs 
 *  as specified, and converts to/from names for loaded MIB data. 
 *  It also prints loaded trap names and descriptions when the
 *  corresponding traps are received.
 *
 * [-c] <community>    - community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-m] <mibs>           - The mibs to be loaded.
 * [-e] <engineID>       - The V3 engineID
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>  - The snmpv3 contextName to be used.
 * [-i] <contextID>    - The snmpv3 contextID to be used.
 * [-pp] <privProtocol> - The privacy protocol. Must be accompanied with auth,priv password and authProtocol fields.
 */

import com.adventnet.snmp.beans.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;


public class trapreceiver {

    private static int MIBS = 0;
    private static int COMMUNITY = 1;
    private static int PORT = 2;

    private static int USER_NAME = 3;
    private static int ENGID = 4;
    private static int AUTH_PROTOCOL = 5;
    private static int AUTH_PASSWORD = 6;
    private static int PRIV_PASSWORD = 7;
    private static int DEBUG = 8;
    private static int CONTEXT_NAME = 9;
    private static int CONTEXT_ID = 10;
    private static int PRIV_PROTOCOL = 11;

    public static void main(String args[]) {

    // Take care of getting options

    String usage = "trapreceiver [-m MIB_files] [-c community] [-p port] [-u user] [-e engineID(1234.../0x1234...)] [-a authProtocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-d] [-n contextName] [-i contextId] [ -pp priv_protocol(AES/DES) ]";
    String options[] = { "-m" , "-c", "-p", "-u", "-e", "-a", "-w", "-s", "-d", "-n", "-i", "-pp" };
    String values[] = { null, null, null, null, null, null, null, null, "None", null, null, null};

    String userName = null;
    int authProtocol = USMUserEntry.NO_AUTH;
    String authPassword = new String ("");
    String privPassword = new String ("");
    String engineID = null;
    int privProtocol = USMUserEntry.NO_PRIV;
    byte secLevel = 0;

    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length!=0) opt.usage_error();

    // instantiate a receiver object
    SnmpTrapReceiver receiver = new SnmpTrapReceiver();

	 //To load MIBs from compiled file
	 receiver.getMibOperations().setLoadFromCompiledMibs(true);

        if (values[COMMUNITY] != null) receiver.setCommunity( values[COMMUNITY] );
    try {  // set trap port to listen on if specified - else port 162
        
        if (values[PORT] != null) 
        receiver.setPort( Integer.parseInt(values[PORT]) );
        else
            receiver.setPort( 162 );

        if (values[USER_NAME] != null) {
            userName = values[USER_NAME];
            receiver.setPrincipal(userName); 
        }
    
        if (values[ENGID] != null) {
            engineID = values[ENGID];
			if(engineID.startsWith("0x") || engineID.startsWith("0X"))
				engineID = new String(gethexValue(values[ENGID]));
        }
    
        if (values[AUTH_PROTOCOL] != null) {
        	if(engineID == null)	{
        		System.out.println("EngineID is missing");        	
        		opt.usage_error();
        	}         
		if ( values[AUTH_PROTOCOL].equals("SHA"))
		    authProtocol = USMUserEntry.SHA_AUTH;
		else if ( values[AUTH_PROTOCOL].equals("MD5"))
		    authProtocol = USMUserEntry.MD5_AUTH;
		else
		    authProtocol = USMUserEntry.NO_AUTH;
		receiver.setAuthProtocol(authProtocol);
			receiver.setTrapAuthEnable(true);
		secLevel |= 0x01;
        }

        if (values[AUTH_PASSWORD] != null) {
        	if(engineID == null)	{
        		System.out.println("EngineID is missing");        	
        		opt.usage_error();
        	}         
		if (secLevel == 0x01) {
		    authPassword = values[AUTH_PASSWORD];
		    receiver.setAuthPassword(authPassword);
		}
		else
		    opt.usage_error();
        }

        if(values[PRIV_PASSWORD] != null) {
        	if(engineID == null)	{
        		System.out.println("EngineID is missing");        	
        		opt.usage_error();
        	}         
		if (secLevel == 0x01)
		{
		    privPassword = values[PRIV_PASSWORD];
      if(values[PRIV_PROTOCOL] != null)
      {
              if(values[PRIV_PROTOCOL].equals("AES"))
              {
                      privProtocol = USMUserEntry.CFB_AES_128;
              }
              else if(values[PRIV_PROTOCOL].equals("DES"))
              {
                      privProtocol = USMUserEntry.CBC_DES;
              }
              else
              {
                      System.out.println(" Invalid PrivProtocol "+values[PRIV_PROTOCOL]);
                      opt.usage_error();
              }
      }

		    receiver.setPrivPassword(privPassword);
      receiver.setPrivProtocol(privProtocol);

      if(values[CONTEXT_NAME]!= null)
      receiver.setContextName(values[CONTEXT_NAME]);
      if(values[CONTEXT_ID]!= null)
      receiver.setContextID((values[CONTEXT_ID]).getBytes());

		    secLevel |= 0x02;
		}
		else
		    opt.usage_error();
        }
         
    } catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Arg");
    }

	if(values[DEBUG].equals("Set"))
		receiver.setDebug(true);
    
    if(userName != null) 
        receiver.createUserEntry(engineID.getBytes(),secLevel);
        
    if (values[MIBS] != null) try { // load MIB files
        System.err.println("Loading MIBs: "+values[MIBS]);
        receiver.loadMibs(values[MIBS]);
        System.err.println("Done.");
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
    }

    // we need to instantiate a trap listener to listen for trap events
    TrapListener listener = new TrapListener() {

        // This method is called when trap is received by SnmpTrapReceiver
        public void receivedTrap(TrapEvent trap) {
        System.out.println("Got a trap from: "+trap.getRemoteHost());
            // print PDU details
        System.out.println( ((SnmpTrapReceiver)trap.getSource())
            .getMibOperations().toString(trap.getTrapPDU()) );
		if( trap.getTrapPDU().getCommand() == SnmpAPI.TRP_REQ_MSG)
		{
        com.adventnet.snmp.mibs.MibTrap trapDefn = // get trap defn
            trap.getTrapDefinition();

        if (trapDefn != null)  // print name and description
            System.out.println("Trap Name: "+trapDefn.getName()+
                       "\nDescr: "+trapDefn.getDescription());
         }
         else if( trap.getTrapPDU().getCommand() == SnmpAPI.TRP2_REQ_MSG)
		{
		
		com.adventnet.snmp.mibs.MibNode notification = trap.getNotificationDefinition();
		if(notification != null)
			System.out.println("Notification Name: "+notification.getLabel()+
						   "\nObjects: "+ notification.getObjects()+
						   "\nStatus: "+ notification.getStatus()+
						   "\nDescr: "+notification.getDescription()+
						   "\nParent: "+ notification.getParent());
		
        }
        }
    };
 
    receiver.addTrapListener(listener);    
    
    System.out.println("Trap Receiver started at port "+receiver.getPort());
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

}
