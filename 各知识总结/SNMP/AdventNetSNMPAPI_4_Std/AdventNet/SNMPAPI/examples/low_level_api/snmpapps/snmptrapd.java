/* $Id: snmptrapd.src,v 1.4.2.2 2004/03/17 10:05:30 vivek Exp $ */
/*
 * @(#)snmptrapd.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program for receiving traps using the
 * com.adventnet.snmp.snmp2 package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmptrapd [options]
 *
 * java snmptrapd [-d] [-p port][-c community]
 * e.g. 
 * java snmptrapd -p 162 -c public 
 *
 * Options:
 * [-d]                - Debug output. By default off.
 * [-p] <port>         - remote port no. By default 162.
 * [-c] <community>    - community String. By default "public".               
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;

public class snmptrapd implements SnmpClient
{
    static SnmpAPI api;
    
    public static void main(String args[])
    {
        // Take care of getting options
        String usage = "snmptrapd [-d] [-p port][-c community]";
        String options[] =
        {
            "-d", "-p", "-c"
        };
        String values[] =
        {
            "None", null, null
        };

        ParseOptions opt = new ParseOptions(args,options,values, usage);

        // Start SNMP API
        api = new SnmpAPI();
        if (values[0].equals("Set"))
        {
            api.setDebug( true );
        }

        if (opt.remArgs.length>0) opt.usage_error();

        // Open session 
        SnmpSession session = new SnmpSession(api);
        session.addSnmpClient(new snmptrapd());
        
        // set local port
        try
        {
            UDPProtocolOptions ses_opt = new UDPProtocolOptions();
            if (values[1] != null)
            {
                ses_opt.setLocalPort( Integer.parseInt(values[1]) );
            }
            else 
            {
                ses_opt.setLocalPort(162);
            }
            session.setProtocolOptions(ses_opt);
        }
        catch (NumberFormatException ex)
        {
            System.err.println("Invalid local port: " + values[1] );
            System.exit(1);
        }

        // set community
        if(values[2] != null)
        {
            session.setCommunity(values[2]);
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
        // check trap version
        if(pdu == null)
        {
            return false;
        }
        if (pdu.getCommand() == api.TRP_REQ_MSG)
        {
            System.out.print("Trap received from: ");
            System.out.print(pdu.getProtocolOptions().getSessionId());
            System.out.println(", community: " + pdu.getCommunity());
            System.out.println("Enterprise: " + pdu.getEnterprise());
            System.out.println("Agent: " + pdu.getAgentAddress().getHostAddress());
            System.out.println("TRAP_TYPE: " + pdu.getTrapType());
            System.out.println("SPECIFIC NUMBER: " + pdu.getSpecificType());
            System.out.println("Time: " + pdu.getUpTime()+"\nVARBINDS:");
            // print varbinds
            Enumeration e = pdu.getVariableBindings().elements();
            while(e.hasMoreElements())
            {
                System.out.println(((SnmpVarBind) e.nextElement()).toTagString());
            }
        }
        else if(pdu.getCommand() == api.TRP2_REQ_MSG)
        {
            System.out.print("Trap received from: ");
            System.out.print(pdu.getProtocolOptions().getSessionId());
            System.out.println(", community: " + pdu.getCommunity());
            Enumeration e = pdu.getVariableBindings().elements();
            while(e.hasMoreElements())
            {
                System.out.println(((SnmpVarBind) e.nextElement()).toTagString());
            }
        }
        else
        {
            System.err.println("Non trap PDU received.");
        }
        System.out.println(""); // a blank line between traps
        return true;
    }

    public void debugPrint(String debugOutput)
    {
        System.out.println(debugOutput);
        return;    
    }
}
