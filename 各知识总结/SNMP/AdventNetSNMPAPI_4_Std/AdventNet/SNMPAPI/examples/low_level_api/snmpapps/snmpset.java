/* $Id: snmpset.src,v 1.6.2.4 2004/10/18 10:19:57 amaresh Exp $ */
/*
 * @(#)snmpset.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to do
 * the basic SNMP operation SET using com.adventnet.snmp.snmp2 package of
 * AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpset [options] hostname oid value [oid value] ...
 *
 * v1 request: 
 * java snmpset [-d] [-c community] [-wc writeCommunity] [-p port] [-t timeout] [-r retries] host [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER | OID } value] ...
 * e.g. 
 * java snmpset -p 161 -c public adventnet 1.1.0 advent-machine 1.4.0 contact-advent
 *
 * v2c request:  
 * java snmpset [-d] [-v version(v1,v2)] [-c community] [-wc writeCommunity] [-p port] [-t timeout] [-r retries] host [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER | OID } value] ...
 * e.g. For v1 request give -v v1 or drop the option -v .
 * java snmpset -p 161 -v v2 -c public adventnet 1.7.0 76
 * 
 * v3 request:
 * java snmpset [-d] [-v version(v1,v2,v3)] [-c community] [-p port] [-r retries] [-t timeout] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-i context_id] host [OID {INTEGER | STRING | GAUGE | TIMETICKS | OPAQUE | IPADDRESS | COUNTER | OID } value] ...
 * e.g.
 * java snmpset -v v3 -u initial2 -w initial2Pass -a MD5 10.3.2.120 1.5.0 STRING whatever
 * 
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 *
 * Options:
 * [-d]                - Debug output. By default off.
 * [-c] <community>    - community String. By default "public".
 * [-wc] <community>   - write community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-t] <Timeout>      - Timeout. By default 5000ms.
 * [-r] <Retries>      - Retries. By default 0.      
 * [-v] <version>      - version(v1 / v2 / v3). By default v1.
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-pp]<privProtocol  - The privProtocol(AES/DES)
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>  - The contextName to be used for the v3 pdu.
 * [-i] <contextID>    - The contextID to be used for the v3 pdu.
 * <host> Mandatory    - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <OID>  Mandatory    - Give multiple no. of Object Identifiers with type and value.
 * <type> Mandatory    - object type
 * <value> Mandatory   - The object instance value to be set .
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;

 
public class snmpset
{
    public static void main(String args[])
    {
        // Take care of getting options
        String usage =
            "\nsnmpset [-d] [-v version(v1,v2,v3)] [-c community] \n" +
            "[-wc writeCommunity] [-p port] [-r retries] \n" +
            "[-t timeout] [-u user] [-a auth_protocol] \n" +
            "[-w auth_password] [-s priv_password] \n" +
            "[-n contextName] [-i contextID] \n" +
            "[-DB_driver database_driver]\n" +
            "[-DB_url database_url]\n" +
            "[-DB_username database_username]\n" +
            "[-DB_password database_password]\n" +
						"[-pp privProtocol(AES/DES)]\n" +
            "host [OID {INTEGER | STRING | GAUGE | TIMETICKS | \n" +
            "OPAQUE | IPADDRESS | COUNTER | OID } value] ...\n"; 
            
        
        String options[] =
        {
            "-d", "-c", "-wc", "-p", "-r", "-t", "-m",
            "-v", "-u", "-a", "-w", "-s", "-n", "-i",
            "-DB_driver", "-DB_url", "-DB_username", "-DB_password","-pp"
        };
        
        String values[] =
        {
            "None", null, null, null, null, null, "None",
            null, null, null, null, null, null, null,
            null, null, null, null,null
        };


        ParseOptions opt = new ParseOptions(args,options,values,usage);
        if (opt.remArgs.length<1)
        {
            opt.usage_error();
        }

        // Start SNMP API
        SnmpAPI api;
        api = new SnmpAPI();
        if (values[0].equals("Set")) api.setDebug( true );

        // Open session 
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

        // set values
        SetValues setVal = new SetValues(session, values);
        if(setVal.usage_error)
        {
            opt.usage_error();
        }


        String driver = values[14];
        String url = values[15];
        String username = values[16];
        String password = values[17];
        if(driver != null || url != null ||
            username != null || password != null)
        {
            if(session.getVersion() != 3)
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
            try
            {
                api.setV3DatabaseFlag(true);
                api.initJdbcParams(driver, url, username, password);
            }
            catch(Exception exp)
            {
                System.out.println("Unable to Establish Database Connection.");
                System.out.println("Please check the driverName and url.");
                System.exit(1);
            }
        }

        // Build set request PDU
       // SnmpPDU pdu = new SnmpPDU();
        pdu.setCommand( api.SET_REQ_MSG );

        // add Variable Bindings
        for (int i=1;i<opt.remArgs.length;)
        {
            if (opt.remArgs.length < i+3)
            {
                opt.usage_error(); //need "{OID type value}"
            }
            SnmpOID oid = new SnmpOID(opt.remArgs[i++]);
            if (oid.toValue() == null)
            {
                System.err.println("Invalid OID argument: " + opt.remArgs[i]);
            }
            else
            {
                addvarbind(pdu, oid, opt.remArgs[i++], opt.remArgs[i++]);
            }
        } // end of add variable bindings

        try
        {
            //open session
            session.open();
        }
        catch (SnmpException e)
        {
            System.err.println("Sending PDU"+e.getMessage());
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
            catch(SnmpException exp)
            {
                System.out.println(exp.getMessage());
                System.exit(1);
            }
            pdu.setContextName(setVal.contextName.getBytes());
            pdu.setContextID(setVal.contextID.getBytes());
        }

        SnmpPDU res_pdu = null;

        try // Send PDU receive response PDU
        {
            res_pdu = session.syncSend(pdu); 
        }
        catch (SnmpException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (res_pdu == null)
        {
            // timeout
            System.out.println("Request timed out to: " + opt.remArgs[0] );
            System.exit(1);
        }

        String res = "Response PDU received from " + res_pdu.getProtocolOptions().getSessionId() + ".";
        if(res_pdu.getVersion() < 3)
        {
            res = res + " Community = " + res_pdu.getCommunity() + ".";
        }
        System.out.println(res);
    
        // Check for error in response
        if (res_pdu.getErrstat() != 0)
        {
            System.err.println(res_pdu.getError());
        }
        else
        {
            // print the response pdu varbinds
            System.out.println(res_pdu.printVarBinds());
        }

        // close session
        session.close();
        // stop api thread
        api.close();
    }

    /** adds the varbind  with specified oid, type and value to the pdu */
    static void addvarbind(SnmpPDU pdu, SnmpOID oid, String type, String value)
    {
        byte dataType ;
        if (type.equals("INTEGER"))
        {
            dataType = SnmpAPI.INTEGER;
        }
        else if (type.equals("STRING"))
        {
            dataType = SnmpAPI.STRING;
        }
        else if (type.equals("GAUGE"))
        {
            dataType = SnmpAPI.GAUGE;
        }
        else if (type.equals("TIMETICKS"))
        {
            dataType = SnmpAPI.TIMETICKS;
        }
        else if (type.equals("OPAQUE"))
        {
            dataType = SnmpAPI.OPAQUE;
        }
        else if (type.equals("IPADDRESS"))
        {
            dataType = SnmpAPI.IPADDRESS;
        }
        else if (type.equals("COUNTER"))
        {
            dataType = SnmpAPI.COUNTER;
        }
        else if (type.equals("OID"))
        { 
            dataType = SnmpAPI.OBJID;
        }
        else if (type.equals("BITS"))
        {
            dataType = SnmpAPI.STRING;
        }
        else
        {
            System.err.println("Invalid variable type: " + type);
            return;
        }

        SnmpVar var = null;
        try
        {
            // create SnmpVar instance for the value and the type
            var = SnmpVar.createVariable( value, dataType );
        }
        catch(SnmpException e)
        {
            System.err.println("Cannot create variable: " + oid + " with value: " + value);
            return;
        }
        //create varbind
        SnmpVarBind varbind = new SnmpVarBind(oid, var);
        // add variable binding
        pdu.addVariableBinding(varbind);
    }
}
