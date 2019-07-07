/*$Id: snmpget_by_index.src,v 1.3.2.2 2006/07/10 06:43:13 srikrishnan Exp $*/
/*
 * @(#)snmpget_by_index.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the COPYRIGHTS file for more details.
 */

/** 
 *  Get SNMP data based on command line arguments.  Loads MIBs 
 *  as specified, and converts to/from names for loaded MIB data. 
 *
 * [-d]                - Debug output. By default off.
 * [-c] <community>    - community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-t] <Timeout>      - Timeout. By default 5000ms.
 * [-r] <Retries>      - Retries. By default 0.      
 * [-v] <version>      - version(v1 / v2 / v3). By default v1.
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>  - The snmpv3 contextName to be used.
 * [-i] <contextID>    - The snmpv3 contextID to be used.
 * [-s] <privProtocol> - The privacy protocol. Must be accompanied with auth,priv password and authProtocol fields.
 * host Mandatory      - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * mib  Mandatory      - The mib to be loaded.
 * OID  Mandatory      - Give multiple no. of Object Identifiers.
 * index Mandatory     - The INDEX component of the table entry.
 */

import com.adventnet.snmp.beans.*;
import com.adventnet.snmp.snmp2.*;

public class snmpget_by_index {

    private static final int COMMUNITY = 1;
    private static final int PORT = 2;
    private static final int RETRIES = 3;
    private static final int TIMEOUT = 4;
    



    private static final int VERSION = 0;
    private static final int USER_NAME = 5;
    private static final int AUTH_PROTOCOL = 6;
    private static final int AUTH_PASSWORD = 7;
    private static final int PRIV_PASSWORD = 8;
	private static final int CONTEXT_NAME = 9;
	private static final int CONTEXT_ID = 10;
    private static final int DEBUG = 11;
    private static final int PRIV_PROTOCOL = 12;

    public static void main(String args[]) {

    // Take care of getting options
    String usage = "snmpget_by_index [-v version(v1,v2,v3)] [-c community] [-p port] [-t timeout] [-r retries] [-u user] [-a auth_protocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] [-d debug] [ -pp priv_protocol (DES/AES) ] host MIB_File OID index [index] ...";
    String options[] = { "-v", "-c", "-p", "-r", "-t", "-u", "-a", "-w", "-s", "-n", "-i", "-d", "-pp" };
    String values[] = { null, null, null, null, null, null, null, null, null, null, null, "None", null};

    String authProtocol = new String("NO_AUTH");

    ParseOptions opt = new ParseOptions(args,options,values, usage);

    // check for hostname, index, and one OID in remaining arguments
    if (opt.remArgs.length<4) opt.usage_error();
    if(values[VERSION] != null)
    {      
    if ((values[VERSION].equals("v3")) && (values[USER_NAME] == null)) opt.usage_error();
  }

    // Use an SNMP target bean to perform SNMP operations
    SnmpTarget target = new SnmpTarget();
    if (values[DEBUG].equals("Set")) target.setDebug(true);

	 //To load MIBs from compiled file
	 target.setLoadFromCompiledMibs(true);


        if(values[VERSION] != null) {  // if SNMP version is specified, set it
            if(values[VERSION].equals("v2"))
                target.setSnmpVersion( SnmpTarget.VERSION2C ) ;
            else if(values[VERSION].equals("v1"))
                target.setSnmpVersion( SnmpTarget.VERSION1 );
            else if(values[VERSION].equals("v3"))
                target.setSnmpVersion( SnmpTarget.VERSION3 );
            else {
                    System.out.println("Invalid Version Number"); 
                 System.exit(1);
            }
        }

    target.setTargetHost( opt.remArgs[0] );  // set the agent hostname
    if (values[COMMUNITY] != null) // set the community if specified
        target.setCommunity( values[COMMUNITY] );

    try { // set the timeout/retries/port parameters, if specified
        if (values[PORT] != null) 
             target.setTargetPort( Integer.parseInt(values[PORT]) );
        if (values[RETRIES] != null) 
            target.setRetries( Integer.parseInt(values[RETRIES]) );
        if (values[TIMEOUT] != null) 
            target.setTimeout( Integer.parseInt(values[TIMEOUT]) );
    } catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Argument "+ex);
    }

    if(target.getSnmpVersion() == target.VERSION3) {
        if (values[USER_NAME] != null) 
            target.setPrincipal(values[USER_NAME]);
        if (values[AUTH_PROTOCOL] != null) {
            //System.out.println("authProtocol = " + authProtocol);
            authProtocol = values[AUTH_PROTOCOL];
        }
        if(authProtocol.equals("SHA"))
            target.setAuthProtocol(target.SHA_AUTH);
        else if(authProtocol.equals("MD5"))
            target.setAuthProtocol(target.MD5_AUTH);
        else
            target.setAuthProtocol(target.NO_AUTH);            
        if (values[AUTH_PASSWORD] != null) 
            target.setAuthPassword(values[AUTH_PASSWORD]);
        if (values[PRIV_PASSWORD] != null) 
         {
            target.setPrivPassword(values[PRIV_PASSWORD]);
            if(values[PRIV_PROTOCOL] != null)
            {
                    if(values[PRIV_PROTOCOL].equals("AES"))
                    {
                       target.setPrivProtocol(target.CFB_AES_128);
                    }
                    else if(values[PRIV_PROTOCOL].equals("DES"))
                    {
                       target.setPrivProtocol(target.CBC_DES);
                    }
                    else
                    {
                     System.out.println(" Invalid PrivProtocol "+values[PRIV_PROTOCOL]);
                     opt.usage_error();
                    }
            }
        }

		if(values[CONTEXT_NAME]!= null)
			target.setContextName(values[CONTEXT_NAME]);
		if(values[CONTEXT_ID]!= null)
			target.setContextID(values[CONTEXT_ID]);
    }

    if (opt.remArgs[1] != null) try { // Load the MIB files 
        System.err.println("Loading MIBs: "+opt.remArgs[1]);
        target.loadMibs(opt.remArgs[1]);
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
    }
    
	if(target.getSnmpVersion() == target.VERSION3){
		target.create_v3_tables();
	}
    
    try { 
      // create OID and index array from command line arguments
      String oid = opt.remArgs[2];
      String indexes[] = new String[opt.remArgs.length-3];
      for (int i=3;i<opt.remArgs.length;i++)
          indexes[i-3] = opt.remArgs[i];

    
      SnmpVarBind varb = 
        target.getMibOperations().createVariableBinding(oid, indexes, null);
      // Set the OID List on the SnmpTarget instance
      target.setSnmpOID(varb.getObjectID());

    } catch (Exception ex) {
        System.err.println("Error Setting OID: "+ex);
    }

    String result[] = target.snmpGetList();  // do a get request
    
    if (result == null) {
        System.err.println("Request failed or timed out. \n"+
                   target.getErrorString());

    } else { // print the values
        System.out.println("Response received.  Values:");

        System.out.println(target.getObjectID(0) + ": " + result[0]);
    }

    System.exit(0);

    }

}
