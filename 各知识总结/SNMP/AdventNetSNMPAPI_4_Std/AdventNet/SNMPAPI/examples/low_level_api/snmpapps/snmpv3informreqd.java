/* $Id: snmpv3informreqd.src,v 1.5.2.3 2004/10/18 10:37:49 amaresh Exp $ */
/*
 * @(#)snmpv3informreqd.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program for receiving SNMP Inform Requests using the
 * com.adventnet.snmp.snmp2 package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv3informreqd [options]
 *
 * java snmpv3informreqd [-d] <-p port> <-u user> <-e engineId>
 * [-a authProtocol] [-w auth_password] [-s priv_password]
 * e.g. 
 * java snmpv3informreqd -p 2000 -u inform_test -e 1234 
 * 
 * Mandatory Options:
 * <-p port>        - remote port no.
 * <-u user>                - user name
 * <-e engineId>        - Engine Id
 *
 * Optional Options:
 * [-d]                - Debug output. By default off.
 * [-a authProtocol]     - Authentication protocol used.
 * [-w auth_password]    - Authentication password used.
 * [-s priv_password]    - Priv protocol used.
 *
 * Note: 
 * 1. If neither of -a, -w, -s are used, then the security level is NO_AUTH, 
 *      NO_PRIV,
 * 2. If -a, -w are used, then the security level is AUTH, NO_PRIV,
 * 3. If -a, -w and -s are used, then the security level is AUTH, PRIV,
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;

public class snmpv3informreqd implements SnmpClient {

  private final static int PORT = 1;
  private final static int COMMUNITY = 2;
  private final static int USER_NAME = 3;
  private final static int ENGID = 4;
  private final static int AUTH_PROTOCOL = 5;
  private final static int AUTH_PASSWORD = 6;
  private final static int PRIV_PROTOCOL = 7;
  private final static int PRIV_PASSWORD = 8;
  static final int USM_SECURITY_MODEL = 3;

    SnmpAPI api;
    SnmpSession session; 
    int localport;
    byte[] engineId = null;

    public snmpv3informreqd ()  {

        // Start SNMP API
        api = new SnmpAPI();

        // Create session 
        session = new SnmpSession(api);
    }

  public static void main(String args[]) {
        
    // Take care of getting options
    String usage = "snmpv3informreqd [-d] [-p port] [-c community] [-u user] [-e engineId] [-a authProtocol] [-w auth_password] [-pp privProtocol(DES/AES)] [-s priv_password]" ;
    String options[] = { "-d", "-p", "-c", "-u", "-e", "-a", "-w", "-pp", "-s"};
    String values[] = { "None", null, null, null, null, null, null, null,null};

    ParseOptions opt = new ParseOptions(args,options,values, usage);
    String userName = null;
    int authProtocol = USMUserEntry.NO_AUTH;
    String authPassword = null;
    String privPassword = null;
    int privProtocol = 0;
    byte secLevel = 0;

    snmpv3informreqd    infReqObj = new snmpv3informreqd(); 
        
    if (values[0].equals("Set")) 
    {
        infReqObj.api.setDebug( true );
    }
        
    if (opt.remArgs.length > 0) 
    {
        opt.usage_error();
    }
        
      // Add client to the session 
    infReqObj.session.addSnmpClient(infReqObj);

    // Set local port
    UDPProtocolOptions ses_opt = new UDPProtocolOptions();
    try {
        if (values[PORT] != null)
        {
            infReqObj.localport = Integer.parseInt(values[PORT]);
            ses_opt.setLocalPort(infReqObj.localport);
        }
        else 
        {
            ses_opt.setLocalPort(162);
        }
        infReqObj.session.setProtocolOptions(ses_opt);

        if (values[USER_NAME] != null) 
        {
            userName = values[USER_NAME];
        }
            
        if (values[ENGID] != null)
        {
            infReqObj.engineId = values[ENGID].getBytes();
        }

        if (values[AUTH_PROTOCOL] != null) 
        {
            if(infReqObj.engineId == null)  {
                System.out.println("EngineID is missing");          
                opt.usage_error();
            }       
            if (values[AUTH_PROTOCOL].equals("SHA"))
                {
                authProtocol = USMUserEntry.SHA_AUTH;
                secLevel |= 0x01;
                }
            else if (values[AUTH_PROTOCOL].equals("MD5"))
                {
                authProtocol = USMUserEntry.MD5_AUTH;
                secLevel |= 0x01;
                }
            else
                {
                authProtocol = USMUserEntry.NO_AUTH;
                }

        }
            
        if (values[AUTH_PASSWORD] != null) 
        {
            if(infReqObj.engineId == null)  {
                System.out.println("EngineID is missing");          
                opt.usage_error();
            }       
            if (secLevel == 0x01)
                {
                authPassword = values[AUTH_PASSWORD];
                }
            else
                {
                opt.usage_error();
            }
        }
            
        if(values[PRIV_PASSWORD] != null) 
        {
            if(infReqObj.engineId == null)  {
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
						privProtocol= USMUserEntry.CFB_AES_128;
					}
					else
					{
		                privProtocol = USMUserEntry.CBC_DES;
					}
				}
				else
				{
	                privProtocol = USMUserEntry.CBC_DES;
				}
                secLevel |= 0x02;
            }
            else
                {
                opt.usage_error();
            }
        }
      }
        catch (NumberFormatException ex) {
            System.err.println("Invalid Integer Arg");
        }

        if(userName != null) {
        // Create the USM table entry.
            infReqObj.createUSMTable(userName.getBytes(), authProtocol, 
                                    authPassword, privPassword,privProtocol,infReqObj.api);
        }

        // set community in case of v1/v2c
        if(values[COMMUNITY] != null)
            infReqObj.session.setCommunity(values[COMMUNITY]);

    // Open the session
    try { 
        infReqObj.session.open(); 
            System.out.println ("Waiting to receive SNMP Inform requests\n");
    }
    catch (SnmpException e) {
      System.err.println(e);
      System.exit(1);
    }
  }    
    
  public boolean authenticate(SnmpPDU pdu, String community){
    if(pdu.getVersion() == SnmpAPI.SNMP_VERSION_3)
        return (true);
    else
        return (pdu.getCommunity().equals(community));
  }
 
  /*
   * Callback method that is invoked on receiving an SNMP Inform request message
   */
  public boolean callback(SnmpSession session,SnmpPDU pdu, int requestID)
  {
    // Check received SNMP PDU command type.
    if (pdu.getCommand() == api.INFORM_REQ_MSG) 
    {
      System.out.println("INFORM REQUEST received from: " +
      pdu.getProtocolOptions().getSessionId());
      System.out.println("\nVARBINDS:");
  
      // Print varbinds
      for (Enumeration e=pdu.getVariableBindings().elements();
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
    
  public void debugPrint(String debugOutput)    {
  
    System.out.println(debugOutput);
    return;    
  }

    /*
     * Method to create the USM user entry in the USM table and the engine Id 
     * entry in the engine table.
     */
  public void createUSMTable(byte[] name, int authProtocol, 
                    String authPassword, String privPassword, int privProtocol, SnmpAPI api)
  {
    byte level = 0;

    USMUserEntry entry = new USMUserEntry(name, engineId);
    entry.setAuthProtocol(authProtocol);

    if ((authProtocol != USMUserEntry.NO_AUTH) && (authPassword != null))
    {
      byte[] authKey = USMUtils.password_to_key(authProtocol, 
                                            authPassword.getBytes(), 
                                            authPassword.getBytes().length, 
                                            engineId);
      entry.setAuthKey(authKey);
      level = 1;

      if (privPassword != null)
      {
        byte[] tempKey = USMUtils.password_to_key(authProtocol, 
                                            privPassword.getBytes(),
                                            privPassword.getBytes().length,
                                            engineId);
        byte privKey[]=new byte[16];
        System.arraycopy(tempKey,0,privKey,0,16);
        entry.setPrivKey(privKey);
        level |= 2;
      }
    }

    entry.setSecurityLevel(level);
    entry.setPrivProtocol(privProtocol);

        USMUserTable USMTable = (USMUserTable)api.getSecurityProvider().
                                            getTable(USM_SECURITY_MODEL);
        USMTable.addEntry(entry);

    byte[] names=entry.getUserName();
        byte[] id=entry.getEngineID();

    SnmpEngineEntry e = new SnmpEngineEntry("localhost", localport);
    e.setEngineID(engineId);
    entry.setEngineEntry(e);

    api.setSnmpEngineID(engineId);
  }
}
