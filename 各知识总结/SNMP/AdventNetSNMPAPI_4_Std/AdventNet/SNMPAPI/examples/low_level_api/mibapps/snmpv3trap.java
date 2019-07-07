/* $Id: snmpv3trap.src,v 1.4.2.3 2006/07/07 15:55:17 rravikumar Exp $ */
/*
 * @(#)snmpv3trap.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how to write an application to send a
 * v3 Trap message using com.adventnet.snmp.snmp2 package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * java snmpv3trap [-d] [-p port][-e engineID(0x....)] [-a auth_protocol] [-w auth_password] [-s priv_password] [-i contextName] -m MIB_files userName host TimeTicksvalue OIDvalue [OID value] ...
 * e.g.
 * java snmpv3trap -m "../../../mibs/RFC1213-MIB ../../../mibs/SNMPv2-MIB" -e 0x000012141516171819202121 -a MD5 -w initial2Pass -i initial initial2 10.3.2.120 16352 coldStartTrap ifIndex.2 2
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
 * [-p] <port>           - remote port no. By default 162.
 * [-e] <engineID>       - Engine ID.
 * [-a] <autProtocol>    - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword>   - The authentication password.
 * [-s] <privPassword>   - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>    - The context to be used for the v3 pdu.
 * [-i] <contextID>      - The contextID to be used for the v3 pdu.
 * -m <MIBfile>          - MIB files.Mandatory.To load multiple mibs give within double quotes files seperated by a blank space.       
 * <timeticks> Mandatory - the value of object sysUpTime when the event occurred
 * <OID-value> Mandatory - Object Identifier  
 * <username>  Mandatory - The v3 principal/userName.
 * <host>      Mandatory - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <OID>       Mandatory - Object Identifier.
 * <value>     Mandatory - The object instance value to be set .
 */ 
    
import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.usm.*;

