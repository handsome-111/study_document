/* $Id: snmptrapforwarder.src,v 1.4.2.3 2004/03/17 10:05:30 vivek Exp $*/

/*
 * @(#)snmptrapforwarder.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the COPYRIGHTS file for more details.
 */




import com.adventnet.snmp.snmp2.*;
import java.util.*;
import java.net.InetAddress;

public class snmptrapforwarder extends Thread implements SnmpClient
{
    SnmpAPI api;
    SnmpSession v3_session;
    Vector v;
    UDPProtocolOptions remote_opt = new UDPProtocolOptions("localhost", 2001);
    int local_port=162;
    private static final int DEBUG_FLAG = 0;

    snmptrapforwarder()
    {
        // Start SNMP API
        api = new SnmpAPI();

        // Open session and set remote host & port if needed,
        v3_session = new SnmpSession(api);
        v = new Vector();
    }

    public static void main(String args[])
    {
        if (args.length!= 3)
        {
            System.out.println("Usage : java snmptrapforwarder [LocalPort] [RemoteHost] [RemotePort]");
            System.exit(1);
        }
        snmptrapforwarder eserv = new snmptrapforwarder();

        // Take care of getting options

        //String[] hostname={""};
        //eserv.v3_session.setLocalAddresses(hostname);
        eserv.local_port=Integer.parseInt(args[0]);
        UDPProtocolOptions v3_opt = new UDPProtocolOptions();
        v3_opt.setLocalPort(eserv.local_port);

        eserv.remote_opt.setRemoteHost(args[1]);
        eserv.remote_opt.setRemotePort(Integer.parseInt(args[2]));

        eserv.v3_session.setProtocolOptions(v3_opt);

        try
        {
            eserv.v3_session.open();
        }
        catch (SnmpException e)
        {
            System.out.println("Unable to start session: " + e);
            System.exit(1);
        }

        eserv.v3_session.addSnmpClient(eserv);
        eserv.start();
    } // end main()

    public boolean authenticate(SnmpPDU pdu, String community)
    {
        return true;
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


    /** Print octet data in a more readable form */
    String printOctets(byte[] data, int length)
    {
        StringBuffer s = new StringBuffer();

        int j = 0, line = 20; // we'll allow 20 bytes per line
        if (data.length < length) length = data.length;

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
        System.out.println("snmptrapforwarder: Ready to forward traps");
        while (true)
        {
            SnmpPDU pdu = deQ();

            if (pdu == null)
            {
                wait_for_v3pdus();
            }

            if (pdu == null)
            {
                pdu = deQ();
            }

            if (pdu == null)
            {
                continue;
            }

            try
            {
                if ((pdu.getCommand() == SnmpAPI.TRP_REQ_MSG) 
                ||(pdu.getCommand() == SnmpAPI.TRP2_REQ_MSG)
                )
                {
                    pdu.setProtocolOptions(remote_opt);
                    System.out.println("Trap is Forwarded to " + remote_opt.getSessionId());
                    v3_session.send(pdu);
                }
                else
                {
                    System.out.println("Not a Trap PDU -- dropping");
                }
            }
            catch (SnmpException e)
            {
                System.err.println("Sending  PDU" + e.getMessage());
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
