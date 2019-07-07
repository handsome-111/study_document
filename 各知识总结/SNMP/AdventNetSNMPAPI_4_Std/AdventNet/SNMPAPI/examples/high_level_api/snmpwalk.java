/*$Id: snmpwalk.src,v 1.4.2.2 2006/07/10 06:43:13 srikrishnan Exp $*/
/*
 * @(#)snmpwalk.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/** 
 *  Do an SNMP walk based on command line arguments.  Loads MIBs 
 *  as specified, and converts to/from names for loaded MIB data.
 *  Multiple OIDs are ok, but checking for when to stop
 *  is based on first OID. 
 *
 * [-d]                - Debug output. By default off.
 * [-c] <community>    - community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-t] <Timeout>      - Timeout. By default 5000ms.
 * [-r] <Retries>      - Retries. By default 0.      
 * [-m] <mibs>           - The mibs to be loaded.
 * [-v] <version>      - version(v1 / v2 / v3). By default v1.
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>  - The snmpv3 contextName to be used.
 * [-i] <contextID>    - The snmpv3 contextID to be used.
 * [-pp] <privProtocol> - The privacy protocol . Must be accompanied with auth,priv password and authProtocol fields.
 * <host> Mandatory    - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <OID>  Mandatory    - Give multiple no. of Object Identifiers.
 */

import com.adventnet.snmp.beans.*;
import com.adventnet.snmp.snmp2.*;

public class snmpwalk {
    
    private static final int COMMUNITY = 1;
    private static final int PORT = 2;
    private static final int RETRIES = 3;
    private static final int TIMEOUT = 4;
    private static final int MIBS = 5;



    private static final int VERSION = 0;
    private static final int USER_NAME = 6;
    private static final int AUTH_PROTOCOL = 7;
    private static final int AUTH_PASSWORD = 8;
    private static final int PRIV_PASSWORD = 9;
	private static final int CONTEXT_NAME = 10;
	private static final int CONTEXT_ID = 11;
    private static final int DEBUG = 12;
    private static final int PRIV_PROTOCOL = 13;

    public static void main(String args[]) {

        // Take care of getting options
    String usage = "snmpwalk [-v version(v1,v2,v3)] [-m MIB_files] [-c community] [-p port] [-t timeout] [-r retries] [-u user] [-a auth_protocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] [-d debug] [ -pp priv_protocol (DES/AES) ] host OID";
    String options[] = { "-v", "-c", "-p", "-r", "-t", "-m" , "-u", "-a", "-w", "-s", "-n", "-i", "-d", "-pp" };
    String values[] = { null, null, null, null, null, null, null, null, null, null, null, null, "None", null};

    String authProtocol = new String("NO_AUTH");

    ParseOptions opt = new ParseOptions(args,options,values, usage);

    // check for at least hostname and one OID in remaining arguments
    if (opt.remArgs.length != 2) opt.usage_error();
 if(values[VERSION] != null)
  {       
  if ((values[VERSION].equals("v3")) && (values[USER_NAME] == null)) opt.usage_error();
}

    // Use an SNMP target bean to perform SNMP operations
    SnmpTarget target = new SnmpTarget();

	 //To load MIBs from compiled file
	 target.setLoadFromCompiledMibs(true);

    if (values[DEBUG].equals("Set")) target.setDebug(true);


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

    if (values[MIBS] != null) try { // Load the MIB files 
        System.err.println("Loading MIBs: "+values[MIBS]);
        target.loadMibs(values[MIBS]);
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
    }

	if(target.getSnmpVersion() == target.VERSION3){
		target.create_v3_tables();
	}

    // Set the OID on the SnmpTarget instance
    target.setObjectID(opt.remArgs[1]);

    int maxtry = 0;
    SnmpOID[] oidList = target.getSnmpOIDList();
    if(oidList == null)	{
    	System.out.println("Invalid OID has been specified / Check if the OID is present in the MIB loaded if any");
    	target.releaseResources();
    }
    else	{
	    SnmpOID rootoid = oidList[0];
	    while ( maxtry++ < 1000) {  // limit the max getnexts to 1000
		String result[] = target.snmpGetNextList();
		if (result == null) break;
		if ( !SnmpTarget.isInSubTree(rootoid,target.getSnmpOID()) )
		  break;  // check first column

		if (maxtry == 1) System.out.println("Response received: ");

		for (int i=0;i<result.length;i++) {  // print the values
		    System.out.println(target.getObjectID(i) + ": " + result[i]);
		}

	    }

	    if (maxtry == 1) {  // we did not get a valid row
		System.err.println("Request failed, timed out or no available data. \n"+
			   target.getErrorString());
	    } 
	    target.releaseResources();
	}

    }

}
