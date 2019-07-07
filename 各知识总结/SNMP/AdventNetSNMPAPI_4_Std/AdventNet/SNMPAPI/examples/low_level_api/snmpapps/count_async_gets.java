/* $Id: count_async_gets.src,v 1.4.2.3 2004/03/17 10:05:30 vivek Exp $ */
/*
 * @(#)count_async_gets.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to do
 * the basic SNMP operation GET using com.adventnet.snmp.snmp2 package of
 * AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java count_async_gets [options] hostname oid ...
 *
 * v1 request:
 * java count_async_gets [-d] [-c community] [-p port] host OID [OID] ...
 * e.g. 
 * java count_async_gets -p 161 -c public adventnet 1.1.0 1.2.0
 *
 * v2c request:
 * java count_async_gets [-d] [-v version(v1,v2)] [-c community] [-p port] host OID [OID] ...
 * e.g. For v1 request give -v v1 or drop the option -v .
 * java count_async_gets -p 161 -v v2 -c public adventnet 1.1.0 1.2.0
 * 
 * v3 request:
 * java count_async_gets [-d] [-v version(v1,v2,v3)] [-c community] [-p port] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-i contextName] host OID [OID] ...
 * e.g.
 * java count_async_gets -v v3 -u initial2 -w initial2Pass -a MD5 10.3.2.120 1.2.0
 * 
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 *
 * Options:
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
 * [-n] <contextName>  - The contextName to be used for the v3 pdu.
 * [-i] <contextID>    - The contextID to be used for the v3 pdu.
 * host Mandatory      - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * OID  Mandatory      - Give multiple no. of Object Identifiers.
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;

public class count_async_gets implements SnmpClient
{
    public static void main(String args[])
    {
        // Take care of getting options
        String usage = "count_async_gets [-d] [-v version(v1,v2,v3)] [-c community] [-p port] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] host OID [OID] ...";
        String options[] = { "-d", "-c", "-p", "-v", "-u", "-a", "-w", "-s", "-n", "-i"};
        String values[] = { "None", null, null, null, null, null, "None", null, null, null, null, null, null, null };       

        ParseOptions opt = new ParseOptions(args,options,values, usage);
        if (opt.remArgs.length<2)
        {
            opt.usage_error();
        }

        // Start SNMP API
        SnmpAPI api = new SnmpAPI();
        if (values[0].equals("Set"))
        {
            api.setDebug( true );
        }

        // Open session and set remote host & port if needed,
        SnmpSession session = new SnmpSession(api);
        
        int PORT = 3;
                
                SnmpPDU pdu = new SnmpPDU();
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
        pdu.setProtocolOptions(ses_opt);

        SetValues setVal = new SetValues( session, values ); 
        if(setVal.usage_error)
        {
            opt.usage_error();
        }
       
        
        // Build get request PDU
        // SnmpPDU pdu = new SnmpPDU();
        pdu.setCommand( SnmpAPI.GET_REQ_MSG );

        //add OIDs
        for (int i=1;i<opt.remArgs.length;i++)
        {
            SnmpOID oid = new SnmpOID(opt.remArgs[i]);
            if (oid.toValue() == null)
            { 
                System.err.println("Invalid OID argument: " + opt.remArgs[i]);
                System.exit(1);
            }
            else
            {
                pdu.addNull(oid);
            }
        }
    
        try
        {
            count_async_gets rec = new count_async_gets();
            rec.rlastd = System.currentTimeMillis();
            session.addSnmpClient( rec );
            session.open();
        }
        catch (SnmpException e)
        {
            System.err.println("Open session: "+e.getMessage());
            System.exit(1);
        }
    
        if(session.getVersion()==SnmpAPI.SNMP_VERSION_3)
        {
            pdu.setUserName(setVal.userName.getBytes());
            try
            {
                USMUtils.init_v3_parameters(
                    setVal.userName,
                    setVal.authProtocol,
                    setVal.authPassword,
                    setVal.privPassword,
                    ses_opt.getRemoteHost(),
                    ses_opt.getRemotePort(),
                    session);
            }
            catch(Exception exp)
            {
                System.out.println(exp.getMessage());
                System.exit(1);
            }
            pdu.setContextName(setVal.contextName.getBytes());
            pdu.setContextID(setVal.contextID.getBytes());
        }
        int count=0;
        long lastd = System.currentTimeMillis();
        while (true)
        {
            try
            {
                session.send(pdu);
                if (++count >= 1000)
                {
                    long date = System.currentTimeMillis();
                    System.out.println("1,000 Requests Sent at: "
                    + (count*1000/(date-lastd)) + " per second");
                    count = 0;
                    lastd = date;        
                }
            }
            catch (SnmpException e)
            {
                System.err.println("Sending PDU"+e.getMessage());
                System.exit(1);
            }
        }
    }

    int rcount=0;
    long rlastd;

/** The callback for incoming PDUs */
    public boolean callback(SnmpSession session, SnmpPDU npdu, int reqid)
    {
        if (npdu == null)  // timeout
        {
            return true;
        }

        if(npdu.getVersion() == SnmpAPI.SNMP_VERSION_1)
        {
            if (npdu.getErrstat() == 0)
            {
                rcount++;
            }
            else
            {
                System.out.println("Error Indication in response: " +
                SnmpException.exceptionString((byte)npdu.getErrstat()) + 
                "\nErrindex: " + npdu.getErrindex());
            }
        }
        else if((npdu.getVersion() == SnmpAPI.SNMP_VERSION_2C) || (npdu.getVersion() == SnmpAPI.SNMP_VERSION_3))
        {
            if (npdu.getErrstat() != 0) 
            {
                System.out.println("Error Indication in response: " +
                SnmpException.exceptionString((byte)npdu.getErrstat()) + 
                "\nErrindex: " + npdu.getErrindex());
            }
            else
            {
                rcount++;
            }

        }
        else
        {
            System.out.println("Invalid Version Number");
        }
        if (rcount >= 1000)
        {
            long date = System.currentTimeMillis();
            System.out.println("1,000 Requests received at: "
                        + (rcount*1000/(date-rlastd)) + " per second");
            rcount = 0;
            rlastd = date;
        }
        return true;
    }

    /** We need to implement the other methods in the SnmpClient interface */
    public void debugPrint(String s)
    {
        System.err.println(s);
    }
  
    public boolean authenticate(SnmpPDU pdu, String community)
    {
        return (pdu.getCommunity().equals(community));
    }
}
