/* $Id: snmpasyncget.src,v 1.6.2.4 2004/10/18 10:20:55 amaresh Exp $ */
/*
 * @(#)snmpasyncget.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to do
 * an asynchronous SNMP GET operation using com.adventnet.snmp.snmp2 package of
 * AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpasyncget [options] hostname oid ...
 *
 * v1 request:
 * java snmpasyncget [-d] [-c community] [-p port] [-t timeout] [-r retries] host OID [OID] ...
 * e.g. 
 * java snmpasyncget -p 161 -c public adventnet 1.1.0 1.2.0
 *
 * v2c request:
 * java snmpasyncget [-d] [-v version(v1,v2)] [-c community] [-p port] [-t timeout] [-r retries] host OID [OID] ...
 * e.g. For v1 request give -v v1 or drop the option -v .
 * java snmpasyncget -p 161 -v v2 -c public adventnet 1.1.0 1.2.0
 * 
 * v3 request:
 * java snmpasyncget [-d] [-v version(v1,v2,v3)] [-c community] [-p port] [-r retries] [-t timeout] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-i context_id] host OID [OID] ...
 * e.g.
 * java snmpasyncget -v v3 -u initial2 -w initial2Pass -a MD5 10.3.2.120 1.2.0
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
 * [-pp]<privProtocol> - The privProtocol(DES/AES).
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

public class snmpasyncget implements SnmpClient
{
    SnmpAPI api=null;
    SnmpSession session =null; 
    boolean getFinished = false;

    public static void main(String args[])
    {
        // Take care of getting options
        String usage =
            "snmpasyncget [-d] [-v version(v1,v2,v3)] [-c community]\n" +
            "[-p port] [-r retries] [-t timeout] [-u user]\n" +
            "[-a auth_protocol] [-w auth_password] [-s priv_password]\n" +
            "[-n contextName] [-i contextID]\n" +
            "[-DB_driver database_driver]\n" +
            "[-DB_url database_url]\n" +
            "[-DB_username database_username]\n" +
            "[-DB_password database_password]\n" +
						"[-pp privProtocol]\n" +
						"host OID [OID] ...\n";
        String options[] = 
        {
            "-d", "-c",  "-wc", "-p", "-r", "-t", "-m", "-v",
            "-u", "-a", "-w", "-s", "-n", "-i", 
            "-DB_driver", "-DB_url", "-DB_username", "-DB_password", "-pp"
        };
        String values[] = 
        {
            "None", null, null, null, null, null, "None",
            null, null, null, null, null, null, null,
            null, null, null, null, null
        };

        ParseOptions opt = new ParseOptions(args,options,values, usage);
        if (opt.remArgs.length<2)
        {
            opt.usage_error();
        }
        String driver = values[14];
        String url = values[15];
        String username = values[16];
        String password = values[17];

        snmpasyncget snmpobj = new snmpasyncget ();

        // Start SNMP API
        snmpobj.api = new SnmpAPI();

        if (values[0].equals("Set"))
        {
            snmpobj.api.setDebug( true );
        }

        // Open session
        snmpobj.session = new SnmpSession(snmpobj.api);        

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

        SetValues setVal = new SetValues( snmpobj.session, values ); 


        if(driver != null || url != null ||
            username != null || password != null)
        {
            if(snmpobj.session.getVersion() != 3)
            {
                System.out.println(
                    "Database option can be used only for SNMPv3.");
                System.exit(1);
            }
            if(driver == null)
            {
                System.out.println(
                    "The Database driver name should be given.");
                System.exit(1);
            }
            if(url == null)
            {
                System.out.println("The Database URL should be given.");
                System.exit(1);
            }
            snmpobj.api.setV3DatabaseFlag(true);
            try
            {
                snmpobj.api.initJdbcParams(driver, url, username, password);
            }
            catch(Exception exp)
            {
                System.out.println("Unable to Establish Database Connection.\n" +
                    "Please check the driverName and url.");
                System.exit(1);
            }
        }


        snmpobj.session.addSnmpClient(snmpobj);

        if(setVal.usage_error)
        {
            opt.usage_error();
        }

        // Build Get request PDU
       // SnmpPDU pdu = new SnmpPDU();
        pdu.setCommand( snmpobj.api.GET_REQ_MSG );


        // add OIDs
        for (int i=1;i<opt.remArgs.length;i++)
        {
            SnmpOID oid = new SnmpOID(opt.remArgs[i]);
            if (oid.toValue() == null) 
            {
                System.err.println("Invalid OID argument: " + opt.remArgs[i]);
            }
            else
            {
                pdu.addNull(oid);
            }
        }

        try
        {
            //Open session
            snmpobj.session.open();
        }
        catch (SnmpException e)
        {
            System.err.println("Error opening session:"+e.getMessage());
            System.exit(1);
        }

        if(snmpobj.session.getVersion()==SnmpAPI.SNMP_VERSION_3)
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
                snmpobj.session,
								false,
								setVal.privProtocol);
            }
            catch(Exception exp)
            {
                System.out.println(exp.getMessage());
                System.exit(1);
            }
            pdu.setContextName(setVal.contextName.getBytes());
            pdu.setContextID(setVal.contextID.getBytes());
        }

        try
        {
            snmpobj.session.send(pdu);
        }
        catch (SnmpException e)
        {
        }
        while(true)
        {
            try
            {
                Thread.sleep(100);
            }
            catch(Exception e)
            {
            }
            if(snmpobj.getFinished)
            {
                System.exit(0);
            }
        }
    }
    
    public boolean authenticate(SnmpPDU pdu, String community)
    {
        if(pdu.getVersion() == 3)
        {
            return !((Snmp3Message)pdu.getMsg()).isAuthenticationFailed();
        }
        else
        {
            return (pdu.getCommunity().equals(community));
        }
    }
    
    private int totalResponses = 1;
    
    /*
     * Callback method that is invoked on receiving an SNMP Inform request message
     */
    public boolean callback(SnmpSession session,SnmpPDU pdu, int requestID)
    {
        // Check if valid PDU
        if (pdu == null)
        {
            System.out.println("Null PDU received :Timeout\n");
            getFinished = true;
        }
        else
        {
            String res = "Response received from: " + pdu.getProtocolOptions().getSessionId() + ".";
            if(pdu.getVersion() < 3)
            {
                res = res + " Community: " + pdu.getCommunity();
            }
            System.out.println(res);

            // print varbinds
            Enumeration e = pdu.getVariableBindings().elements();
            while(e.hasMoreElements())
            {
                System.out.println(
                    ((SnmpVarBind) e.nextElement()).toTagString());
            }
            if(pdu.isBroadCastEnabled())
            {
                System.out.println("This is the response for" +
                    " a broadCast request.");
                System.out.println("Total Responses received = " +
                    (totalResponses++) + ".");
            }
            else
            {
                getFinished = true;
            }
        }
        System.out.println(""); 
        return true;
    }
 
    public void debugPrint(String debugOutput)
    {
        System.out.println(debugOutput);
        return;    
    }
}
