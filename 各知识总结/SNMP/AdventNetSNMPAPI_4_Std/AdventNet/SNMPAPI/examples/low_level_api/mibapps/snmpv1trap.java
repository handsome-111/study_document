/* $Id: snmpv1trap.java,v 1.4.2.5 2004/04/07 13:49:54 amaresh Exp $ */
/*
 * @(#)snmpv1trap.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details. 
 */

/**
 * This is an example program to explain how to write an application to send a
 * v1 Trap message using com.adventnet.snmp.snmp2 and com.adventnet.snmp.mib
 * packages of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv1trap [options] [-m mibfile] hostname enterprise-oid agent-addr generic-trap specific-trap 
 *      timeticks [OID value] ...
 *
 * java snmpv1trap [-d] [-c community] [-p port] [-m MIB_files] host enterprise agent-addr generic-trap specific-trap timeticks [OID value] ...
 * e.g. 
 * java snmpv1trap -p 162 -c public [-m ../../../mibs/RFC1213-MIB] adventnet 1.2.0 adventnet 0 6 1000 1.5.0 advent
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
 * [-d] option     Debug output. By default off.
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
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.mibs.*;

public class snmpv1trap
{
    public static void main(String args[])
    {
        SnmpAPI api;
        // Take care of getting options
        String usage =
        "\nsnmpv1trap [-d] [-c community] [-p port] [-m MIB_files] \n" +
        "host enterprise agent-addr generic-trap specific-trap \n" +
        "timeticks [OID value] ..."; 

        String options[] = { "-d", "-c", "-p" , "-m"};
        String values[] = { "None", null, null, null};

        ParseOptions opt = new ParseOptions(args,options,values, usage);
        if (opt.remArgs.length<6) opt.usage_error();

        // Start SNMP API
        api = new SnmpAPI();
        if (values[0].equals("Set")) api.setDebug( true );

        // create a UDPProtocolOptions object
        UDPProtocolOptions udpOpt = null;
        try
        {
            if(values[2] != null)
            {
                udpOpt = new UDPProtocolOptions(opt.remArgs[0],Integer.parseInt(values[2]));
								
            }
            else
            {
                udpOpt = new UDPProtocolOptions(opt.remArgs[0], 162);
            }
        }
        catch(Exception exp)
        {
            System.err.println("Invalid port: " + values[2]);
            System.exit(1);
        }


        // Loading MIBS - For addtional mibs to load please modify this by yourself.
        MibOperations mibOps = new MibOperations();

        //To load MIBs from compiled file
        mibOps.setLoadFromCompiledMibs(true);

        if (values[3] != null)
        {
            try
            {
                System.err.println("Loading MIBs: "+values[3]);
                mibOps.loadMibModules(values[3]);
            }
            catch (Exception ex)
            {
                System.err.println("Error loading MIBs: "+ex.getMessage());
                System.exit(1);
            }
        }

        // Open session
        SnmpSession session = new SnmpSession(api);


        // set the protocolOptions on the session.
        session.setProtocolOptions(udpOpt);


        // set community
        if (values[1] != null) session.setCommunity( values[1] );

        // Build SNMPv1 Trap PDU
        SnmpPDU pdu = new SnmpPDU();
        pdu.setCommand( api.TRP_REQ_MSG );

        // fill in v1 trap PDU fields
        try
        {
            // set enterprise OID   
            pdu.setEnterprise(mibOps.getSnmpOID(opt.remArgs[1]));
            // set agent address
            pdu.setAgentAddress(InetAddress.getByName(opt.remArgs[2]));
            // set generic trap type
            pdu.setTrapType(Integer.parseInt(opt.remArgs[3]));
            // set specific code
            if (opt.remArgs.length>4) 
            {
                pdu.setSpecificType(Integer.parseInt(opt.remArgs[4]));
            }
            // set time-stamp
            if (opt.remArgs.length>5)
            {
                pdu.setUpTime(Integer.parseInt(opt.remArgs[5]));
            }
        }
        catch (Exception ex)
        { 
                System.err.println("error in one or more required fields: "+ex);
                opt.usage_error();
        }

        // add Variable Bindings
        for (int i=6;i<opt.remArgs.length;)
        { 
            if (opt.remArgs.length < i+2) opt.usage_error(); //need "{OID value}"
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
                    System.err.println("Failed. MIB node unavailable for OID:"+ oid);
                    System.exit(1);
                }
                else
                {
                    // Get the syntax associated with this node
                    if (node.getSyntax() == null) 
                    {
                            System.err.println("Failed. OID not a leaf node .");
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
                            System.exit(1);
                        }
                        // Add the variable-bindings to the PDU
                        pdu.addVariableBinding(varbind);      
                    }
                }
            }
        } // end of add variable bindings

        try
        { 
            // open session
            session.open(); 
            // Send Trap PDU
            session.send(pdu); 
        }
        catch (SnmpException e)
        {
            System.err.println("Sending PDU : " + e.getMessage());
            System.exit(1);
        }

        // close session
        session.close();
        // stop api thread
        api.close();
    }
}
