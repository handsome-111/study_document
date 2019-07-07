/* $Id: snmpv2ctrap.src,v 1.4.2.2 2004/03/17 09:55:44 vivek Exp $ */
/*
 * @(#)snmpv2ctrap.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to send a
 * v2c Trap message using com.adventnet.snmp.snmp2 package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv2ctrap [options] -m mibfile hostname timeTicks snmpTrapOID-value [OID value] ...
 *
 * java snmpv2ctrap [-d] [-c community] [-p port] -m MIB_files host TimeTicksvalue OIDvalue [OID value] ...
 * e.g.
 * java snmpv2ctrap -m ../../../mibs/RFC1213-MIB 10.3.2.120 16352 coldStartTrap ifIndex.2 2
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
 * [-d]                  - Debug output. By default off.
 * [-c] <community>      - community String. By default "public".
 * [-p] <port>           - remote port no. By default 162.
 * -m   <MIBfile>        - MIB files.Mandatory.To load multiple mibs give within double quotes files seperated by a blank space.       
 * <host>      Mandatory - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <timeticks> Mandatory - the value of object sysUpTime when the event occurred
 * <OID-value> Mandatory - Object Identifier  
 * <OID>                 - Give multiple no. of Object Identifiers with value.
 * <value>               - object-instance value
 */
    
import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.mibs.*;

public class snmpv2ctrap 
{
    private static final int DEBUG = 0;
    private static final int COMMUNITY = 1;
    private static final int PORT = 2;
    private static final int MIBS = 3;

    public static void main(String args[]) 
    {
        SnmpAPI api;
            
        // Take care of getting options
        String usage = 
            "\nsnmpv2ctrap [-d] [-c community] [-p port] -m MIB_files \n"+
            "host TimeTicksvalue OIDvalue [OID value] ...";

        String options[] = { "-d", "-c", "-p", "-m"};

        String values[] = { "None", null, null, null};

        ParseOptions opt = new ParseOptions(args,options,values, usage);
        if (opt.remArgs.length<3)
        {
            opt.usage_error();
        }   
                     
        // Start SNMP API
        api = new SnmpAPI();
        if (values[DEBUG].equals("Set"))
        {
            api.setDebug( true );    
        }   
        
        // Open session 
        SnmpSession session = new SnmpSession(api);
        
        // set version 2c
        session.setVersion( SnmpAPI.SNMP_VERSION_2C ) ;
        
        // set remote Host
        UDPProtocolOptions ses_opt = new UDPProtocolOptions(opt.remArgs[0]);
        
        // set community
        if (values[COMMUNITY] != null)
        {
            session.setCommunity( values[COMMUNITY] );
        }   
    
        // set remote Port
        try 
        {
            if (values[PORT] != null) 
            {
                ses_opt.setRemotePort( Integer.parseInt(values[PORT]) );
            }   
            else 
            {
                ses_opt.setRemotePort(162);
            }   
        }
        catch (NumberFormatException ex) 
        {
            System.err.println("Invalid Integer Arg");
        }

        session.setProtocolOptions(ses_opt);
        
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
                System.err.println("Error loading MIBs: "+ex.getMessage());
                System.exit(1);
            }
        }
        
        // Build trap request PDU
        SnmpPDU pdu = new SnmpPDU();
        pdu.setCommand( api.TRP2_REQ_MSG );
    
        // Adding the sysUpTime variable binding 
        SnmpOID oid = mibOps.getSnmpOID(".1.3.6.1.2.1.1.3.0");
        if (oid == null) 
        {
            System.exit(1);
        }   
        else     
        {
            addVarBind(mibOps, pdu, oid, opt.remArgs[1]);
        }   

        // Adding the snmpTrapOID variable binding
         oid = mibOps.getSnmpOID(".1.3.6.1.6.3.1.1.4.1.0");

        if (oid == null) 
        {
            System.exit(1);
        }
        else 
        {
            addVarBind(mibOps, pdu, oid, opt.remArgs[2]);
        }   
    
        // add Variable Bindings
        for (int i=3;i<opt.remArgs.length;) 
        { 
            if (opt.remArgs.length < i+2)
            {
                opt.usage_error(); //need "{OID value}"
            }   

            oid = mibOps.getSnmpOID(opt.remArgs[i++]);
            if (oid == null) 
            {
                System.exit(1);
            }   
            else
            {
                addVarBind(mibOps, pdu, oid, opt.remArgs[i++]);
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
    
        System.exit(0);
    }


    /** adds the varbind  with specified oid and value */
    static void addVarBind( MibOperations mibOps, SnmpPDU pdu, SnmpOID oid, String value)
    {        
        // Get the MibNode for this SnmpOID instance if found 
        MibNode node = mibOps.getMibNode(oid);
        if (node == null) 
        {
            System.err.println("Failed. MIB node unavailable for OID:"+oid);
        }   
        else
        {
            // Get the syntax associated with this node
            if (node.getSyntax() == null) 
            {
                System.err.println("Failed. OID not a leaf node.");
            }       
            else
            {
                SnmpVarBind varbind = null;
                try
                {
                    // Get the SnmpVar instance for the value
                    SnmpVar var = node.getSyntax().createVariable(value);
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
  
}
