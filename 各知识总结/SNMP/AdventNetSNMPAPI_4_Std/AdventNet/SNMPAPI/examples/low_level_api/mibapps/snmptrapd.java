/* $Id: snmptrapd.src,v 1.5.2.2 2004/03/17 09:55:44 vivek Exp $ */
/*
 * @(#)snmptrapd.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program for receiving traps using the
 * com.adventnet.snmp.snmp2 and com.adventnet.snmp.mibs packages
 * of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmptrapd [options]
 *
 * java snmptrapd [-d] [-p port] [-m MIB_files]
 * e.g. 
 * java snmptrapd -p 162
 *
 * Options:
 * [-d]                - Debug output. By default off.
 * [-p] <port>         - remote port no. By default 162.
 * [-m] <MIBfile>      - MIB files.To load muliple mibs give within double quotes files seperated by a blank space. 
 */
        
import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.mibs.*;

public class snmptrapd implements SnmpClient 
{
    private static final int DEBUG = 0;
    private static final int PORT = 1;
    private static final int MIBS = 2;
    
    static SnmpAPI api;
    MibOperations mibOps = null;

    public snmptrapd(String mibs) throws Exception
    {
        mibOps = new MibOperations();
        if(mibs!=null)
        {
            // To load MIBs from compiled file
            mibOps.setLoadFromCompiledMibs(true);
            // Load MIBs
            mibOps.loadMibModules(mibs);
        }   
    }
    
    public static void main(String args[]) 
    {
        // Take care of getting options
        String usage = "snmptrapd [-d] [-p port] [-m MIB_files]";
        
        String options[] = 
            {
                "-d", "-p", "-m"
            };
            
        String values[] = 
            {
                "None", null, null
            };

        ParseOptions opt = new ParseOptions(args,options,values, usage);
        if (opt.remArgs.length > 0 ) 
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
        snmptrapd recvTrap = null;
    
        try
        {
            recvTrap = new snmptrapd(values[MIBS]);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        
        session.addSnmpClient(recvTrap);
        // set local port
        try 
        {
            UDPProtocolOptions ses_opt = new UDPProtocolOptions();
            if (values[PORT] != null)
            {
                ses_opt.setLocalPort( Integer.parseInt(values[PORT]) );        
            }   
            else 
            {
                ses_opt.setLocalPort(162);
            }
            session.setProtocolOptions(ses_opt);
        }
        catch (NumberFormatException ex) 
        {
            System.err.println("Invalid Integer Arg");
        }

        // Open the session
        try 
        { 
            session.open();
        }
        catch (SnmpException e) 
        {
            System.err.println(e);
            System.exit(1);
        }
    }

    public boolean authenticate(SnmpPDU pdu, String community)
    {
        return (pdu.getCommunity().equals(community));
    }

    public boolean callback(SnmpSession session,SnmpPDU pdu, int requestID)
    {
        if(pdu == null)
        {
            return false;
        }   
        // Check for trap version
        if (pdu.getCommand() == api.TRP_REQ_MSG) 
        {
            System.out.println("Trap received from: ");
            System.out.println(pdu.getProtocolOptions().getSessionId());
            System.out.println(", community: " + pdu.getCommunity());
            System.out.println("Enterprise: " + pdu.getEnterprise());
            System.out.println("Agent: " + pdu.getAgentAddress());
            System.out.println("TRAP_TYPE: " + pdu.getTrapType());
            System.out.println("SPECIFIC NUMBER: " + pdu.getSpecificType());
            System.out.println("Time: " + pdu.getUpTime()+"\nVARBINDS:");
            // print varbinds
            System.out.println(mibOps.varBindsToString(pdu));
        }
        else if(pdu.getCommand() == api.TRP2_REQ_MSG)
        {
            System.out.println("Trap received from: ");
            System.out.println(pdu.getProtocolOptions().getSessionId());
            System.out.println(", community: " + pdu.getCommunity());
            System.out.println(mibOps.varBindsToString(pdu));
        }
        else
        {
            System.err.println("Non trap PDU received.");
        }           
        System.out.println(""); // a blank line between traps
        return true;
    } // end for listen for ever

    public void debugPrint(String debugOutput)
    {
        System.out.println(debugOutput);
        return;
    }
}
