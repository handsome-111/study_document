/* $Id: snmpset.src,v 1.4.2.7 2006/07/07 14:55:36 rravikumar Exp $ */
/*
 * @(#)snmpset.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to do
 * the basic SNMP operation SET using com.adventnet.snmp.snmp2 and
 * com.adventnet.snmp.mibs packages of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpset [options] hostname oid value [oid value] ...
 *
 * v1 request:
 * java snmpset [-d] [-c community] [-wc writeCommunity] [-p port] [-t timeout] [-r retries] MIB_files host [OID value] ...
 * e.g. 
 * java snmpset -p 161 -c public  ../../../mibs/RFC1213-MIB adventnet 1.1.0 advent-machine 1.4.0 contact-advent
 *
 * v2c request:
 * java snmpset [-d] [-v version(v1,v2)] [-c community] [-wc writeCommunity] [-p port] [-t timeout] [-r retries] MIB_files host [OID value] ...
 * e.g. For v1 request give -v v1 or drop the option -v .
 * java snmpset -p 161 -v v2 -c public ../../../mibs/RFC1213-MIB adventnet 1.7.0 76
 * 
 * v3 request:  
 * java snmpset [-d] [-v version(v1,v2,v3)] [-c community] [-p port] [-r retries] [-t timeout] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] MIB_File host [OID value] ...
 * e.g.
 * java snmpset  ../../../mibs/RFC1213-MIB -v v3 -u initial2 -w initial2Pass -a MD5 10.3.2.120 1.5.0 whatever
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
 * [-wc]<write community> - write community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-t] <Timeout>      - Timeout. By default 5000ms.
 * [-r] <Retries>      - Retries. By default 0. 
 * [-v] <version>      - version(v1 / v2 / v3). By default v1.
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-i] <contextName>  - The contextName to be used for the v3 pdu.
 * [-n] <contextID>    - The contextID to be used for the v3 pdu.
 * <MIBfile> Mandatory - MIB files.To load multiple mibs give within double quotes files seperated by a blank space.       
 * <host>  Mandatory   - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <OID>   Mandatory   - Object Identifier.
 * <value> Mandatory   - The object instance value to be set .
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.usm.*;

public class snmpset {

    private static final int DEBUG = 0;
    
    
    public static void main(String args[]) {
    // Take care of getting options



    
    String usage = 
        "\nsnmpset [-d] [-v version(v1,v2,v3)] [-c community]\n"+
        "[-wc writeCommunity] [-p port] [-r retries] [-t timeout]\n"+
        "[-u user] [-a auth_protocol] [-w auth_password] \n"+
        "[-s priv_password] [-n contextName] [-i contextID][-pp privProtocol (AES/DES)]\n"+
        "MIB_files host OID value [OID value] ...";

    String options[] = 
        { 
            "-d", "-c", "-wc", "-p", "-r", "-t","-m", "-v", "-u", "-a", "-w", "-s", "-n", "-i","-pp"
        };

    String values[] = 
        {
            "None", null, null, null, null, null, null, null,null, null, null, null, null, null,null 
        };

    ParseOptions opt = new ParseOptions(args,options,values,usage);
    if (opt.remArgs.length<4) opt.usage_error();

    // Start SNMP API
    SnmpAPI api;
    api = new SnmpAPI();
    if (values[DEBUG].equals("Set"))
    {
        api.setDebug( true );
    }   
    
    // Open session
    SnmpSession session = new SnmpSession(api);
 
   
   int PORT = 3;
        UDPProtocolOptions ses_opt = new UDPProtocolOptions();
        ses_opt.setRemoteHost(opt.remArgs[1]);
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
   

    // set values
    SetValues setVal = new SetValues(session, values);
    if(setVal.usage_error)
    {
        opt.usage_error();
    }   

    // Loading MIBS
    MibOperations mibOps = new MibOperations();
     
    // To load MIBs from compiled file
     mibOps.setLoadFromCompiledMibs(true);

    if (opt.remArgs[0] != null)
    {
        try
        {
            System.err.println("Loading MIBs: "+opt.remArgs[0]);
            mibOps.loadMibModules(opt.remArgs[0]);
        }
        catch (Exception ex) 
        {
            System.err.println("Error loading MIBs: "+ex.getMessage());
            System.exit(1);
        }
    }

    // Build set request PDU 
    SnmpPDU pdu = new SnmpPDU();
    pdu.setCommand( api.SET_REQ_MSG );
   
    // add Variable Bindings
    for (int i=2;i<opt.remArgs.length;) 
    { 
        if (opt.remArgs.length < i+2)
        {
            opt.usage_error(); //need "{OID value}"
        }   
        SnmpOID oid = mibOps.getSnmpOID(opt.remArgs[i++]);
        if (oid == null) 
        {
            System.exit(1);
        }   
        else
        {
            // Get the MibNode for this SnmpOID instance if found 
            MibNode node = mibOps.getMibNode(oid);
            if (node == null) 
            {
                System.err.println("Failed. MIB node unavailable for OID.");
            }   
            else
            {
                // Get the syntax associated with this node
                if (node.getSyntax() == null) 
                {
                    System.err.println("Failed. OID not a leaf node or.");
                }   
                else
                {
                    SnmpVarBind varbind = null;
                    try 
                    {
                        // Get the SnmpVar instance for the value
                        SnmpVar var = node.getSyntax().createVariable(opt.remArgs[i++]);
                        // Create SnmpVarBind instance 
                        varbind = new SnmpVarBind(oid,var);
                    } 
                    catch (SnmpException ex) 
                    { 
                        System.err.println("Error creating variable."); 
                    }
                    // Add the variable-bindings to the PDU
                    pdu.addVariableBinding(varbind);      
                }
            }
        }

    } // end of add variable bindings

    try 
    {
        session.open();        // Send PDU
    } 
    catch (SnmpException e) 
    { 
        System.err.println("Error opening seesion");
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
    catch (SnmpException e) 
    {
        System.err.println("Sending PDU"+e.getMessage());
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
