/* $Id: mibnodeinfo.java,v 1.3 2002/09/09 05:43:43 pushpar Exp $ */ 
/*
 * @(#)mibnodeinfo.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to use some of the methods of the mibs
 * package.
 * The user could run this application by giving any one of the following usage.
 *  
 *	java mibinfo [-d] [-m MIB_files] [-n] [-s] [-l] OID   
 *
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 * 
 *
 * Options:
 * [-d]                - Debug output. By default off.
 * [-s]                _ prints the String OID (in the form .iso.org.dod.internet.mgmt.mib-2)
 * [-n]                - prints the associated numeric oid.
 * [-l]                - prints the associated syntax of the node.
 * [-D]                _ prints the associated description of the node.
 * [-P]                - prints all the properties of the MibNode.
 * -m   <MIBfile>      - MIB files.To load multiple mibs give within double quotes files seperated by a blank space. Mandatory.      
 * <OID>  Mandatory    - Object Identifier.
 */





import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.*;

public class mibnodeinfo {

	private static final int DEBUG = 0;
	private static final int MIBS = 6;

	public static void main(String args[]) {

		// Take care of getting options     
		String usage = "java mibnodeinfo [-d] -m MIB_files [-n] [-s] [-l] [-D] [-P] OID";
		String options[] = { "-d", "-n", "-s", "-l", "-D", "-P", "-m" };
		String values[] = { "None", "None", "None", "None", "None", "None", null};

		usage += "\n Options:" +
			"\n[-d]                - Debug output. By default off." +
			"\n[-s]                _ prints the String OID (in the form .iso.org.dod.internet.mgmt.mib-2)" +
			"\n[-n]                - prints the associated numeric oid." + 
			"\n[-l]                - prints the associated syntax of the node." +
			"\n[-D]                _ prints the associated description of the node." +
			"\n[-P]                - prinys all the properties of the MibNode." + 
			"\n-m   <MIBfile>      - MIB files.To load multiple mibs give within double quotes files seperated by a blank space. Mandatory." +
			"\n<OID>  Mandatory    - Object Identifier.";

		ParseOptions opt = new ParseOptions(args,options,values, usage);
		if (opt.remArgs.length<1 || opt.remArgs.length > 1) {
			opt.usage_error();
		}

		MibOperations mibOps = new MibOperations();

		if (values[DEBUG].equals("Set")) mibOps.setDebug( true );

		// To load MIBs from compiled file
		mibOps.setLoadFromCompiledMibs(true);

		// Loading MIBS
		if (values[MIBS] != null) {
			try {
				System.out.println("Loading MIBs: "+values[MIBS]);
				mibOps.loadMibModules(values[MIBS]);
			} catch (Exception ex) {
				System.out.println("Error loading MIBs: "+ex);
			}
		}else {
			System.out.println("Loading MIBs is mandatory");
			System.exit(0);
		}

		SnmpOID oid = mibOps.getSnmpOID(opt.remArgs[0]);
		MibNode node = mibOps.getMibNode(oid);
		if(node == null) {
			System.out.println("Invalid OID / the node " + opt.remArgs[0] + " is not avialable");
		}

		if(values[1].equals("Set")) {
			System.out.println("\nNumbered OID: " + oid);
		}

		if(values[2].equals("Set")) {
			System.out.println("\nString OID: " + node.getOIDString()); 
		}

		if(values[3].equals("Set")) {
			LeafSyntax syntax = mibOps.getLeafSyntax(oid);
			System.out.println("\nSyntax: " + syntax);
		}

		if(values[4].equals("Set")) {
			System.out.println("\nDescription: " + node.getDescription());
		}

		if(values[5].equals("Set")) {
			System.out.println("\n" + node.toTagString());
		}

	}
}
