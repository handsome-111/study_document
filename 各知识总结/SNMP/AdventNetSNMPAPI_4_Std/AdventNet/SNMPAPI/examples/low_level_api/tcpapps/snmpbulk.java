/* $Id: snmpbulk.src,v 1.4.2.3 2006/07/10 16:31:39 rravikumar Exp $ */
/*
 * @(#)snmpbulk.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to do
 * the basic SNMP operation GETBULK using com.adventnet.snmp.snmp2 package of
 * AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * Note: The values of non-repeaters max-repetitions should be the last arguments
 *  
 * java snmpbulk [options] hostname oid [oid] ... non-repeaters max-repetitions
 *
 * v2c request:
 * java snmpbulk [-d] [-c community] [-p port] [-t timeout] [-r retries] host OID [OID] ... nonRepeaters maxRepetions
 * e.g. 
 * java snmpbulk -p 161 -c public adventnet 1.2.0 1.2.0 1.5.0 1 2
 *
 * v3 request:
 * java snmpbulk [-d] [-v version(v2,v3)] [-c community] [-p port] [-r retries] [-t timeout] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-i contextName] host OID [OID] ... nonRepeaters maxRepetions
 * e.g.
 * java snmpbulk -v v3 -c public -p 161 -u advent -a MD5 -w authPass localhost 1.2.0 1.3.0 1.4.0 1 2
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
 * [-v] <version>      - version(v2 / v3).
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>  - The contextName to be used for the v3 pdu.
 * [-i] <contextID>    - The contextID to be used for the v3 pdu.
 * <host> Mandatory    - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <OID>  Mandatory    - Give only one Object Identifier.
 * <nonRepeaters>      - non-repeaters. Mandatory
 * <maxRepetions>      - max-repetitions. Mandatory
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;

public class snmpbulk {


    public static void main(String args[]) {
        
        // Take care of getting options
        String usage = "snmpbulk [-d] [-v version(v2,v3)] [-c community] [-p port] [-r retries] [-t timeout] [-u user] [-a auth_protocol] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] [-pp privProtocol (AES/DES)]host OID [OID] ... nonRepeaters maxRepetions";
        String options[] = { "-d", "-c",  "-wc", "-p", "-r", "-t", "-m", "-v", "-u", "-a", "-w", "-s", "-n", "-i" ,"-pp"};
        String values[] = { "None", null, null, null, null, null, "None", null, null, null, null, null, null, null,null };
        ParseOptions opt = new ParseOptions(args,options,values, usage);
        
        // Start SNMP API
        SnmpAPI api;
        api = new SnmpAPI();
        if (values[0].equals("Set")) api.setDebug( true );        

        if (opt.remArgs.length<4) opt.usage_error();
        
        // Open session
        SnmpSession session = new SnmpSession(api);
        session.setTransportProvider("com.adventnet.snmp.snmp2.TcpTransportImpl");
        
        ProtocolOptions params = null;
        if(values[3] != null)   {
            params = new TcpProtocolOptionsImpl(opt.remArgs[0], Integer.parseInt( values[3] ), -1);
        }
        else    {
            params = new TcpProtocolOptionsImpl(opt.remArgs[0],161, -1);
        }
        session.setProtocolOptions(params);


        //set the values
        SetValues setVal = new SetValues(session, values);
        if(setVal.usage_error) opt.usage_error();

        // set version for default
        if(values[7] == null)
            session.setVersion(SnmpAPI.SNMP_VERSION_2C);

        if(session.getVersion()==SnmpAPI.SNMP_VERSION_1)
            opt.usage_error();

        // Build GetBulk request PDU
        SnmpPDU pdu = new SnmpPDU();
        pdu.setCommand( api.GETBULK_REQ_MSG );

        // add OIDs
        for (int i=1;i<(opt.remArgs.length)-2;i++) {             
            SnmpOID oid = new SnmpOID(opt.remArgs[i]);
            if (oid.toValue() == null) 
                System.err.println("Invalid OID argument: " + opt.remArgs[i]);
            else 
                pdu.addNull(oid);
        }

    try {
        // set non-repeaters
        pdu.setErrstat( Integer.parseInt(opt.remArgs[(opt.remArgs.length)-2]) );
        // set max-repetitions
        pdu.setErrindex( Integer.parseInt(opt.remArgs[(opt.remArgs.length)-1]) );
    }
    catch(NumberFormatException nfe)    {
        nfe.printStackTrace();
        System.exit(1);
    }
        try {
            // open session
            session.open();
        } 
        catch (SnmpException e) {
            System.err.println("Opening session"+e.getMessage());
            System.exit(1);
        }

        if(session.getVersion()==SnmpAPI.SNMP_VERSION_3) {
            pdu.setUserName(setVal.userName.getBytes());
            try
            {
                 
				USMUtils.init_v3_parameters(
					setVal.userName,
				         null,
					setVal.authProtocol,
					setVal.authPassword,
					setVal.privPassword,
					params,
					session,
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
        try {
            pdu = session.syncSend(pdu);
        }
        catch(SnmpException e) {
            System.err.println("Sending PDU:"+e.getMessage());
        session.close();
        api.close();
            System.exit(1);
        }
        // timeout
        if (pdu == null) {
            System.out.println("Request timed out to: " + opt.remArgs[0] );
            session.close();
            api.close();        
         System.exit(1);
        }

        // print and exit
        System.out.println("Response PDU received from " +((TcpProtocolOptionsImpl)(pdu.getProtocolOptions())).getRemoteHost()+
            ", community: " + pdu.getCommunity());
        // check for error
        if (pdu.getErrstat() != 0)
            System.out.println("Error Indication in response: " +
            SnmpException.exceptionString((byte)pdu.getErrstat()) +
            "\nErrindex: " + pdu.getErrindex());
        else
        {
            for(Enumeration e = pdu.getVariableBindings().elements();e.hasMoreElements() ;)
            {
                int error = 0;
                SnmpVarBind varbind = (SnmpVarBind)e.nextElement();
                // check for error index
                if ( (error = varbind.getErrindex()) != 0)
                    System.out.println("Error Indication in response: " +
                    SnmpException.exceptionString((byte)error));
                // print varbind
                System.out.println(varbind.toTagString());
            }
        }
        
        //close session
        session.close();
        // stop api thread
        api.close();
        
        System.exit(0);

    }

}
