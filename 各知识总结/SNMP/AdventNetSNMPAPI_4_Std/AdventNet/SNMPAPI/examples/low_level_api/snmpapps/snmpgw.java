/* $Id: snmpgw.src,v 1.4.2.3 2004/03/17 10:05:30 vivek Exp $ */
/*
 * @(#)snmpgw.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the COPYRIGHTS file for more details.
 */

/**
 *  snmpgw acts as a gateway between a SNMPv3 management application and a
 *  SNMP v1/v2c agent system.When you start the snmpgw application, you can
 *  specify the remote v1 agent address and port number to which requests
 *  from the SNMPv3 management applications need to be forwarded. You can
 *  also specify the community string to be used while forwarding requests
 *  and other session parameters that are appropriate.
 *  The snmpgw applicaiton also requires the port number on which to listen
 *  for SNMPv3 requests, the USM security parameters like the user name on
 *  whose behalf the requests will be accepted, the authentication and privacy
 *  parameters for the specified user etc. need to be specified. 
 *  Usage:
 *     snmpgw [-d] [-v agent_version(v1,v2)] [-c agent_community] [-wc agent_writeCommunity] [-h agent_host] [-p agent_port] [-t timeout] [-r retries] [-a auth_protocol] [-w auth_password] [-s priv_password] ] port engineID user;
 *     Options:
 *         -d To get detailed debug output. Currently not a very usefule option
 *            Default : DEBUG is disabled
 *         -v version. Used to specify the version of remote host (v1 or v2)
 *            Default : remote agent version is SNMPv1
 *         -c community. Specify the community string used in communication with the v1/v2c remote agent
 *            Default : public
 *         -wc writeCommunity. Specify the wite community string used in communication with the v1/v2c remote agent
 *            Default : null
 *         -h agent_host. The remote agent host on which the SNMPv1/SNMPv2c agent is running.
 *            Default : localhost
 *         -p agent_port. Specify the remote agent port on which the agent listens for SNMPv1/v2c requests
 *            Default : 161
 *         -t timeout. Specify the timeout that is applicable both to the v1/v2c session and the v3 session
 *         -r retries. Specify the number of retries. Applicable both to the v1/v2c session and the v3 session
 *         -a authProtocol. The authentication protocol used in communication between the SNMPv3 management
 *              application and the snmpgw. Default: No Authentication
 *         -w auth_password. The authentication password used between the SNMPv3 managemnet application and
 *              the snmpgw application.
 *         -s priv_password. The privacy password used between the SNMPv3 managemnet application and the
 *              snmpgw application.
 *         port. The local UDP port on which the gateway application listens for SNMPv3 requests
 *         engineID. Specify the engineID of the snmpgw.
 *         user. Specify the user for whom the security parameters are defined. Currently this is the only user
 *              for whom the snmpgw will accept requests and forward it to the v1/v2c remote agent.
 */

import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;
import java.util.*;

public class snmpgw extends Thread implements SnmpClient
{
    SnmpAPI api;

    SnmpSession v3_session;
    SnmpSession v1_session;
    Vector v;
    int remoteVersion = 0;
    String remoteHost = "localhost";
    int remotePort = 161;
    int local_port;
    byte[] engineID; 

    private static final int DEBUG_FLAG = 0;
    private static final int VERSION_FLAG = 1;
    private static final int COMMUNITY_FLAG = 2;
    private static final int WRITE_COMMUNITY_FLAG = 3;
    private static final int AGENTHOST_FLAG = 4;
    private static final int AGENTPORT_FLAG = 5;
    private static final int TIMEOUT_FLAG = 6;
    private static final int RETRY_FLAG = 7;
    private static final int AUTHPROTOCOL_FLAG = 8;
    private static final int AUTHPASSWORD_FLAG = 9;
    private static final int PRIVPASSWORD_FLAG = 10;

    snmpgw()
    {
        // Start SNMP API
        api = new SnmpAPI();

        try
        {
            Thread.sleep(500);
        }
        catch (Exception x)
        {
        }

        // Open session and set remote host & port if needed,
        v3_session = new SnmpSession(api);
        v1_session = new SnmpSession(api);
        v = new Vector();
    }

