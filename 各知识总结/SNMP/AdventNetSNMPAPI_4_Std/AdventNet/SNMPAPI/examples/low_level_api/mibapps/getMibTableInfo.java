/* $Id: getMibTableInfo.src,v 1.3 2002/09/09 05:43:43 pushpar Exp $ */ 
/*
 * @(#)getMibTableInfo.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to use some of the methods of 
 * the mibs  package specific to Table nodes.
 * The user could run this application by giving the Usage.
 *  
 *	java getMibTableInfo [-m MIB_files] OID   
 *
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 * 
 *
 * Options:
 * [-d]                - Debug output. By default off.
 * -m   <MIBfile>      - MIB files.To load multiple mibs give within double quotes files seperated by a blank space. Mandatory.      
 * <OID>  Mandatory    - Object Identifier.
 */

import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.*;

public class getMibTableInfo {

	private static final int DEBUG = 0;
	private static final int MIBS = 1;

	public static void main(String args[]) {

		// Take care of getting options     
		String usage = "getMibTableInfo -m <MIB_files> OID";
		String options[] = { "-d", "-m" };
		String values[] = { "None", null};

		ParseOptions opt = new ParseOptions(args,options,values, usage);
		if (opt.remArgs.length<1 || opt.remArgs.length > 1) opt.usage_error();

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
			System.out.println("Invalid OID or the Node " + opt.remArgs[0] + " is not avialable");
			System.exit(0);
		}

		//check for table
		if(node.isTable()) {
			System.out.println("\nThe node is a Table");

			System.out.println("\nThe Table Entry: " + node.getChild(1));

			System.out.println("\nTable Items: " + node.getTableItems());

		}else if(node.isTableEntry()) {// check for table entry
			System.out.println("\nThe node is a TableEntry");
			System.out.println("\nisAugmentedEntry: " + node.getIsAugmented());

			System.out.println("\nAugments Node " + node.getAugments());  

			System.out.println("\nIndex Names: " + node.getIndexNames());

		}else if(node.isTableColumn()) {// check for table column
			System.out.println("\nThe node ia a Table Column");

			System.out.println("\nisReadable: " + node.isReadable());

			System.out.println("\nisWritable: " + node.isWriteable());

			System.out.println("\nIndexes for the table column: " + node.getIndexes(mibOps));

		}else {
			System.out.println("\nThe given OID is not a table or table entry or a table column");	
		}

	}
}
