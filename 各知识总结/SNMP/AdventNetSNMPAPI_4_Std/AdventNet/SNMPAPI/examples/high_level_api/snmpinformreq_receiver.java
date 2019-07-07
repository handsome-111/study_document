 /* $Id: snmpinformreq_receiver.src,v 1.4.2.5 2006/10/19 13:32:52 srikrishnan Exp $ */
 /*
 * @(#)snmpinformreq_receiver.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the COPYRIGHTS file for more details.
 *
 */

/** 
 *  Run command line Inform request receiver and print incoming PDUs.  Loads 
 *  MIBs as specified, and converts to/from names for loaded MIB data. 
 *  It also prints loaded trap names and descriptions when the
 *  corresponding Inform Requests are received.
 *
 * [-d] <debug>        - Debug output.By default off.
 * [-c] <community>    - community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-m] <mibs>           - The mibs to be loaded.
 * [-e] <engineID>       - The V3 engineID
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-pp] <privProtocol> - The privacy protocol. Must be accompanied with auth,priv password and authProtocol fields.
 */

import com.adventnet.snmp.beans.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;
//import ParseOptions;


public class snmpinformreq_receiver {

    private static int MIBS = 0;
    private static int COMMUNITY = 1;
    private static int PORT = 2;
    private static int DEBUG =3;
    private static int USER_NAME = 4;
    private static int ENGID = 5;
    private static int AUTH_PROTOCOL = 6;
    private static int AUTH_PASSWORD = 7;
    private static int PRIV_PASSWORD = 8;
    private static int CONTEXT_NAME = 9;
    private static int CONTEXT_ID = 10;
    private static int PRIV_PROTOCOL =11;

    public static void main(String args[]) {

    // Take care of getting options
    String usage = "snmpinformreq_receiver [-m MIB_files] [-c community] [-p port] [-d] [-u user] [-e engineID(1234.../0x1234...)] [-a authProtocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-n contextName] [-i contextId] [ -pp priv_protocol (DES/AES) ] ";
    String options[] = { "-m" , "-c", "-p", "-d", "-u", "-e", "-a", "-w", "-s","-n","-i","-pp" };
    String values[] = { null, null, null, "None", null, null, null, null, null, null, null, null};

    String userName = null;
    int authProtocol = USMUserEntry.NO_AUTH;
    String authPassword = new String ("");
    String privPassword = new String ("");
    String engineID = null;
    int privProtocol = 0;
    byte secLevel = 0;

    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length!=0) opt.usage_error();

    // instantiate a receiver object
    SnmpTrapReceiver receiver = new SnmpTrapReceiver();

     //To load MIBs from compiled file
     receiver.getMibOperations().setLoadFromCompiledMibs(true);


         if(values[DEBUG].equals("Set"))
         receiver.setDebug(true);



        if (values[COMMUNITY] != null) receiver.setCommunity( values[COMMUNITY] );
    try {  // set Inform Request port to listen on if specified - else port 162
        
        if (values[PORT] != null)   {
            int port = -1;
            if((port = Integer.parseInt(values[PORT])) < 0) {
                System.err.println("Invalid port number");
                opt.usage_error();
            }
                receiver.setPort( Integer.parseInt(values[PORT]) );
        }
        else
            receiver.setPort( 162 );
    } catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Arg");
    }

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
        if ( values[AUTH_PROTOCOL].equals("SHA"))
            authProtocol = USMUserEntry.SHA_AUTH;
        else if ( values[AUTH_PROTOCOL].equals("MD5"))
            authProtocol = USMUserEntry.MD5_AUTH;
        else
            authProtocol = USMUserEntry.NO_AUTH;
        receiver.setAuthProtocol(authProtocol);
        secLevel |= 0x01;
        }

        if (values[AUTH_PASSWORD] != null) {
        if (secLevel == 0x01) {
            authPassword = values[AUTH_PASSWORD];
            receiver.setAuthPassword(authPassword);
        }
        else
            opt.usage_error();
        }

        if(values[PRIV_PASSWORD] != null) {
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
            {
            opt.usage_error();
            }
        }
    
    if(userName != null) 
        receiver.createUserEntry(engineID.getBytes(),secLevel);
        
    if (values[MIBS] != null) try { // load MIB files
        System.err.println("Loading MIBs: "+values[MIBS]);
        receiver.loadMibs(values[MIBS]);
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
    }

    

    // we need to instantiate a trap listener to listen for trap events
    TrapListener listener = new TrapListener() {

        // This method is called when Inform Request is received by SnmpTrapReceiver
        public void receivedTrap(TrapEvent trap) {
            // print PDU details
        System.out.println( ((SnmpTrapReceiver)trap.getSource())
            .getMibOperations().toString(trap.getTrapPDU()) );
        if( trap.getTrapPDU().getCommand() == SnmpAPI.TRP_REQ_MSG)
        {
        System.out.println("Received Trap Notification from: "+trap.getRemoteHost());
        com.adventnet.snmp.mibs.MibTrap trapDefn = // get trap defn
            trap.getTrapDefinition();

        if (trapDefn != null)  // print name and description
            System.out.println("Trap Name: "+trapDefn.getName()+
                       "\nDescr: "+trapDefn.getDescription());
         }
         else if( trap.getTrapPDU().getCommand() == SnmpAPI.TRP2_REQ_MSG)
        {
        System.out.println("Received Trap Notification from: "+trap.getRemoteHost());
        
        com.adventnet.snmp.mibs.MibNode notification = trap.getNotificationDefinition();
        if(notification != null)
            System.out.println("Notification Name: "+notification.getLabel()+
                           "\nObjects: "+ notification.getObjects()+
                           "\nStatus: "+ notification.getStatus()+
                           "\nDescr: "+notification.getDescription()+
                           "\nParent: "+ notification.getParent());
        
        }
         else if( trap.getTrapPDU().getCommand() == SnmpAPI.INFORM_REQ_MSG)
                 {
                System.out.println("Received Inform Request from: "+trap.getRemoteHost());
                 }
        }
    };
 
    receiver.addTrapListener(listener);    
    
    System.out.println("Inform Receiver started at port "+receiver.getPort());
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
