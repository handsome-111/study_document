/*$Id: gettable.src,v 1.3.2.2 2006/07/10 10:16:31 srikrishnan Exp $*/
/*
 * @(#)gettable.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/** 
 *  An example of using the SnmpTarget class to get a table.
 *  A simpler/better approach to looking at table data 
 *  is to use the SnmpTable class, which is
 *  illustrated in the getSnmpTable example.  This example is
 *  to provide another example of using the SnmpTarget class, and
 *  the MibOperations class.
 *
 *  The MIB with the requested table must be loaded for this example.
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
 * [-pp] <privProtocol> - The privacy protocol. Must be accompanied with auth,priv password and authProtocol fields.
 * host Mandatory      - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * tableOID  Mandatory - Give the Object Identifier of a Table.
 * mibs           - The mibs to be loaded.Mandatory.
 */

import com.adventnet.snmp.beans.*;
import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.*;
import java.util.*;

public class gettable {

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
    String usage = "gettable [-v version(v1,v2,v3)] [-c community] [-p port] [-t timeout] [-r retries] [-u user] [-a auth_protocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] [-d debug] [ -pp priv_protocol (AES/DES) ] host tableOID MIB_files";
    String options[] = { "-v", "-c", "-p", "-r", "-t", "-u", "-a", "-w", "-s", "-n", "-i", "-d", "-pp" };
    String values[] = { null, null, null, null, null, null, null, null, null, null, null, "None", null};

    String authProtocol = new String("NO_AUTH");
          
    ParseOptions opt = new ParseOptions(args,options,values, usage);

    // check for at least hostname and one OID in remaining arguments
    if (opt.remArgs.length<3) opt.usage_error();
 if(values[VERSION] != null)
  {     
 if ((values[VERSION].equals("v3")) && (values[USER_NAME] == null)) opt.usage_error();
}

    // Use an SNMP target bean to perform SNMP operations
    SnmpTarget target = new SnmpTarget();

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
    if (opt.remArgs[2] != null) try { // Load the MIB files 
        System.out.println("Loading MIBs: "+opt.remArgs[2]);
        target.loadMibs(opt.remArgs[2]);
        System.out.println("Done.");
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
    }

	if(target.getSnmpVersion() == target.VERSION3){
		target.create_v3_tables();
	}

	if (values[DEBUG].equals("Set"))
    {
        target.setDebug(true);
    }  

    // get a reference to the MIB operations instance
    MibOperations mibOps = target.getMibOperations();
    SnmpOID tableOID = mibOps.getSnmpOID(opt.remArgs[1]); // get table OID
    MibNode tableNode = mibOps.getMibNode(tableOID); // get table MIB node

    if (tableNode == null) { // could not get table MIB node
      System.err.println("Cannot find MIB node for table.  Correct MIB must be loaded: "+opt.remArgs[1]);  
      System.exit(1);
    } 

    Vector columns = tableNode.getTableItems();
    if ( (columns == null) || (columns.size() == 0) ) {
      System.err.println("Not a table.  No columns: "+opt.remArgs[1]);  
      System.exit(1);
    } 

    // We need to confirm the first column is read-accessible
    while (columns.size() > 0) {
      SnmpOID firstOID = mibOps.getSnmpOID((String)columns.elementAt(0));
      MibNode firstNode = mibOps.getMibNode(firstOID);
      if ( (firstNode.getAccess() != SnmpAPI.RONLY) &&
           (firstNode.getAccess() != SnmpAPI.RWRITE) && (firstNode.getAccess() !=SnmpAPI.RCREATE)) {
          System.err.println("Column inaccessible.  Drop: "+firstNode); 
          columns.removeElementAt(0);
      } else break;
    }
      
    // create OID array from table columns
    String oids[] = new String[columns.size()];
    for (int i=0;i<oids.length;i++) oids[i] = (String)columns.elementAt(i);
    
    target.setObjectIDList(oids); 

    // get entire table by doing successive get nexts
    String result[][] = target.snmpGetAllList();  
    
    if (result == null) {
        System.err.println("Request failed or timed out. \n"+
                   target.getErrorString());

    } else { // print the table
        System.out.println("Response received.  Table items:");
        StringBuffer sb = new StringBuffer();

        // first print the column names
        for (int i=0;i<oids.length;i++) sb.append(oids[i]+" \t");
        sb.append("\n");

        // next pront each table item
        for (int j=0;j<result.length;j++) { // for each row
          for (int i=0;i<result[j].length;i++) // for each column
              sb.append(result[j][i]+" \t");
          sb.append("\n");
        }
        System.out.println(sb.toString());
    }

    System.exit(0);

    }

}
