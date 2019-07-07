/* $Id: snmpgetnext.src,v 1.4.2.5 2006/07/07 14:39:44 rravikumar Exp $ */
/*
 * @(#)snmpgetnext.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to do
 * the basic SNMP operation GETNEXT using com.adventnet.snmp.snmp2 and
 * com.adventnet.snmp.mibs packages of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpgetnext [options] hostname oid ...
 *
 * v1 request: 
 * java snmpgetnext [-d] [-m MIB_files] [-c community] [-p port] [-t timeout] [-r retries] host OID [OID] ...
 * e.g. 
 * java snmpgetnext -p 161 -c public -m ../../../mibs/RFC1213-MIB adventnet 1.1.0 1.2.0
 * 
 * v2c request: 
 * java snmpgetnext [-d] [-v version(v1,v2)] [-m MIB_files] [-p port] [-t timeout] [-r retries] host OID [OID] ...
 * e.g. For v1 request give -v v1 or drop the option -v .
 * java snmpgetnext -p 161 -v v2 -c public -m ../../../mibs/RFC1213-MIB adventnet 1.1.0 1.2.0
 * 
 * v3 request:
 * java snmpgetnext [-d] [-v version(v1,v2,v3)] [-c community] [-m MIB_files] [-p port] [-r retries] [-t timeout] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] host OID [OID] ...
 * e.g.
 * java snmpgetnext -m ../../../mibs/RFC1213-MIB -v v3 -u initial2 -w initial2Pass -a MD5 10.3.2.120 1.2.0
 * 
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 * 
 * If the mib is loaded you can also give string oids in the following formats
 * .iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0 or system.sysDescr.0 or
 * sysDescr.0 .
 *
 * Options:
 * [-d]                - Debug output. By default off.
 * [-c] <community>    - community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-t] <Timeout>      - Timeout. By default 5000ms.
 * [-r] <Retries>      - Retries. By default 0. 
 * [-m] <MIBfile>      - MIB files.To load multiple mibs give within double quotes files seperated by a blank space.       
 * [-v] <version>      - version(v1 / v2 / v3). By default v1.
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>  - The contextName to be used for the v3 pdu.
 * [-i] <contextID>    - The contextID to be used for the v3 pdu.
 * <host> Mandatory    - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <OID>  Mandatory    - Give multiple no. of Object Identifiers.
 */

import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.usm.*;

public class snmpgetnext {

    private static final int DEBUG = 0;
    private static final int MIBS = 6;
    
    public static void main(String args[]) {
    
    // Take care of getting options



    String usage = 
        "\nsnmpgetnext [-d] [-v version(v1,v2,v3)] [-c community]\n"+
        "[-m MIB_files] [-p port] [-r retries] [-t timeout] [-u user]\n"+
        "[-a auth_protocol] [-w auth_password] [-s priv_password]\n"+
        "[-n contextName] [-i contextID][-pp privProtocol (AES/DES)] host OID [OID] ...";

    String options[] = 
        {
            "-d", "-c",  "-wc", "-p", "-r", "-t", "-m", "-v", "-u", "-a", "-w", "-s", "-n", "-i", "-pp"
        };

    String values[] = 
        {
            "None", null, null, null, null, null, null, null, null, null, null, null, null, null,null 
        };
    
    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length<2) 
    {
        opt.usage_error();
    }   

    // Start SNMP API
    SnmpAPI api;
    api = new SnmpAPI();
    
    if (values[DEBUG].equals("Set")) 
    {
        api.setDebug( true );
    }   

    // Open SnmpSession 
    SnmpSession session = new SnmpSession(api);

   int PORT = 3;
        UDPProtocolOptions ses_opt = new UDPProtocolOptions();
        ses_opt.setRemoteHost(opt.remArgs[0]);
        if(values[PORT] != null)
        {
          try
          {
              ses_opt.setRemotePort(Integer.parseInt(values[PORT]));
          }
          catch(Exception exp)
          {
              System.out.println("Invalid port: " + values[PORT]);
              System.exit(1);
          }
        }
        session.setProtocolOptions(ses_opt);
  

    //set the values
    SetValues setVal = new SetValues(session, values);
    if(setVal.usage_error)
    {
        opt.usage_error(); 
    }   

    // Loading MIBS
    MibOperations mibOps = new MibOperations();
     
    // To load MIBs from compiled file
     mibOps.setLoadFromCompiledMibs(true);

    if (values[MIBS] != null)
    {
        try 
        {
            System.err.println("Loading MIBs: "+values[MIBS]);
            mibOps.loadMibModules(values[MIBS]);
        }
        catch (Exception ex) 
        {
            System.err.println("Error loading MIBs: "+ex);
        }
    }

    // Build Get request PDU
    SnmpPDU pdu = new SnmpPDU();
    pdu.setCommand( api.GETNEXT_REQ_MSG );

    // add OIDs
    for (int i=1;i<opt.remArgs.length;i++) 
    { 
        SnmpOID oid = mibOps.getSnmpOID(opt.remArgs[i]);
        if (oid == null) 
        {
            System.exit(1);
        }   
        else
        {
            pdu.addNull(oid);
        }   
    }

    try 
    {
        session.open();        // Send PDU
    } 
    catch (SnmpException e) 
    { 
        System.err.println("Error opening seesion"+e.getMessage());
        System.exit(1);
    }
    

    if(session.getVersion()==SnmpAPI.SNMP_VERSION_3) 
    {
        pdu.setUserName(setVal.userName.getBytes());
        try
        {
           	USMUtils.init_v3_parameters(
					setVal.userName,
					null,
					setVal.authProtocol,
					setVal.authPassword,
					setVal.privPassword,
					ses_opt,
					session,
					false,
					setVal.privProtocol);
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        pdu.setContextName(setVal.contextName.getBytes());
        pdu.setContextID(setVal.contextID.getBytes());
    }

    SnmpPDU res_pdu = null;
    try 
    {
        // Send PDU
        res_pdu = session.syncSend(pdu);
    }
    catch (SnmpException ex) 
    {
        System.err.println("Sending PDU"+ex.getMessage());
        System.exit(1);
    }

    if (res_pdu == null) 
    { 
        // timeout
        System.out.println("Request timed out to: " + opt.remArgs[0] );
        System.exit(1);
    }

    // print and exit
    System.out.println("Response Received from "+ res_pdu.getProtocolOptions().getSessionId());

    // Check for error in response
    if (res_pdu.getErrstat() != 0)
    {
        System.err.println("Error in response: " + mibOps.getErrorString(res_pdu));
    }   
    else
    {
        // print the response pdu varbinds
        System.out.println(mibOps.varBindsToString(res_pdu));

        // print the response pdu
        System.out.println(mibOps.toString(res_pdu));
    }
    
    // close session
    session.close();
    // stop api thread
    api.close();
    
    System.exit(0);

    }

}
