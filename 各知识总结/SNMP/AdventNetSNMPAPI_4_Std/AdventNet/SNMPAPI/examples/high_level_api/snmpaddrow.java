/*$Id: snmpaddrow.src,v 1.3.2.1 2006/07/10 11:36:26 srikrishnan Exp $*/
/*
 * @(#)snmpaddrow.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/** 
 *  Add a row based on command line arguments.  Loads MIBs 
 *  as specified, and converts to/from names for loaded MIB data.
 *  Since we do not ask user for variable type, in order to
 *  convert set value strings to the correct variable type, the  
 *  MIB corresponding to any object must be loaded.  
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
 * [-pp] <privProtocol> - The privacy protocol. Must be accompanied with auth, password and authProtocol fields.
 * <mibs> Mandatory    - The mibs to be loaded.
 * <host> Mandatory    - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <OID>  Mandatory    - Give multiple no. of Object Identifiers.
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.beans.*;

public class snmpaddrow {

    private static final int COMMUNITY = 1;
    private static final int PORT = 2;
    private static final int RETRIES = 3;
    private static final int TIMEOUT = 4;
    



    private static final int VERSION = 0;
    private static final int USER_NAME = 5;
    private static final int AUTH_PROTOCOL = 6;
    private static final int AUTH_PASSWORD = 7;
    private static final int PRIV_PASSWORD = 8;
    private static final int DEBUG = 9;
    private static final int CONTEXT_NAME =10;
    private static final int CONTEXT_ID = 11;
    private static final int PRIV_PROTOCOL = 12;

    public static void main(String args[]) {

        // Take care of getting options
    String usage = "snmpaddrow [-v version(v1,v2,v3)] [-c community] [-p port] [-t timeout] [-r retries] [-u user] [-a auth_protocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-d debug] [-n contextName ] [-i contextID ] [ -pp priv_protocol (AES/DES)] host MIB_files OID value [OID value] ...";
    String options[] = { "-v", "-c", "-p", "-r", "-t", "-u", "-a", "-w", "-s", "-d","-n","-i","-pp" };
    String values[] = { null, null, null, null, null, null, null, null, null, "None", null, null, null};

    String authProtocol = new String("NO_AUTH");
      
    ParseOptions opt = new ParseOptions(args,options,values, usage);

    // Use an SNMP table bean to perform SNMP operations
    SnmpTable table = new SnmpTable();

	 //To load MIBs from compiled file
	 table.setLoadFromCompiledMibs(true);

    // at least 3 arguments are needed to do anything useful
    if (opt.remArgs.length<4) opt.usage_error();
        if (values[DEBUG].equals("Set")) table.setDebug(true);

    table.setTargetHost( opt.remArgs[0] );  // set the agent hostname
    if (values[COMMUNITY] != null) // set the community if specified
        table.setCommunity( values[COMMUNITY] );


    if(values[VERSION] != null) {  // if SNMP version is specified, set it
        if(values[VERSION].equals("v2"))
            table.setSnmpVersion( SnmpTable.VERSION2C ) ;
        else if(values[VERSION].equals("v1"))
            table.setSnmpVersion( SnmpTable.VERSION1 );
        else if(values[VERSION].equals("v3"))
            table.setSnmpVersion( SnmpTable.VERSION3 );
        else {
            System.out.println("Invalid Version Number"); 
            System.exit(1);
        }
    }

    try { // set the timeout/retries/port parameters, if specified
        if (values[PORT] != null) 
            table.setTargetPort( Integer.parseInt(values[PORT]) );
        if (values[RETRIES] != null) 
            table.setRetries( Integer.parseInt(values[RETRIES]) );
        if (values[TIMEOUT] != null) 
            table.setTimeout( Integer.parseInt(values[TIMEOUT]) );
    } catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Argument "+ex);
        opt.usage_error();
    }

    if(table.getSnmpVersion() ==table.VERSION3) {
        if (values[USER_NAME] != null) 
            table.setPrincipal(values[USER_NAME]);
        if (values[AUTH_PROTOCOL] != null) {
            //System.out.println("authProtocol = " + authProtocol);
            authProtocol = values[AUTH_PROTOCOL];
        }
        if(authProtocol.equals("SHA"))
            table.setAuthProtocol(table.SHA_AUTH);
        else if(authProtocol.equals("MD5"))
            table.setAuthProtocol(table.MD5_AUTH);
        else
            table.setAuthProtocol(table.NO_AUTH);
        if (values[AUTH_PASSWORD] != null) 
            table.setAuthPassword(values[AUTH_PASSWORD]);
         if (values[PRIV_PASSWORD] != null)
        {
            table.setPrivPassword(values[PRIV_PASSWORD]);
            if(values[PRIV_PROTOCOL] != null)
            {
                    if(values[PRIV_PROTOCOL].equals("AES"))
                    {
                            table.setPrivProtocol(table.CFB_AES_128);
                    }
                    else if(values[PRIV_PROTOCOL].equals("DES"))
                    {
                            table.setPrivProtocol(table.CBC_DES);
                    }
                    else
                    {
                            System.out.println(" Invalid PrivProtocol "+values[PRIV_PROTOCOL]);
                            opt.usage_error();
                    }
            }
        }

        if(values[CONTEXT_NAME]!= null)
                table.setContextName(values[CONTEXT_NAME]);
        if(values[CONTEXT_ID]!= null)
                table.setContextID(values[CONTEXT_ID]);
    }

    if (opt.remArgs[1] != null) try { // Load the MIB files 
        System.err.println("Loading MIBs: "+opt.remArgs[1]);
        table.loadMibs(opt.remArgs[1]);
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
	}

		if(table.getSnmpVersion() ==table.VERSION3){
				table.create_v3_tables();
		}
	String oids[] = null, var_values[] = null;  // trap oids and values

	int num_varbinds = 0;
	for (int i=2;i<opt.remArgs.length;i+=2) { // add Variable Bindings
		if (opt.remArgs.length < i+2) //need "{OID type value}"
				opt.usage_error(); // unmatched arguments for name-value pairs
		num_varbinds++;
	}

	oids = new String[num_varbinds];
	var_values = new String[num_varbinds];
	for (int i=0;i<num_varbinds;i++) { // add Variable Bindings
			oids[i] = opt.remArgs[(2*i)+2];
			var_values[i] = opt.remArgs[(2*i)+3];
	}

	try {
			table.addRow(false,oids,var_values);//adding a row to the table
			String result[] = table.snmpGetList();

			if (result == null) {
				System.err.println("Request failed or timed out. \n"+
							table.getErrorString());
			} else { // print response
				System.out.println("Set OK.  Return values:");

				for (int i=0;i<oids.length;i++) {
					System.out.println(table.getObjectID(i) +": "+result[i]);
				}
			}

	} catch (Exception e) {
			System.err.println("Set Error: "+e.getMessage());
	}

	System.exit(0);

	}

}