public class snmpv3trap 
{
    private static final int DEBUG = 0;
    private static final int PORT = 1;
    private static final int MIBS = 2;
    private static final int AUTH_PROTOCOL = 3;
    private static final int AUTH_PASSWORD = 4;
    private static final int PRIV_PASSWORD = 5;
    private static final int CONTEXT_NAME = 6;
    private static final int CONTEXT_ID = 7;
    private static final int ENGINEID = 8;
    static final int USM_SECURITY_MODEL = 3;
    private static final int PRIV_PROTOCOL=9;
    public static void main(String args[]) 
    {
        
        // Take care of getting options
        String usage = 
            "\nsnmpv3trap [-d] [-p port][-e engineID(0x....)]\n"+
            "[-a auth_protocol] [-w auth_password] [-s priv_password]\n"+
            "[-n contextName] [-i contextID][-pp privProtocol (AES/DES)] -m MIB_files userName\n"+
            "host TimeTicksvalue OIDvalue [OID value] ...";

        String options[] = 
            {
                "-d", "-p", "-m", "-a", "-w", "-s", "-n", "-i", "-e" ,"-pp"
            };

        String values[] = 
            {
                "None", null, null, null, null, null, null, null, null , null
            };
   
        String id = new String(""); 
        String userName = new String("");
        int authProtocol = USMUserEntry.NO_AUTH;
        int privProtocol = USMUserEntry.NO_PRIV;
        String authPassword = new String ("");
        String privPassword = new String ("");
        String contextName = new String ("");
        String contextID = new String ("");
    
        ParseOptions opt = new ParseOptions(args,options,values, usage);
        if (opt.remArgs.length<4) 
        {
            opt.usage_error();
        }   
                     
        // Start SNMP API
        SnmpAPI api;
        api = new SnmpAPI();
        if (values[DEBUG].equals("Set"))
        {
            api.setDebug( true );
        }   
        
        SnmpPDU pdu = new SnmpPDU();
        Snmp3Message msg = (Snmp3Message)(pdu.getMsg());
        pdu.setCommand( api.TRP2_REQ_MSG );

        // Open session 
        SnmpSession session = new SnmpSession(api);
   
        // set remoteHost
        UDPProtocolOptions ses_opt = new UDPProtocolOptions(opt.remArgs[1]);

        // set version
        session.setVersion( SnmpAPI.SNMP_VERSION_3 ) ;
    
        // set EngineID
        try 
        {        
            if(values[PORT] != null)
            {
                ses_opt.setRemotePort( Integer.parseInt(values[PORT]) );
            }   
            else
            {
                ses_opt.setRemotePort(162);
            }   
            
            if (values[ENGINEID]!=null) 
            {
                id = values[ENGINEID];
                if(id.startsWith("0x") || id.startsWith("0X"))
                {
                    id = new String(gethexValue(values[ENGINEID]));
                }   
            }
        }
        catch (NumberFormatException ex) 
        {
            System.err.println("Invalid Integer Arg");
        }
        catch (StringIndexOutOfBoundsException sie)
        {
            System.err.println("Invalid engineID. Please specify proper" +
                                        " hex value. Exception = " + sie);
            opt.usage_error();
        }

        session.setProtocolOptions(ses_opt);
            
        userName = opt.remArgs[0];
                
        if ((values[AUTH_PROTOCOL] != null) && (values[AUTH_PASSWORD] != null)) 
        {
            if(values[AUTH_PROTOCOL].equals("SHA"))
            {
                authProtocol = USMUserEntry.SHA_AUTH;
            }   
            else 
            {
                authProtocol = USMUserEntry.MD5_AUTH;                   
            }   
            if(authProtocol==USMUserEntry.NO_AUTH)
            {
                System.err.println("Enter authentication protocol");
                opt.usage_error();
            }
                    
            authPassword = values[AUTH_PASSWORD];
            if (values[PRIV_PASSWORD] != null) 
	{
              privPassword = values[PRIV_PASSWORD];
	    if(values[PRIV_PROTOCOL] !=null)
	    {
		  if(values[PRIV_PROTOCOL].equals("DES"))
		  {  
		    
		     privProtocol=USMUserEntry.CBC_DES;
		  }
		  else if(values[PRIV_PROTOCOL].equals("AES"))
		  {  
		    
		     privProtocol=USMUserEntry.CFB_AES_128;
		  }
		  else
		  {
			  System.out.println(" Invalid privProtocol ");
			   opt.usage_error();
		  }
	
	    }
	    else
	    {
		    System.out.println(" Please specify the privProtocol value ");
                     opt.usage_error();
		    
	    }	    
		
	}		         
        }
        else if ((values[AUTH_PROTOCOL] != null) 
                || (values[AUTH_PASSWORD] != null) 
                || (values[PRIV_PASSWORD] != null)) 
        {
            opt.usage_error();
        }
    
        if (values[CONTEXT_NAME] != null)
        {
            contextName = values[CONTEXT_NAME];
        }   
        if (values[CONTEXT_ID] != null) 
        {
            contextID = values[CONTEXT_ID];
        }   


        createUSMTable(userName.getBytes(), id.getBytes(), authProtocol,
           authPassword, privPassword, api,privProtocol);
        pdu.setUserName(userName.getBytes());
    
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
            }
        }
        
        // Adding the sysUpTime variable binding 
        SnmpOID oid = mibOps.getSnmpOID(".1.3.6.1.2.1.1.3.0");
        if (oid == null) 
        {
            System.exit(1);
        }   
        else     
        {
            addVarBind(mibOps, pdu, oid, opt.remArgs[2]);
        }   

        // Adding the snmpTrapOID variable binding
         oid = mibOps.getSnmpOID(".1.3.6.1.6.3.1.1.4.1.0");
        if (oid == null) 
        {
            System.exit(1);
        }
        else 
        {
            addVarBind(mibOps, pdu, oid, opt.remArgs[3]);
        }   
    
        // add Variable Bindings
        for (int i=4;i<opt.remArgs.length;) 
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
            // Opening session
            session.open();
            // Send PDU
             session.send(pdu);    
        } 
        catch (SnmpException e) 
        {
            System.err.println("Sending PDU"+e.getMessage());
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
                    LeafSyntax syntx = node.getSyntax();
                    if(syntx.getType() == SnmpAPI.OBJID)
                    {
                        SnmpOID snmpoid = mibOps.getSnmpOID(value);
                        value = snmpoid.toString();
                    }
                    // Get the SnmpVar instance for the value
                    SnmpVar var = syntx.createVariable(value);
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

    private static byte[] gethexValue(String value)
    {
        byte temp;
        byte[] Key=new byte[value.length()/2 - 1];
        String ss,str;

        ss = value.substring(2);
        for(int i = 0; i < ss.length(); i+=2)
        {
            str = ss.substring(i,i+2);
            temp = (byte)Integer.parseInt(str,16);
            Key[i/2] = temp;
        }
        return Key;    
    }

    
    public static void createUSMTable(byte[] name, byte[] engineID, 
                                    int authProtocol, String authPassword,
                                    String privPassword, SnmpAPI api, int privProtocol)
    {
        byte level = 0;
    
        USMUserEntry entry = new USMUserEntry(name, engineID);
        entry.setAuthProtocol(authProtocol);

        if ((authProtocol != USMUserEntry.NO_AUTH) && (authPassword != null))
        {
            byte[] authKey = USMUtils.password_to_key(authProtocol, 
                                            authPassword.getBytes(), 
                                            authPassword.getBytes().length,
                                            engineID);
            entry.setAuthKey(authKey);
            level = 1;
            
            if ((privPassword != null)&&(privPassword.length()>0))
            {
                byte[] tempKey = USMUtils.password_to_key(authProtocol,
                                            privPassword.getBytes(), 
                                            privPassword.getBytes().length,
                                            engineID);
     	entry.setPrivProtocol(privProtocol);          
      byte privKey[]=new byte[16];
                        System.arraycopy(tempKey,0,privKey,0,16);
                entry.setPrivKey(privKey);
                level |= 2;
            }
        }
    
        entry.setSecurityLevel(level);
        USMUserTable uut = (USMUserTable)api.getSecurityProvider().
                                            getTable(USM_SECURITY_MODEL);
        uut.addEntry(entry);

        api.setSnmpEngineID(engineID);
    }   
  
}
