/* $Id: snmpv3trapd.src,v 1.4.2.4 2006/06/29 16:55:09 rravikumar Exp $ */
/*
 * @(#)snmpv3trapd.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program for receiving traps using the
 * com.adventnet.snmp.snmp2 package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv3trapd [options]
 *
 * java snmpv3trapd [-d] [-p port][-c community] [-u user] [-e engineID] [-a authProtocol] [-w auth_password] [-s priv_password]
 * e.g. 
 * java snmpv3trapd -p 162 -c public 
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
import com.adventnet.snmp.snmp2.usm.*;

public class snmpv3trapd implements SnmpClient {

    private final static int PORT = 1;
    private final static int COMMUNITY = 2;
    private final static int USER_NAME = 3;
    private final static int ENGID = 4;
    private final static int AUTH_PROTOCOL = 5;
    private final static int AUTH_PASSWORD = 6;
    private final static int PRIV_PASSWORD = 7;
    static final int USM_SECURITY_MODEL = 3;
    private final static int PRIV_PROTOCOL=8;


    static SnmpAPI api;
    public static void main(String args[]) {
        
        System.out.println("Please wait till the snmpv3trapd initializes ...");

        // Take care of getting options
        String usage = "snmpv3trapd [-d] [-p port] [-c community] [-u user] [-e engineID(0x...)] [-a authProtocol] [-w auth_password] [-s priv_password] [-pp privProtocol(AES/DES)]";
        String options[] = { "-d", "-p", "-c", "-u", "-e", "-a", "-w", "-s", "-pp"};
        String values[] = { "None", null, null, null, null, null, null, null,null};

        ParseOptions opt = new ParseOptions(args,options,values, usage);
        String userName = null;
        int authProtocol = USMUserEntry.NO_AUTH;
       	int privProtocol = USMUserEntry.NO_PRIV;
        String authPassword = new String ("");
        String privPassword = new String ("");
        String engineID = null;
        byte secLevel = 0;
                int portNo=162; // Default port number at which receiver listens.

        // Start SNMP API
        api = new SnmpAPI();
        if (values[0].equals("Set")) api.setDebug( true );
        
        if (opt.remArgs.length>0) opt.usage_error();

        // Open session 
        SnmpSession session = new SnmpSession(api);
        session.addSnmpClient(new snmpv3trapd());

        // set local port
        try {
            if (values[PORT] != null) 
                        {
                            portNo = Integer.parseInt(values[PORT]);
                        }

            UDPProtocolOptions ses_opt = new UDPProtocolOptions();
            ses_opt.setLocalPort(portNo);
            session.setProtocolOptions(ses_opt);

            if (values[USER_NAME] != null) {
                userName = values[USER_NAME];
            }
            
            if (values[ENGID] != null) {
                engineID = values[ENGID];
                if(engineID.startsWith("0x") || engineID.startsWith("0X"))
                    engineID = new String(gethexValue(values[ENGID]));
            }
            
            if (values[AUTH_PROTOCOL] != null) {
                if ( values[AUTH_PROTOCOL].equals("SHA")){
                    authProtocol = USMUserEntry.SHA_AUTH;
                    secLevel |= 0x01;
                }
                else if ( values[AUTH_PROTOCOL].equals("MD5")){
                    authProtocol = USMUserEntry.MD5_AUTH;
                    secLevel |= 0x01;
                }
                else
                    authProtocol = USMUserEntry.NO_AUTH;
            }
            
            if (values[AUTH_PASSWORD] != null) {
                if (secLevel == 0x01)
                    authPassword = values[AUTH_PASSWORD];
                else
                    opt.usage_error();
            }
            
            if(values[PRIV_PASSWORD] != null) {
               if(values[PRIV_PROTOCOL] !=null)
	    {
		  if(values[PRIV_PROTOCOL].equals("DES"))
		  {  
		     privPassword = values[PRIV_PASSWORD];
		     privProtocol=USMUserEntry.CBC_DES;
		     secLevel |= 0x02;
		  }
		  else if(values[PRIV_PROTOCOL].equals("AES"))
		  {  
		     privPassword = values[PRIV_PASSWORD];
		     privProtocol=USMUserEntry.CFB_AES_128;
		     secLevel |= 0x02;
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
        catch (NumberFormatException ex) {
            System.err.println("Invalid Integer Arg: " + ex.getMessage());
            System.exit(1);
        }
        catch (StringIndexOutOfBoundsException sie){
            System.err.println("Invalid engineID. Please specify proper" +
                                            " hex value. Exception = " + sie);
            opt.usage_error();
        }

        if(userName != null) {
            // Create a new USMUserEntry for the userName and engineID pair.
            USMUserEntry user = new USMUserEntry(userName.getBytes(),
                                                engineID.getBytes());
            if ((secLevel & 0x01)== 0x01)
            {
                user.setAuthProtocol(authProtocol);
                user.setAuthPassword(authPassword.getBytes());
                // Convert the auth password to key.
                byte[] authKey = 
                    USMUtils.password_to_key(authProtocol, 
                                            authPassword.getBytes(),
                                            authPassword.getBytes().length,
                                            engineID.getBytes());
                user.setAuthKey(authKey);
                if (secLevel == 0x03)
                {
                    user.setPrivProtocol(privProtocol);
                    user.setPrivPassword(privPassword.getBytes());
                    // Convert the priv password to key.
                    byte[] privKey = USMUtils.password_to_key(authProtocol,
                                            privPassword.getBytes(),
                                            privPassword.getBytes().length,
                                            engineID.getBytes());
                    user.setPrivKey(privKey);
                }
            }
            user.setSecurityLevel(secLevel);
            USMUserTable uut = (USMUserTable)api.getSecurityProvider().
                                            getTable(USM_SECURITY_MODEL);
            uut.addEntry(user);

            // create a SnmpEngineEntry for the localhost,port pair
            SnmpEngineEntry e = new SnmpEngineEntry("localhost", portNo);
            e.setEngineID(engineID.getBytes());

            // Add the SnmpEngineEntry reference to USMUserEntry.
            user.setEngineEntry(e);
        }

        // set community in case of v1/v2c
        if(values[COMMUNITY] != null)
            session.setCommunity(values[COMMUNITY]);
        
        // Open the session
        try { 
            session.open(); 
        }
        catch (SnmpException e) {
            System.err.println(e);
            System.exit(1);
        }
        System.out.println("snmpv3trapd ready to receive v1/v2c/v3 traps");
    }
    
    public boolean authenticate(SnmpPDU pdu, String community){
        if(pdu.getVersion() == SnmpAPI.SNMP_VERSION_3)
            return true;
        else
            return (pdu.getCommunity().equals(community));
    }
  
    public boolean callback(SnmpSession session,SnmpPDU pdu, int requestID){
        // check trap version
        if (pdu.getCommand() == api.TRP_REQ_MSG) {
            System.out.println("Trap received from: "
                +pdu.getProtocolOptions().getSessionId()
                +", community: " + pdu.getCommunity());
            System.out.println("Enterprise: " + pdu.getEnterprise());
            System.out.println("Agent: " 
                                + (pdu.getAgentAddress()).getHostAddress());
            System.out.println("TRAP_TYPE: " + pdu.getTrapType());
            System.out.println("SPECIFIC NUMBER: " + pdu.getSpecificType());
            System.out.println("Time: " + pdu.getUpTime()+"\nVARBINDS:");
            // print varbinds
            for (Enumeration e = pdu.getVariableBindings().elements();
                                                    e.hasMoreElements();)
                System.out.println(((SnmpVarBind) e.nextElement()).
                                                    toTagString());
        }
        else if(pdu.getCommand() == api.TRP2_REQ_MSG)
        {
            System.out.println("Trap received from: "
                + pdu.getProtocolOptions().getSessionId()
                + ", community: " + pdu.getCommunity());
            for (Enumeration e = pdu.getVariableBindings().elements();
                                                    e.hasMoreElements();)
                System.out.println(((SnmpVarBind) e.nextElement()).
                                                    toTagString());
        }
        else
            System.err.println("Non trap PDU received.");

        System.out.println(""); // a blank line between traps

        return true;
  
    }
  
    public void debugPrint(String debugOutput){
        System.out.println(debugOutput);
        return;    
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
