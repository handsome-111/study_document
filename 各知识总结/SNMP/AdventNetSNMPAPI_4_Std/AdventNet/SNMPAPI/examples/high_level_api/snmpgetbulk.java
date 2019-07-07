/*$Id: snmpgetbulk.src,v 1.3.2.2 2006/07/10 06:43:13 srikrishnan Exp $*/
/*
 * @(#)snmpgetbulk.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/** 
 *  Do an SNMP Getbulk based on command line arguments.  Loads MIBs 
 *  as specified, and converts to/from names for loaded MIB data. 
 *
 * [-d]                - Debug output. By default off.
 * [-c] <community>    - community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-t] <Timeout>      - Timeout. By default 5000ms.
 * [-r] <Retries>      - Retries. By default 0. 
 * [-m] <mibs>           - The mibs to be loaded.
 * <nr>                 - non-repeaters.
 * <mr>                 - max-repetitions.
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>  - The snmpv3 contextName to be used.
 * [-i] <contextID>    - The snmpv3 contextID to be used.
 * [-pp] <privProtocol> - The privacy protocol . Must be accompanied with auth,priv password and authProtocol fields.
 * <host> Mandatory    - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <OID>  Mandatory    - Give only one Object Identifier.
 */

import com.adventnet.snmp.beans.*;
import com.adventnet.snmp.snmp2.*;

public class snmpgetbulk {
    
    private static final int VERSION = 0;
    private static final int COMMUNITY = 1;
    private static final int PORT = 2;
    private static final int RETRIES = 3;
    private static final int TIMEOUT = 4;
    private static final int DEBUG = 5;
    private static final int MIBS = 6;
    private static final int NON_REPETERS = 7;
    private static final int MAX_REPETITIONS = 8;
    private static final int USER_NAME = 9;
    private static final int AUTH_PROTOCOL = 10;
    private static final int AUTH_PASSWORD = 11;
    private static final int PRIV_PASSWORD = 12;
	private static final int CONTEXT_NAME = 13;
	private static final int CONTEXT_ID = 14;
  private static final int PRIV_PROTOCOL = 15;

    public static void main(String args[]) {
    // Take care of getting options
    String usage = "snmpgetbulk [-v version(v2,v3)] [-m MIB_files] [-c community] [-p port] [-t timeout] [-r retries] [-d debug] [-nr non-repeaters] [-mr max-repetitions] [-u user] [-a auth_protocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] [ -pp priv_protocol (DES/AES) ] host OID [OID] ...";
    String options[] = { "-v", "-c", "-p", "-r", "-t", "-d", "-m", "-nr", "-mr", "-u", "-a", "-w", "-s", "-n", "-i", "-pp" };
    String values[] = { null, null, null, null, null, "None", null, null, null, null, null, null, null, null, null, null};

    String authProtocol = new String("NO_AUTH");
    ParseOptions opt = new ParseOptions(args,options,values, usage);

    // check for at least hostname and one OID in remaining arguments
    if (opt.remArgs.length<2) opt.usage_error();
 if(values[VERSION] != null)
  {        
 if ((values[VERSION].equals("v3")) && (values[USER_NAME] == null)) opt.usage_error();
}
    // Use an SNMP target bean to perform SNMP operations
    SnmpTarget target = new SnmpTarget();

	 //To load MIBs from compiled file
	 target.setLoadFromCompiledMibs(true);
	 
	 if(values[DEBUG].equals("Set"))
	 target.setDebug(true);

    target.setSnmpVersion( SnmpTarget.VERSION2C ) ;

    int nonRepeaters = 0; // we need this for printing

    if(values[VERSION] != null) { // if SNMP version is specified, set it
        if(values[VERSION].equals("v2"))
            target.setSnmpVersion( SnmpTarget.VERSION2C ) ;
        else if(values[VERSION].equals("v3"))
            target.setSnmpVersion( SnmpTarget.VERSION3 ) ;
        else {
            System.out.println("Invalid Version Number"); 
            System.exit(1);
        }
    }
    else 
        target.setSnmpVersion( SnmpTarget.VERSION2C ) ;

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
        if (values[NON_REPETERS] != null) 
            nonRepeaters = Integer.parseInt(values[NON_REPETERS]);
        target.setNonRepeaters( nonRepeaters );
        if (values[MAX_REPETITIONS] != null) 
            target.setMaxRepetitions( Integer.parseInt(values[MAX_REPETITIONS]) );

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
    // create OID array from command line arguments
    String oids[] = new String[opt.remArgs.length-1];
    for (int i=1;i<opt.remArgs.length;i++) oids[i-1] = opt.remArgs[i];
    
    // Set the OID List on the SnmpTarget instance
    target.setObjectIDList(oids);

    SnmpVarBind result[][] = target.snmpGetBulkVariableBindings(); // do a getbulk request
    
    if (result == null) {
        System.err.println("Request failed or timed out. \n"+
                   target.getErrorString());

    } else { // print the values
        System.out.println("Response received.  Values:\n");

        for (int i=0;i<nonRepeaters;i++)
        System.out.println(target.getMibOperations().toString(result[i][0]));
        
        StringBuffer sb = new StringBuffer("\nRepeaters:\n");
        
        for (int j=0;j<result[0].length;j++) {
            for (int i=nonRepeaters;i<oids.length;i++) { 
                sb.append(target.getMibOperations().toString(result[i][j])
                  + " \t ");
        }
            sb.append("\n");
        }

        System.out.println(sb.toString());
    }

    System.exit(0);

    }

}
