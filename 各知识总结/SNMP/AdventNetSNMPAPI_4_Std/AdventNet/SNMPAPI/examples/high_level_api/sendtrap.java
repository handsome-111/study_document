/* $Id: sendtrap.java,v 1.3 2002/09/09 05:36:28 tonyjpaul Exp $ */
/*
 * @(#)sendtrap.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/** 
 *  Send SNMP trap based on command line arguments.  Loads MIBs 
 *  as specified, and converts to/from names for loaded MIB data. 
 *  Since variable types are not input, MIBs have to be loaded for
 *  any variables being used in trap PDU.
 *
 * This is an example program to explain how to write an application to send a
 * v1 Trap message using com.adventnet.snmp.beans packages of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java sendtrap [options] -m mibfile hostname enterprise-oid agent-addr generic-trap specific-trap 
 *      timeticks [OID value] ...
 *
 * java sendtrap [-c community] [-p port] -m MIB_files host enterprise agent-addr generic-trap specific-trap timeticks [OID value] ...
 * e.g. 
 * java sendtrap -p 162 -c public -m ../../mibs/RFC1213-MIB adventnet 1.2.0 adventnet 0 6 1000 1.5.0 advent
 * 
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 * 
 * Since the mib is loaded you can also give string oids in the following formats
 * .iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0 or system.sysDescr.0 or
 * sysDescr.0 .
 * 
 * Options:
 * [-d] option	   Debug output.By default off.	
 * [-c] option     community String. By default "public".
 * [-p] option     remote port no. By default 162.
 * [-m] option     MIB files to be loaded. Used to find the type of object.
 * enterprise      Object Identifier (sysObjectID for generic traps)
 * agent-addr      the IP address of the agent sending the trap
 * generic-trap    generic trap type INTEGER (0..6)
 * specific-trap   specific code INTEGER(0..2147483647)
 * timeticks       the value of object sysUpTime when the event occurred
 * host mandatory  The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * OID  option     Give multiple no. of Object Identifiers with value.
 * value           object-instance value . 
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.beans.*;

public class sendtrap {

    public static void main(String args[]) {

    // Take care of getting options
    String usage = "sendtrap [-d] [-c community] [-p port] [-m MIB_files] host enterprise agent-addr generic-trap specific-trap timeticks [OID value] ..."; 
    String options[] = { "-d", "-c", "-m", "-p"};
    String values[] = { "None", null,null, null};
      
    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length<6) opt.usage_error();


    // Use an SNMP target bean to perform SNMP operations
    SnmpTarget target = new SnmpTarget();

	 //To load MIBs from compiled file
	 target.setLoadFromCompiledMibs(true);
	 
    if (values[0].equals("Set")) target.setDebug(true);
	
    target.setTargetHost( opt.remArgs[0] );  // set destination hostname
    target.setTargetPort( 162 );  // set destination port
    if (values[1] != null) // set the community if specified
        target.setCommunity( values[1] );

    int trap_type=-1,specific_type=-1;
    long time=0;
    try {  // set the port parameter, if specified, and trap parameters
        if (values[3] != null) 
        target.setTargetPort( Integer.parseInt(values[3]) );
        trap_type = Integer.parseInt(opt.remArgs[3]); 
        specific_type = Integer.parseInt(opt.remArgs[4]);
        time = (long) Integer.parseInt(opt.remArgs[5]);
    } catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Argument "+ex);
        opt.usage_error();
    }

    if (values[2] != null) try {  // Load the MIB files 
        System.err.println("Loading MIBs: "+values[2]);
        target.loadMibs(values[2]);
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
	System.exit(1);
    }


    // Put together OID and variable value lists from command line
    String oids[] = null, var_values[] = null;  // trap oids and values

    int num_varbinds = 0;
    for (int i=6;i<opt.remArgs.length;i+=2) { // add Variable Bindings
        if (opt.remArgs.length < i+2) //need "{OID type value}"
        opt.usage_error(); 
        num_varbinds++;
    }

    oids = new String[num_varbinds];
    var_values = new String[num_varbinds];
    for (int i=0;i<num_varbinds;i++) { // add Variable Bindings
        oids[i] = opt.remArgs[(2*i)+6];
        var_values[i] = opt.remArgs[(2*i)+7];
     }

    try {  // use SnmpTarget methods to send trap w/ specified OIDs/values
        target.setObjectIDList(oids);
        target.snmpSendTrap(opt.remArgs[1], opt.remArgs[2], trap_type,
                specific_type, time, var_values);

        // allow time to send trap before exiting
        Thread.sleep(500);

    } catch (Exception e) {
        System.err.println("Error Sending Trap: "+e.getMessage());
    }

    System.exit(0);
    
    }
}
