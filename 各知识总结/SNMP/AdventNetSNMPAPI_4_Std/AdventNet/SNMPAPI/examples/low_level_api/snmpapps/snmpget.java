/* $Id: snmpget.src,v 1.10.6.1 2004/10/18 10:48:23 amaresh Exp $ */
/*
 * @(#)snmpget.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to do
 * the basic SNMP operation GET using com.adventnet.snmp.snmp2 package of
 * AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpget [options] hostname oid ...
 *
 * v1 request:
 * java snmpget [-d] [-c community] [-p port] [-t timeout] [-r retries] host OID [OID] ...
 * e.g. 
 * java snmpget -p 161 -c public adventnet 1.1.0 1.2.0
 *
 * v2c request:
 * java snmpget [-d] [-v version(v1,v2)] [-c community] [-p port] [-t timeout] [-r retries] host OID [OID] ...
 * e.g. For v1 request give -v v1 or drop the option -v .
 * java snmpget -p 161 -v v2 -c public adventnet 1.1.0 1.2.0
 * 
 * v3 request:
 * java snmpget [-d] [-v version(v1,v2,v3)] [-c community] [-p port] [-r retries] [-t timeout] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-i context_id] host OID [OID] ...
 * e.g.
 * java snmpget -v v3 -u initial2 -w initial2Pass -a MD5 10.3.2.120 1.2.0
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

public class snmpget
{

	public static void main(String args[])
	{
		// Take care of getting options
		String usage =
			"\nsnmpget [-d] [-v version(v1,v2,v3)] [-c community] \n" +
			"[-p port] [-r retries] [-t timeout] [-u user] \n" +
			"[-a auth_protocol] [-w auth_password] \n" +
			"[-s priv_password] [-n contextName] [-i contextID] \n" +
			"[-DB_driver database_driver]\n" +
			"[-DB_url database_url]\n" +
			"[-DB_username database_username]\n" +
			"[-DB_password database_password]\n" +
			"[-pp privProtocol(AES/DES)]\n" +
			"host OID [OID] ...\n";
        
		String options[] =
		{
			"-d", "-c",  "-wc", "-p", "-r", "-t", "-m",
			"-v", "-u", "-a", "-w", "-s", "-n", "-i",
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

		// Start SNMP API
		SnmpAPI api;
		api = new SnmpAPI();
		if (values[0].equals("Set"))
		{
			api.setDebug( true );
		}

		// Open session
		SnmpSession session = new SnmpSession(api);        
		// set remote Host 
		
		int PORT = 3;
		
		SnmpPDU pdu = new SnmpPDU();
		UDPProtocolOptions udpOpt = new UDPProtocolOptions();
		udpOpt.setRemoteHost(opt.remArgs[0]);
		if(values[PORT] != null)
		{
			try
			{
				udpOpt.setRemotePort(Integer.parseInt(values[PORT]));
			}
			catch(Exception exp)
			{
				System.out.println("Invalid port: " + values[PORT]);
				System.exit(1);
			}
		}
		pdu.setProtocolOptions(udpOpt);

		SetValues setVal = new SetValues( pdu, values ); 
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
			if(pdu.getVersion() != 3)
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

		// Build Get request PDU
	//	SnmpPDU pdu = new SnmpPDU();
		pdu.setCommand( api.GET_REQ_MSG );

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
			session.open();
		}
		catch (SnmpException e)
		{
			System.err.println("Error opening session:"+e.getMessage());
			System.exit(1);
		}

		if(pdu.getVersion()==SnmpAPI.SNMP_VERSION_3)
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
					udpOpt,
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
		
		try
		{
			// Send PDU and receive response PDU
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

		UDPProtocolOptions udpOptions =
			(UDPProtocolOptions)res_pdu.getProtocolOptions();
		String res = "Response PDU received from " +
			udpOptions.getRemoteAddress() + ".";
		// print and exit
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

}