    public static void main(String args[])
    {
        snmpgw eserv = new snmpgw();

        // Take care of getting options
        String usage =
        "snmpgw [-d] [-v agent_version(v1,v2)] [-c agent_community] \n" +
        "[-wc agent_writeCommunity] [-h agent_host] [-p agent_port] \n" +
        "[-t timeout] [-r retries] [-a auth_protocol] [-w auth_password] \n" +
        "[-s priv_password] ] port engineID user";
        String options[] = { "-d", "-v","-c", "-wc", "-h", "-p", "-t", "-r", "-a", "-w", "-s" };
        String values[] = { "None", null, null, null, null, null, null, null, null, null, null };
        String userName = null;
        int authProtocol = 0;
        String authPassword = null;
        String privPassword = null;
        int options_flag = 0;


        ParseOptions opt = new ParseOptions(args,options,values, usage);

        if (opt.remArgs.length<3)
        {
            opt.usage_error();
        }

        eserv.local_port = Integer.parseInt(opt.remArgs[0]);
        UDPProtocolOptions v3_opt = new UDPProtocolOptions();
        v3_opt.setLocalPort(eserv.local_port);
        eserv.v3_session.setProtocolOptions(v3_opt);

        eserv.engineID = (opt.remArgs[1]).getBytes();
        userName = opt.remArgs[2];

        if (values[DEBUG_FLAG].equals("Set"))
        {
            System.out.println("Debug is TRUE");
            eserv.api.setDebug( true );
        }

        if(values[VERSION_FLAG] != null)
        {
            if(values[VERSION_FLAG].equals("v2"))
            {
                eserv.remoteVersion = (SnmpAPI.SNMP_VERSION_2C);
            }
            else if(values[VERSION_FLAG].equals("v1"))
            {
                eserv.remoteVersion = (SnmpAPI.SNMP_VERSION_1);
            }
            else
            {
                System.err.println("Invalid Version Number. Please use v1 or v2");
                System.exit(1);
            }
        }

        if (values[COMMUNITY_FLAG] != null)
        {
            eserv.v1_session.setCommunity( values[COMMUNITY_FLAG] );
        }
        if (values[WRITE_COMMUNITY_FLAG] != null)
        {
            eserv.v1_session.setWriteCommunity( values[WRITE_COMMUNITY_FLAG] );
        }

        try
        {
            UDPProtocolOptions v1_opt = new UDPProtocolOptions();
            if (values[AGENTHOST_FLAG] != null) 
            {
                v1_opt.setRemoteHost(values[AGENTHOST_FLAG]);
                eserv.remoteHost = values[AGENTHOST_FLAG];
            }
            if (values[AGENTPORT_FLAG] != null) 
            {
                eserv.remotePort = Integer.parseInt(values[AGENTPORT_FLAG]);
                v1_opt.setRemotePort(eserv.remotePort);
            }
            eserv.v1_session.setProtocolOptions(v1_opt);
            if (values[TIMEOUT_FLAG] != null) 
            {
                eserv.v1_session.setTimeout(Integer.parseInt(values[TIMEOUT_FLAG]));
                eserv.v3_session.setTimeout(Integer.parseInt(values[TIMEOUT_FLAG]));
            }
            if (values[RETRY_FLAG] != null) 
            {
                eserv.v1_session.setRetries(Integer.parseInt(values[RETRY_FLAG]));
                eserv.v3_session.setRetries(Integer.parseInt(values[RETRY_FLAG]));
            }
            if (values[AUTHPROTOCOL_FLAG] != null) 
            {
                if (values[AUTHPROTOCOL_FLAG].compareTo("MD5") == 0)
                {
                    authProtocol = USMUserEntry.MD5_AUTH;
                }
                else if (values[AUTHPROTOCOL_FLAG].compareTo("SHA") == 0)
                {
                    authProtocol = USMUserEntry.SHA_AUTH;
                }
                else
                {
                    authProtocol = USMUserEntry.NO_AUTH;
                }
            }
            if (values[AUTHPASSWORD_FLAG] != null) 
            {
                authPassword = values[AUTHPASSWORD_FLAG];
            }
            if (values[PRIVPASSWORD_FLAG] != null) 
            {
                privPassword = values[PRIVPASSWORD_FLAG];
            }
        }
        catch(Exception ex)
        {
            System.err.println(ex.getMessage());
            System.exit(2);
        }


        try
        {
            eserv.v3_session.open();
            eserv.v1_session.open();
        }
        catch (SnmpException e)
        {
        }

        eserv.createUSMTable(userName.getBytes(), authProtocol, authPassword, privPassword);
        eserv.v3_session.addSnmpClient(eserv);
        eserv.start();
    } // end main()

    public boolean authenticate(SnmpPDU pdu, String community)
    {
        boolean rv = false;
        if(pdu != null)
        {
            if(((Snmp3Message)pdu.getMsg()).isAuthenticationFailed())
            {
                System.out.println("In snmpgw : authenticate "+
                "failed. Dropping PDU.");
            }
            else
            {
                rv = true;
            }
        }
        return rv;
    }

    public boolean callback(SnmpSession sess, SnmpPDU pdu, int reqID)
    {
        if (pdu == null)
        {
            System.err.println("Null PDU received");
            return false;
        }
        enQ(pdu);
        return true;
    }

    public void debugPrint(String debugOutput)
    {
    }

