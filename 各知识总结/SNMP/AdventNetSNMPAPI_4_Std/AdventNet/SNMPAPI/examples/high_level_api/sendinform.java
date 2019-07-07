/* $Id: sendinform.src,v 1.3.2.1 2006/07/10 11:23:44 srikrishnan Exp $ */
/*
 * @(#)sendinform.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/** 
 *  Send SNMP inform based on command line arguments.  Loads MIBs 
 *  as specified, and converts to/from names for loaded MIB data. 
 *  Since variable types are not input, MIBs have to be loaded for
 *  any variables being used in inform PDU.
 *
 * This is an example program to explain how to write an application to send a
 * inform message using com.adventnet.snmp.beans package of AdventNetSNMP api.
 * The user could run this application by giving any one of the following usage.
 *  
 * Note: Refer README file  
 *
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 * 
 * If the mib is loaded you can also give string oids in the following formats
 * .iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0 or system.sysDescr.0 or
 * sysDescr.0 .
 *
 * [-d]                - Debug output. By default off.
 * [-c] <community>    - community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-m] <mibs>         - The mibs to be loaded. 
 * [-v] <version>      - version(v2 / v3). By default v2.
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>  - The snmpv3 contextName to be used.
 * [-i] <contextID>    - The snmpv3 contextID to be used.
 * [-pp] <privProtocol> - The privacy protocol. Must be accompanied with auth,priv password and authProtocol fields.
 * <host> Mandatory    - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <timeticks> Mandatory - the value of object sysUpTime when the event occurred
 * <OID-value> Mandatory - Object Identifier  
 * <OID>  Mandatory    - Give multiple no. of Object Identifiers.
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.beans.*;

public class sendinform {

    private static final int VERSION = 4;
    private static final int USER_NAME = 5;
    private static final int AUTH_PROTOCOL = 6;
    private static final int AUTH_PASSWORD = 7;
    private static final int PRIV_PASSWORD = 8;
    private static final int CONTEXT_NAME = 9;
    private static final int CONTEXT_ID= 10;
    private static final int PRIV_PROTOCOL = 11;
    
    private static final int COMMUNITY = 1;
    private static final int PORT = 2;
    private static final int MIBS = 3;

    private static final int DEBUG = 0;

    public static void main(String args[]) {

    // Take care of getting options
    String usage = "sendinform [-d Debug][-v version(v2,v3)] [-c community] [-p port] [-u user] [-a auth_protocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID ] [ -pp priv_protocol (AES/DES) ] [-m MIB_file] host TimeTicksvalue trapOID [OID value] ...";
    String options[] = { "-d", "-c", "-p", "-m", "-v", "-u", "-a", "-w", "-s", "-n", "-i","-pp" };
    String values[] = { "None", null, null, null,null, null, null, null, null, null , null ,null};
    long upTime=0;
    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length<3) opt.usage_error();

    // Use an SNMP target bean to perform SNMP operations
    SnmpTarget target = new SnmpTarget();

	 //To load MIBs from compiled file
	 target.setLoadFromCompiledMibs(true);

    // Set Debug mode
    if (values[DEBUG].equals("Set")) target.setDebug(true);

    target.setTargetHost( opt.remArgs[0] );  // set destination hostname
    
    //Assign the port for sending the trap.
    target.setTargetPort(162);
    if(values[PORT]!=null) {
        try {
            target.setTargetPort(Integer.parseInt(values[PORT]));
        }
        catch(NumberFormatException e) {
            System.err.println("Sending trap to port 162");
        }
    }
    try {
        upTime = Long.parseLong(opt.remArgs[1]);
    } catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Argument for upTime "+ex);
        opt.usage_error();
    }
    
    target.setSnmpVersion(target.VERSION2C);
    if (values[VERSION] != null) {// set the version if specified
        if(values[VERSION].equals("v3"))
            target.setSnmpVersion(target.VERSION3);
        else if(values[VERSION].equals("v2"))
			target.setSnmpVersion(target.VERSION2C);
		else
			System.out.println("Invalid Version Number "+values[VERSION]);	    
	}

    if(target.getSnmpVersion()== target.VERSION3) {

        if (values[USER_NAME] != null) {
            target.setPrincipal(values[USER_NAME]);
        
        if ((values[AUTH_PROTOCOL] != null) && (values[AUTH_PASSWORD] != null)) {
            if(values[AUTH_PROTOCOL].equals("SHA"))
                target.setAuthProtocol(target.SHA_AUTH);
            else if(values[AUTH_PROTOCOL].equals("MD5"))
                target.setAuthProtocol(target.MD5_AUTH);
            else
                target.setAuthProtocol(target.NO_AUTH);            
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
        else if ((values[AUTH_PROTOCOL] != null) || (values[AUTH_PASSWORD] != null) || (values[PRIV_PASSWORD] != null)) {
            opt.usage_error();
        }
        }
        else {
            System.err.println("UserName Missing");
            opt.usage_error();
        }
    }
	if(values[COMMUNITY] != null)
  	      target.setCommunity( values[COMMUNITY]);


    if (values[MIBS] != null) try {  // Load the MIB files 
        System.err.println("Loading MIBs: "+values[MIBS]);
        target.loadMibs(values[MIBS]);
        System.err.println("Done.");
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
	System.exit(1);
    }

	if(target.getSnmpVersion()== target.VERSION3) {
		target.create_v3_tables();
	}

    // Put together OID and variable value lists from command line
    String oids[] = null, var_values[] = null;  // trap oids and values

    int num_varbinds = 0;
    for (int i=3;i<opt.remArgs.length;i+=2) { // add Variable Bindings
        if (opt.remArgs.length < i+2) //need "{OID type value}"
           opt.usage_error(); 
        num_varbinds++;
    }

    oids = new String[num_varbinds];
    var_values = new String[num_varbinds];
    for (int i=0;i<num_varbinds;i++) { // add Variable Bindings
        oids[i] = opt.remArgs[(2*i)+3];
        var_values[i] = opt.remArgs[(2*i)+4];
     }

	String result[] = null;
    try {  // use SnmpTarget methods to send trap w/ specified OIDs/values
        target.setObjectIDList(oids);
        result = target.snmpSendInformRequest(upTime, opt.remArgs[2], 
														var_values);
    } catch (Exception e) {
        System.err.println("Error Sending Trap: "+e.getMessage());
    }
    if(result == null){
		System.err.println("inform Request failed or timed out.");
	}
	else{
		System.out.println("Response received.  Values:");
	
		for (int i=0;i<oids.length;i++) {
			System.out.println(target.getObjectID(i) + ": " + result[i]);

		}
	}

    System.exit(0);
    
    }

}
