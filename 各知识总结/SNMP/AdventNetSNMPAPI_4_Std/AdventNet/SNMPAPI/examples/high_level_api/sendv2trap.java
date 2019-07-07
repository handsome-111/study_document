/* $Id: sendv2trap.src,v 1.5.2.4 2006/08/30 17:58:43 srikrishnan Exp $ */
/*
 * @(#)sendtrap.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/** 
 *  Send SNMP trap based on command line arguments.  Loads MIBs 
 *  as specified, and converts to/from names for loaded MIB data. 
 *  Since variable types are not input, MIBs have to be loaded for
 *  any variables being used in trap PDU.
 *
 * This is an example program to explain how to write an application to send a
 * Trap message using com.adventnet.snmp.beans package of AdventNetSNMP2 api.
 * The user could run this application by giving any one of the following usage.
 *  
 * Note: Refer README file  
 *
 * If the oid is not starting with a dot (.) it will be prefixed by .1.3.6.1.2.1 .
 * So the entire OID of 1.1.0 will become .1.3.6.1.2.1.1.1.0 . You can also
 * give the entire OID .
 * 
 * If the mib is loaded you can also give string oids in the following formats
 * .iso.org.dod.internet.mgmt.mib-2.system.sysDescr.0 or system.sysDescr.0 or
 * sysDescr.0 .
 *
 * [-d]                - Debug output. By default off.
 * [-c] <community>    - community String. By default "public".
 * [-p] <port>         - remote port no. By default 161.
 * [-m] <mibs>         - The mibs to be loaded. 
 * [-v] <version>      - version(v2 / v3). By default v2.
 * [-e] <engineID>     - EngineID for this entity.
 * [-u] <username>     - The v3 principal/userName
 * [-a] <autProtocol>  - The authProtocol(MD5/SHA). Mandatory if authPassword is specified
 * [-w] <authPassword> - The authentication password.
 * [-s] <privPassword> - The privacy protocol password. Must be accompanied with auth password and authProtocol fields.
 * [-i] <contextName>  - Context Name.
 * [-pp] <privProtocol> - The privacy protocol . Must be accompanied with auth,priv password and authProtocol fields.
 * <host> Mandatory    - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 * <timeticks> Mandatory - the value of object sysUpTime when the event occurred
 * <OID-value> Mandatory - Object Identifier  
 * <OID>  Mandatory    - Give multiple no. of Object Identifiers.
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.beans.*;
import com.adventnet.snmp.snmp2.SnmpException;
import com.adventnet.snmp.snmp2.SnmpOID;
import com.adventnet.snmp.snmp2.usm.*;

public class sendv2trap {

    private static final int ENGINEID = 4;
    private static final int VERSION = 5;
    private static final int USER_NAME = 6;
    private static final int AUTH_PROTOCOL = 7;
    private static final int AUTH_PASSWORD = 8;
    private static final int PRIV_PASSWORD = 9;
    private static final int CONTEXT_NAME = 10;
    
    private static final int COMMUNITY = 1;
    private static final int PORT = 2;
    private static final int MIBS = 3;

    private static final int DEBUG = 0;
    private static final int AGENTADDRESS = 11;
    private static final int PRIV_PROTOCOL = 12;
    
    public static void main(String args[]) {

    // Take care of getting options
    String usage = "sendv2trap [-d Debug][-v version(v2,v3)] [-c community] [-p port] [-e engineID(1234.../0x1234...)] [-u user] [-a auth_protocol(MD5/SHA)] [-w auth_password] [-s priv_password] [-i contextName] [-g agent-address] [ -pp priv_protocol (DES/AES) ] [-m MIB_file] host TimeTicksvalue trapOID [OID value] ...";
    String options[] = { "-d", "-c", "-p", "-m", "-e", "-v", "-u", "-a", "-w", "-s", "-i" ,"-g", "-pp"};
    String values[] = { "None", null, null, null,null, null, null, null, null, null, null,null, null};
    String id = new String("");
    boolean agentflag=false;
    long upTime=0;
    ParseOptions opt = new ParseOptions(args,options,values, usage);
    if (opt.remArgs.length<3) opt.usage_error();

    // Use an SNMP target bean to perform SNMP operations
    SnmpTarget target = new SnmpTarget();

     //To load MIBs from compiled file
     target.setLoadFromCompiledMibs(true);

    // Set Debug mode
    if (values[DEBUG].equals("Set")) target.setDebug(true);

    target.setTargetHost( opt.remArgs[0] );  // set destination hostname
    
    //Assign the port for sending the trap.
    target.setTargetPort(162);
    if(values[PORT]!=null) {
        try {
            target.setTargetPort(Integer.parseInt(values[PORT]));
        }
        catch(NumberFormatException e) {
            System.err.println("Sending trap to port 162");
        }
    }
    try {
        upTime = Long.parseLong(opt.remArgs[1]);
    } catch (NumberFormatException ex) {
        System.err.println("Invalid Integer Argument for upTime "+ex);
        opt.usage_error();
    }
    
    target.setSnmpVersion(target.VERSION2C);
    if (values[VERSION] != null) // set the version if specified
        if(values[VERSION].equals("v3"))
            target.setSnmpVersion(target.VERSION3);

    if(target.getSnmpVersion()== target.VERSION3) {

        if(values[ENGINEID] != null){
            id =  values[ENGINEID];
            if(id.startsWith("0x") || id.startsWith("0X"))
                id = new String(gethexValue(values[ENGINEID]));
        }

        if (values[USER_NAME] != null) {
            target.setPrincipal(values[USER_NAME]);
        
        if ((values[AUTH_PROTOCOL] != null) && (values[AUTH_PASSWORD] != null)) {
            if(values[AUTH_PROTOCOL].equals("SHA"))
                target.setAuthProtocol(target.SHA_AUTH);
            else if(values[AUTH_PROTOCOL].equals("MD5"))
                target.setAuthProtocol(target.MD5_AUTH);
            else
                target.setAuthProtocol(target.NO_AUTH);            
            target.setAuthPassword(values[AUTH_PASSWORD]);
            if (values[PRIV_PASSWORD] != null) 
            {
            target.setPrivPassword(values[PRIV_PASSWORD]);
            if(values[PRIV_PROTOCOL] != null)
            {
                    if(values[PRIV_PROTOCOL].equals("AES"))
                    {
                       target.setPrivProtocol(target.CFB_AES_128);
                    }
                    else if(values[PRIV_PROTOCOL].equals("DES"))
                    {
                       target.setPrivProtocol(target.CBC_DES);
                    }
                    else
                    {
                     System.out.println(" Invalid PrivProtocol "+values[PRIV_PROTOCOL]);
                     opt.usage_error();
                    }
            }
        }
        }
        else if ((values[AUTH_PROTOCOL] != null) || (values[AUTH_PASSWORD] != null) || (values[PRIV_PASSWORD] != null)) {
            opt.usage_error();
        }
        }
        else {
            System.err.println("UserName Missing");
            opt.usage_error();
        }

        // Configure the USM entries on this entity.
        createUSMTable(target.getPrincipal().getBytes(),id.getBytes(),
                        target.getAuthProtocol(),target.getAuthPassword(),target.getPrivProtocol(),
                        target.getPrivPassword(), target);
    }
    String community="";
    int extraVarCount=0;
    String agentAddress="";


    if(values[COMMUNITY] != null)
    {
          target.setCommunity( values[COMMUNITY]);
            community=values[COMMUNITY];
            extraVarCount++;                    
    }


    if (values[MIBS] != null) try {  // Load the MIB files 
        System.err.println("Loading MIBs: "+values[MIBS]);
        target.loadMibs(values[MIBS]);
        System.err.println("Done.");
    } catch (Exception ex) {
        System.err.println("Error loading MIBs: "+ex);
    System.exit(1);
    }

    if (values[AGENTADDRESS] != null) 
    {  // Load the MIB files 
        agentAddress=values[AGENTADDRESS];
        extraVarCount++;
    }
    // Put together OID and variable value lists from command line
    String oids[] = null, var_values[] = null;  // trap oids and values

    int num_varbinds = 0;
    for (int i=3;i<opt.remArgs.length;i+=2) { // add Variable Bindings
        if (opt.remArgs.length < i+2) //need "{OID type value}"
           opt.usage_error(); 
        num_varbinds++;
    }
     oids = new String[num_varbinds+extraVarCount];
    var_values = new String[num_varbinds+extraVarCount];
    if(!agentAddress.equals(""))
    {
        oids[0]=".1.3.6.1.6.3.18.1.3.0";
        var_values[0]=agentAddress; 
        if(target.getMibOperations()!=null)
        {
            if(target.getMibOperations().getMibNode(new SnmpOID(".1.3.6.1.6.3.18.1.3.0"))==null)
            {
                System.out.println("Error : SNMP-TARGET and SNMP-COMMUNITY mibs are not loaded");       
                System.exit(0);
            }
        }
        else
        {
            System.out.println("Error : SNMP-TARGET and SNMP-COMMUNITY mibs are not loaded");       
            System.exit(0);
        }
    }


        if(!community.equals(""))
        {               
            if(agentflag)//if agent details are given in command line.
            {
                oids[1]=".1.3.6.1.6.3.18.1.4.0";
                var_values[1]=community;    
            }
            else//if agent details are not given in command line.
            {
                oids[0]=".1.3.6.1.6.3.18.1.4.0";
                var_values[0]=community;
            }
            if(target.getMibOperations()!=null)
            {
                if(target.getMibOperations().getMibNode(new SnmpOID(".1.3.6.1.6.3.18.1.4.0"))==null)
                {
                    System.out.println("Error : SNMP-TARGET and SNMP-COMMUNITY mibs are not loaded");       
                    System.exit(0);
                }
            }
            else
            {
                System.out.println("Error : SNMP-TARGET and SNMP-COMMUNITY mibs are not loaded");       
                System.exit(0);
            }
        }

    for (int i=0;i<num_varbinds;i++) { // add Variable Bindings
        oids[i+extraVarCount] = opt.remArgs[(2*i)+3];
        var_values[i+extraVarCount] = opt.remArgs[(2*i)+4];
     }

    try {  // use SnmpTarget methods to send trap w/ specified OIDs/values
        target.setObjectIDList(oids);
        target.snmpSendNotification(upTime, opt.remArgs[2], var_values);
        // allow time to send trap before exiting
        Thread.sleep(500);

    } catch (Exception e) {
        System.err.println("Error Sending Trap: "+e.getMessage());
    }
    
    if (target.getErrorCode() != -1)    {
        System.out.println(target.getErrorString());
    }
    System.exit(0);
    
    }
    public static void createUSMTable(byte[] name, byte[] engineID, 
                                    int authProtocol, String authPassword,
                                    int privProtocol,String privPassword, SnmpTarget target)
    {
        byte level = 0;
    
        USMUserTable uut = target.getUSMTable();
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
                byte privKey[]=new byte[16];
                System.arraycopy(tempKey,0,privKey,0,16);
                entry.setPrivProtocol(privProtocol);
                entry.setPrivKey(privKey);
                level |= 2;
            }
        }
    
        entry.setSecurityLevel(level);
        uut.addEntry(entry);

        target.setSnmpEngineID(engineID);
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
}
