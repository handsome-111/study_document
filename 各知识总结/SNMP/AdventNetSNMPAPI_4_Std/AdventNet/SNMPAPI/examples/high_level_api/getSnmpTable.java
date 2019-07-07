/*$Id: getSnmpTable.src,v 1.3.2.2 2006/07/10 06:43:13 srikrishnan Exp $*/
/*
 * @(#)getSnmpTable.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/** 
 *  This is an example of using the SnmpTable class.
 *  This is a command line application that does not require JFC/swing
 *  components.  See the swingapps directory for examples that need
 *  and use swing components.
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
 * mibs   	           - The mibs to be loaded.Mandatory.
 */

import com.adventnet.snmp.beans.*;
import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.*;
import java.util.*;

public class getSnmpTable implements SnmpTableListener {

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
    String usage = "getSnmpTable [-v version(v1,v2,v3)] [-c community] [-p port] [-t timeout] [-r retries] [-u user] [-a auth_protocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-n contextname] [-i contextID] [-d debug] [ -pp priv_protocol (DES/AES) ] host tableOID MIB_files";
    String options[] = { "-v", "-c", "-p", "-r", "-t", "-u", "-a", "-w", "-s", "-n", "-i", "-d", "-pp"};
    String values[] = { null, null, null, null, null, null, null, null, null, null, null, "None", null};

    String authProtocol = new String("NO_AUTH");

    ParseOptions opt = new ParseOptions(args,options,values, usage);

    // check for at least hostname and one OID in remaining arguments
    if (opt.remArgs.length<3) opt.usage_error();    
    
    
 if(values[VERSION] != null)
  {     
 if ((values[VERSION].equals("v3")) && (values[USER_NAME] == null)) opt.usage_error();
}

    // instantiate an SnmpTable instance
    SnmpTable table = new SnmpTable();

	 //To load MIBs from compiled file
	 table.setLoadFromCompiledMibs(true);
	 
	 
	 if (values[DEBUG].equals("set")) table.setDebug(true);
	 


        if(values[VERSION] != null) {  // if SNMP version is specified, set it
            if(values[VERSION].equals("v2"))
                table.setSnmpVersion( SnmpTarget.VERSION2C ) ;
            else if(values[VERSION].equals("v1"))
                table.setSnmpVersion( SnmpTarget.VERSION1 );
            else if(values[VERSION].equals("v3"))
                table.setSnmpVersion( SnmpTarget.VERSION3 );
            else {
                System.out.println("Invalid Version Number"); 
                System.exit(1);
            }
        }

    table.setTargetHost( opt.remArgs[0] ); // set the agent hostname
    if (values[COMMUNITY] != null) // set the community if specified
        table.setCommunity( values[COMMUNITY] );

    try { // set the timeout/retries/port parameters, if specified
        if (values[PORT] != null) 
            table.setTargetPort( Integer.parseInt(values[PORT]) );
        if (values[RETRIES] != null) 
            table.setRetries( Integer.parseInt(values[RETRIES]) );
        if (values[TIMEOUT] != null) 
            table.setTimeout( Integer.parseInt(values[TIMEOUT]) );
    } catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Argument "+ex);
    }

    if(table.getSnmpVersion() == table.VERSION3) {
        if (values[USER_NAME] != null) 
            table.setPrincipal(values[USER_NAME]);
        if (values[AUTH_PROTOCOL] != null) {
            //System.out.println("authProtocol = " + authProtocol);
            authProtocol = values[AUTH_PROTOCOL];
        }
        if(authProtocol.equals("SHA"))
            table.setAuthProtocol(SnmpTarget.SHA_AUTH);
        else if(authProtocol.equals("MD5"))
            table.setAuthProtocol(SnmpTarget.MD5_AUTH);
        else
            table.setAuthProtocol(SnmpTarget.NO_AUTH);
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

    if (opt.remArgs[2] != null) try { // Load the MIB files 
        System.out.println("Loading MIBs: "+opt.remArgs[2]);
        table.loadMibs(opt.remArgs[2]);
        System.out.println("Done.");
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
    }

	if(table.getSnmpVersion() == table.VERSION3){
		table.create_v3_tables();
	}
	if (values[DEBUG].equals("Set"))
    {
        table.setDebug(true);
    }  

    // instantiate this class to receive table events, and register it
    getSnmpTable gettable = new getSnmpTable();
    table.addSnmpTableListener(gettable);
	// Set the table OID in SnmpTable.  Once specified,
    // SnmpTable will automatically get the data and send table events 
    try { 
        table.setTableOID( opt.remArgs[1] );
    } catch (Exception ex) {
        System.err.println("Invalid table OID: "+ex);
        System.exit(1);
    }

    System.out.println("Getting table.  Table items:");

    }


    /* This is the listener method which is notified of table changes **/
    public void tableChanged(SnmpTableEvent e) {

    SnmpTable table = (SnmpTable)e.getSource();
    if( e.isEndOfTable() || e.getType() == 2){
        if (table.getRowCount() == 0)  // no rows and we're being notified
            System.err.println(table.getErrorString());
        return;
    }

    StringBuffer sb = new StringBuffer();
        
    if (e.getFirstRow() == 0) {  // we're being notified of first row
      for (int i=0;i<table.getColumnCount();i++)  // print column names
        sb.append(table.getColumnName(i)+" \t");
      System.out.println(sb.toString());
    }

    // print the rows we're getting
    sb = new StringBuffer();
    for (int j=e.getFirstRow();j<=e.getLastRow();j++) { 
        for (int i=0;i<table.getColumnCount();i++) 
       	    sb.append(table.getValueAt(j,i)+" \t");
    }
    System.out.println(sb.toString());
    }
}