    public void createUSMTable(byte[] name, int authProtocol, String authPassword, String privPassword)
    {
        byte level = 0;

        USMUserEntry entry = new USMUserEntry(name, engineID);
        entry.setAuthProtocol(authProtocol);

        if ((authProtocol != USMUserEntry.NO_AUTH) && (authPassword != null))
        {
            byte[] authKey = USMUtils.password_to_key(authProtocol, authPassword.getBytes(), authPassword.getBytes().length, engineID);
            entry.setAuthKey(authKey);
            level = 1;

            if (privPassword != null)
            {
                byte[] tempKey = USMUtils.password_to_key(authProtocol, privPassword.getBytes(), privPassword.getBytes().length, engineID);
                byte privKey[]=new byte[16];
                System.arraycopy(tempKey,0,privKey,0,16);
                entry.setPrivKey(privKey);
                level |= 2;
            }
        }

        entry.setSecurityLevel(level);

        USMUserTable USMTable = (USMUserTable)api.getSecurityProvider().getTable(3);
        USMTable.addEntry(entry);

        byte[] names = entry.getUserName();
        byte[] id = entry.getEngineID();

        SnmpEngineEntry e = new SnmpEngineEntry("localhost", local_port);
        e.setEngineID(engineID);
        entry.setEngineEntry(e);

        api.setSnmpEngineID(engineID);
    }

    /** Print octet data in a more readable form */
    String printOctets(byte[] data, int length)
    {
        StringBuffer s = new StringBuffer();

        int j = 0, line = 20; // we'll allow 20 bytes per line
        if (data.length < length)
        {
            length = data.length;
        }

        for (int i=0;i<length;i++)
        {
            if (j++ > 19)
            {
                j=1;
                s.append("\n");
            }
            String bs = Integer.toString(byteToInt(data[i]),16);
            if (bs.length() < 2)
            {
                bs = "0" + bs;
            }
            s.append(bs+ " ");
        }
        return s.toString();
    }

    public synchronized SnmpPDU deQ()
    {
        for (Enumeration e = v.elements() ; e.hasMoreElements() ;)
        {
            SnmpPDU pdu = (SnmpPDU) e.nextElement();
            v.removeElement(pdu);
            return pdu;
        }
        return null;
    }

    /** Place in specified queue */
    public synchronized void enQ(SnmpPDU pdu)
    {
        v.addElement(pdu);
        notifyAll();
    }

    public void run()
    {
        System.out.println("snmpgw: Ready to process requests from SNMPv3 Manager");
        while (true)
        {
            SnmpPDU pdu = deQ();

            if (pdu == null)
            {
                wait_for_v3pdus();
            }

            if (pdu == null)
            {
                continue;
            }

            SnmpPDU ref_pdu = pdu;

            int version = ref_pdu.getVersion();
            ProtocolOptions ref_opt = pdu.getProtocolOptions();

            ref_pdu.setVersion(remoteVersion);
            ref_pdu.setProtocolOptions(null);

            SnmpPDU rpdu = null;
            try
            {
                // Send PDU
                SnmpVarBind varb = ref_pdu.getVariableBinding(0);
                if(varb!=null)
                {
                    System.out.println("sent V" + (remoteVersion + 1) + " request: OID sent = " + varb.getObjectID());
                }
                rpdu = v1_session.syncSend(ref_pdu);
            }
            catch (SnmpException e)
            {
                System.err.println("Sending V1 PDU" + e.getMessage());
                continue;
            }

            if (rpdu == null)  // timeout
            {
                System.err.println("V1 Request timed out to: " + pdu.getProtocolOptions().getSessionId() );
                continue;
            }

            SnmpVarBind varb = null;
            int size = pdu.getVariableBindings().size();

            for (int i = 0; (i < size); i++)
            {
                pdu.removeVariableBinding(0);
            }

            size = rpdu.getVariableBindings().size();
            for (int i = 0; i < size; i++)
            {
                pdu.addVariableBinding(rpdu.getVariableBinding(i));
            }

            SnmpVarBind varbr = pdu.getVariableBinding(0);
            if(varbr!=null)
            {
                System.out.println("Received V" + (remoteVersion + 1) + " response: OID received = " + varbr.getObjectID());
            }
            pdu.setVersion(version);
            pdu.setCommand(rpdu.getCommand());
            pdu.setErrstat(rpdu.getErrstat());
            pdu.setErrindex(rpdu.getErrindex());
            pdu.setProtocolOptions(ref_opt);

            try
            {
                v3_session.send(pdu);
            }
            catch (SnmpException e)
            {
                System.err.println("Session Open "+e.getMessage());
                continue;
            }
        }
    }

    public synchronized void wait_for_v3pdus()
    {
        try
        {
            if (v.size() > 0)
            {
                return;
            }
            else
            {
                wait();
            }
        }
        catch (InterruptedException i)
        {
        }
    }

    static int byteToInt(byte b) 
    {
        return (int)b & 0xFF;
    }
}
