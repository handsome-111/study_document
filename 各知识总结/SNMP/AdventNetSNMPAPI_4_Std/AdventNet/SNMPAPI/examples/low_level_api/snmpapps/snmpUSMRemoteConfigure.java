/* $Id: snmpUSMRemoteConfigure.src,v 1.4.2.2 2004/03/17 10:05:30 vivek Exp $ */
/*
 * @(#)snmpUSMRemoteConfigure.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program to explain how remotely configure users
 * on the agent. The procedure followed is a five step procedure.
 * Step 1. GET(usmUserSpinLock.0) Value 
 * Step 2. SET(usmUserSpinLock.0=spinlockValue
 *              usmUserCloneFrom=templateUser,
 *              usmUserStatus=createAndWait)
 * Strp 3. GET(usmUserSpinLock.0) value
 *              SET(usmUserSpinLock.0=spinLockValue,
 *              usmUserKeyChange=keyChangeValue
 *              usmUserPublic=randomValue)
 * Strp 4. GET(usmUserPulic) and check it has randomValue
 * Step 5. Activate the new user by setting the usmUserStatus=active
 *
 * The application sends request with version v3. 
 * The user could run this application by giving any of the following usage.
 *  
 * java snmpUSMRemoteConfigure [options] userName newUserName hostname 
 *
 * iava snmpUSMRemoteConfigure [-d] [-p port] [-r retries] [-t timeout] [-a auth_protocol] [-w auth_password] [-s priv_password] [-n contextName] [-i contextID] [-y new_auth_password] [-z new_priv_password] userName newUserName host
 *
 * e.g.
 * java snmpUSMRemoteConfigure -a MD5 -w initial2Pass -y initial2NewPass initial2 newInitial 10.3.2.120
 * Where, initial2 user already configured on the agent whose authProtocol is
 * MD5 and authPassword is initial2Pass. newInitial is the name of the new
 * user who will be configured with authProtocol=MD5 and 
 * authPassword=initial2NewPass.
 * Here the clone-from user is initial2, the user on whose behalf all the
 * requests will be sent.
 * 
 * Options:
 * [-d]                   - Debug output. By default off.
 * [-p] <port>            - remote port no. By default 161.
 * [-t] <Timeout>         - Timeout. By default 5000ms.
 * [-r] <Retries>         - Retries. By default 0.      
 * [-a] <autProtocol>     - The authProtocol(MD5/SHA) of the template user. Mandatory if authPassword is specified
 * [-w] <authPassword>    - The authentication password of the template user.
 * [-s] <privPassword>    - The privacy protocol password of the template user. Must be accompanied with auth password and authProtocol fields.
 * [-n] <contextName>     - The contextName to be used for the v3 pdu.
 * [-i] <contextID>       - The contextID to be used for the v3 pdu.
 * [-w] <newAuthPassword> - The authentication password for the new user.
 * [-s] <newPrivPassword> - The privacy protocol password of the new user. Must be accompanied with auth password and authProtocol fields.
 * username Mandatory     - The user who is already configured on the agent.
 *                          (template user)
 * newusername Mandatory  - The user name of the new user who will be 
                            Remotely configured on the agent.
 * host Mandatory         - The RemoteHost (agent).Format (string without double qoutes/IpAddress).
 */

import java.lang.*;
import java.util.*;
import java.net.*;
import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.usm.*;
import com.adventnet.utils.*;

public class snmpUSMRemoteConfigure {

    private static final int USM_SECURITY_MODEL = 3;

    private static final String ENC = "ISO8859_1";
    private static final int DEBUG = 0;
    private static final int PORT = 1;
    private static final int RETRIES = 2;
    private static final int TIMEOUT = 3;
    private static final int AUTH_PROTOCOL = 4;
    private static final int AUTH_PASSWORD = 5;
    private static final int PRIV_PASSWORD = 6;
    private static final int CONTEXT_NAME = 7;
    private static final int CONTEXT_ID = 8;
    private static final int NEW_AUTH_PASSWORD = 9;
    private static final int NEW_PRIV_PASSWORD = 10;

    private static final String SPIN_LOCK_OID = ".1.3.6.1.6.3.15.1.2.1.0";
    private static final String USM_TABLE = ".1.3.6.1.6.3.15.1.2.2";
    private static final String USM_ENTRY = ".1.3.6.1.6.3.15.1.2.2.1";
    private static final String CLONE_COL = "4";
    private static final String KEY_CHANGE_COL = "6";
    private static final String PRIV_KEY_CHANGE_COL = "9";
    private static final String USM_PUBLIC_COL = "11";
    private static final String ROW_STATUS_COL = "13";

    private static final int AUTH_MD5_LEN = 16;
    private static final int AUTH_SHA_LEN = 20;

    boolean debug = false;

    public static void main(String args[]) {
        
        snmpUSMRemoteConfigure surg = new snmpUSMRemoteConfigure();
        
        // Take care of getting options
        String usage =
        "snmpUSMRemoteConfigure [-d] [-p port] [-r retries] \n" +
        "[-t timeout] [-a auth_protocol] [-w auth_password] \n" +
        "[-s priv_password] [-n contextName] [-i contextID] \n" +
        "[-y new_auth_password] [-z new_priv_password] \n" +
        "userName newUserName host ";
        String options[] =
        {
            "-d", "-p", "-r", "-t", "-a",
            "-w", "-s", "-n", "-i", "-y",
            "-z"
        };
        String values[] =
        {
            "None", null, null, null, null,
            null, null, null, null, null,
            null
        };

        String userName = new String("");
        int authProtocol = USMUserEntry.NO_AUTH;
        String authPassword = new String ("");
        String privPassword = new String ("");
        String contextName = new String ("");
        String contextID = new String ("");

        String newUser = new String("");
        //int newAuthProtocol = USMUserEntry.NO_AUTH;
        String newAuthPassword = new String ("");
        String newPrivPassword = new String ("");

         ParseOptions opt = new ParseOptions(args,options,values, usage);
        if (opt.remArgs.length<3) opt.usage_error();

        // Start SNMP API
        SnmpAPI api;
        api = new SnmpAPI();

        if (values[DEBUG].equals("Set")){
            api.setDebug( true );
            surg.debug = true;
        }
    
        userName = opt.remArgs[0];
        newUser = opt.remArgs[1];

        // Open session
        SnmpSession session = new SnmpSession(api);        
        
        // set remote Host 
        UDPProtocolOptions ses_opt = new UDPProtocolOptions(opt.remArgs[2]);

        // Set the values accepted from the command line
        //boolean usage_error = false;
    
        //set remote Port, timeout,retries if needed.
        try {
            if (values[PORT] != null)
                ses_opt.setRemotePort( Integer.parseInt(values[PORT]) );
            if (values[RETRIES] != null)
                session.setRetries( Integer.parseInt(values[RETRIES]) );
            if (values[TIMEOUT] != null)
                session.setTimeout( Integer.parseInt(values[TIMEOUT]) );
        }
        catch (NumberFormatException ex) {
            System.err.println("Invalid Integer Arg");
        }

        session.setProtocolOptions(ses_opt);

        session.setVersion( SnmpAPI.SNMP_VERSION_3 );

        if ((values[AUTH_PROTOCOL] != null) 
                && (values[AUTH_PASSWORD] != null) 
                && (values[NEW_AUTH_PASSWORD] != null)) {
            if(values[AUTH_PROTOCOL].equals("SHA"))
                authProtocol = USMUserEntry.SHA_AUTH;
            else 
                authProtocol = USMUserEntry.MD5_AUTH;                   
            if(authProtocol==USMUserEntry.NO_AUTH){
                System.err.println("Enter authentication protocol");
                opt.usage_error();
            }
            authPassword = values[AUTH_PASSWORD];
            newAuthPassword = values[NEW_AUTH_PASSWORD];
            if (values[PRIV_PASSWORD] != null) {
                if (values[NEW_PRIV_PASSWORD] != null) { 
                    privPassword = values[PRIV_PASSWORD];
                    newPrivPassword = values[NEW_PRIV_PASSWORD];
                }
                else
                    opt.usage_error();
            }
        }
        else if ((values[AUTH_PROTOCOL] != null) 
                || (values[AUTH_PASSWORD] != null) 
                || (values[PRIV_PASSWORD] != null)) {
            opt.usage_error();
        }
        if (values[CONTEXT_NAME] != null)
            contextName = values[CONTEXT_NAME];
        if (values[CONTEXT_ID] != null)
            contextID = values[CONTEXT_ID];


        // Build Get request PDU
        SnmpPDU pdu = new SnmpPDU();

        try {
            //Open session
            session.open();
        } catch (SnmpException e) {
            System.err.println("Error opening session:"+e.getMessage());
            System.exit(1);
        }

        // inititialize the manager by adding the user. All requests will
        // sent with this username 
        pdu.setUserName(userName.getBytes());
        try
        {
            USMUtils.init_v3_parameters(
                userName,
                authProtocol,
                authPassword, 
                privPassword,
                ses_opt.getRemoteHost(),
                ses_opt.getRemotePort(),
                session);
        }
        catch(Exception exp)
        {
            System.out.println(exp.getMessage());
            System.exit(1);
        }
        pdu.setContextName(contextName.getBytes());
        pdu.setContextID(contextID.getBytes());

        // A valid user is now configured.on the manager. 
        System.out.println("A new user " + userName + " is now " +
                             "configured on the manager"); 

    // Let's start the remote configuration.
    // Step 1. Retrive the USMUserSpinLock
        int spinLock = surg.sendSpinLockRequest(pdu,session);

        if(spinLock < 0){
            System.out.println("Error in retriving SnmpLock");
            System.exit(1);
        }

        System.out.println("Spin lock value retrived successfully\n");


        // Since we are reusing the PDU, we will remove the varbinds
        // and set the reqid to 0.
        surg.removeAllVarBinds(pdu);
        //pdu.setReqid(0);


    // Step 2. Send a multiVarBind SET request with the retrived
    // spinlock value, USMUserCloneFrom=templateuser and 
    // usmUserStatus=createAndWait.
    // Here the templateuser will be user on whose behalf the requests
    // are made.

        // Create the ConeFrom OID for the creating new instance.
        // i.e for newUserName and engineID
        byte[] engineID = ((Snmp3Message)pdu.getMsg()).
                                        getSecurity().getEngineID();
        String engID;
        try{
        engID = new String(engineID, ENC);
        }
        catch(Exception e){
        engID = new String(engineID);
        }
        int[] firstindex = surg.stringToIntegerArray(engID);
        int[] secondIndex = surg.stringToIntegerArray(newUser);
        String engIDOID = surg.intArrayToString(firstindex);
        String newUserOID = surg.intArrayToString(secondIndex);

        String cloneFromOID = USM_ENTRY + "." + CLONE_COL + "." 
                            + firstindex.length + engIDOID + "." 
                            + secondIndex.length + newUserOID;
        
        if(surg.debug)
            System.out.println("cloneFrom OID = " + cloneFromOID);


        // Create the CloneFrom OID value. This will point
        // to the existing template entry.i.e userName and engineID
        int[] cloneFromIndex = surg.stringToIntegerArray(userName);

        String userNameOID = surg.intArrayToString(cloneFromIndex);

        String cloneFromOIDValue = USM_ENTRY + "." + CLONE_COL + "." 
                    + firstindex.length + engIDOID + "."
                    + cloneFromIndex.length + userNameOID;

        if(surg.debug)
            System.out.println("cloneFrom OID value = " + cloneFromOIDValue);

        String rowStatusOID = USM_ENTRY + "." + ROW_STATUS_COL + "." +
                    + firstindex.length + engIDOID + "." + 
                    secondIndex.length + newUserOID;
        
        if(surg.debug)
            System.out.println("Row status  OID = " + rowStatusOID);

        // Create the multivarbind. 
        SnmpOID setOID1 = new SnmpOID(SPIN_LOCK_OID);
        surg.addvarbind(pdu, setOID1,"INTEGER",
                            new Integer(spinLock).toString());
        SnmpOID setOID2 = new SnmpOID(cloneFromOID); 
        surg.addvarbind(pdu, setOID2,"OID",cloneFromOIDValue);
        SnmpOID setOID3 = new SnmpOID(rowStatusOID);
        surg.addvarbind(pdu, setOID3,"INTEGER","5");


        System.out.println("\nSending a multivarbind cloneFrom request\n");

        pdu.setCommand(SnmpAPI.SET_REQ_MSG);
        try {
            // Send PDU and receive response PDU
            pdu = session.syncSend(pdu);
        } catch (SnmpException e) {
            System.err.println("Sending PDU"+e.getMessage());
            System.exit(1);
        }    
        if (pdu == null) {
            // timeout
            System.out.println("Request timed out to: " + opt.remArgs[0] );
            System.exit(1);
        }

        System.out.println("Response PDU for clonefrom  received from " +
                            pdu.getProtocolOptions().getSessionId());

        if (pdu.getErrstat() != 0){
            System.out.println("Clonefrom request returned error "
                                    + "User NOT Successfully cloned");
            System.err.println(pdu.getError());
            System.exit(1);
        }
        else 
            // print the response pdu varbinds
            System.out.println(pdu.printVarBinds());


        // Since we are reusing the PDU, we will remove the varbinds
        // and set the reqid to 0.
        surg.removeAllVarBinds(pdu);
        //pdu.setReqid(0);

        if(authPassword.length() > 0 && newAuthPassword.length() > 0)   {       
        // Get the SpinLock to use in the next SET request.
        spinLock = surg.sendSpinLockRequest(pdu,session);
        if(spinLock < 0){
            System.out.println("Error in retriving SnmpLock");
            System.exit(1);
        }

        // Since we are reusing the PDU, we will remove the varbinds
        // and set the reqid to 0.
        surg.removeAllVarBinds(pdu);
        //pdu.setReqid(0);


    // Step 3. Send a multiVarBind SET request with the retrived
    // spinlock value, USMUserKeyChange=keyChangeValue and 
    // usmUserPublic=randomValue.

        // Initialize the random value based on the protocol used.
        byte[] random;
        if(authProtocol == USMUserEntry.MD5_AUTH)
            random = new byte[AUTH_MD5_LEN];
        else
            random = new byte[AUTH_SHA_LEN];


        USMUserTable userTable = (USMUserTable)api.getSecurityProvider().
                                    getTable(USM_SECURITY_MODEL);
        USMUserEntry entry = null;
        try{
             entry = userTable.getEntry(userName.getBytes(),
                                                engID.getBytes(ENC));
        }catch(Exception e){
             entry = userTable.getEntry(userName.getBytes(),
                                                engID.getBytes());
            }
        byte[] oldKey = entry.getAuthKey();

        // Generate the keyChange value based on the secret
        // authKey of the clone-from user and the secret key
        // to be used for the new user. Let us call this akcValue.

        String akcValue = surg.getKeyChangeValue(newUser,engID,authProtocol,
                                newAuthPassword,oldKey,random,false);

        // This is just ot verify the keyChange. This will give
        // the original key taking the keyChange. (This is what is 
        // done on the agent side.

        if(authProtocol == USMUserEntry.MD5_AUTH){
            try{
            random = akcValue.substring(0,AUTH_MD5_LEN).getBytes(ENC);
            }catch(Exception e){
            random = akcValue.substring(0,AUTH_MD5_LEN).getBytes();
            }
        }
        else{
            try{
            random = akcValue.substring(0,AUTH_SHA_LEN).getBytes(ENC);
            }catch(Exception e){
            random = akcValue.substring(0,AUTH_SHA_LEN).getBytes();
            }
        }
        String keyChangeOID = USM_ENTRY + "." + KEY_CHANGE_COL + "." 
                + firstindex.length + engIDOID + "." 
                + secondIndex.length + newUserOID;

        String randomOID = USM_ENTRY + "." + USM_PUBLIC_COL + "." +
                + firstindex.length + engIDOID + "." + 
                secondIndex.length + newUserOID;
                

        setOID1 = new SnmpOID(SPIN_LOCK_OID);
        surg.addvarbind(pdu, setOID1,"INTEGER",
                            new Integer(spinLock).toString());
        
        setOID2 = new SnmpOID(keyChangeOID); //check this up
        surg.addvarbind(pdu, setOID2,"STRING",akcValue);

        setOID3 = new SnmpOID(randomOID);
        String randomString;
        try{
            randomString = new String(random,ENC);
        }catch(Exception e){
            randomString = new String(random);
        }
        surg.addvarbind(pdu, setOID3,"STRING", randomString);

        System.out.println("Sending a request to set the authKeyChange\n");

        pdu.setCommand( SnmpAPI.SET_REQ_MSG );
        try {
            // Send PDU and receive response PDU
            pdu = session.syncSend(pdu);
        } catch (SnmpException e) {
            System.err.println("Sending PDU"+e.getMessage());
            System.exit(1);
        }    
        if (pdu == null) {
            // timeout
            System.out.println("Request timed out to: " + opt.remArgs[0] );
            System.exit(1);
        }

        System.out.println("Response PDU for keyChange  received from " +
                            pdu.getProtocolOptions().getSessionId());

        if (pdu.getErrstat() != 0){
            System.out.println("KeyChange SET request returned error "
                                    + "User NOT Successfully cloned");
            System.err.println(pdu.getError());
            System.exit(1);
        }
        else 
            // print the response pdu varbinds
            System.out.println(pdu.printVarBinds());


        // Since we are reusing the PDU, we will remove the varbinds
        // and set the reqid to 0.
        surg.removeAllVarBinds(pdu);
        //pdu.setReqid(0);


    // Step 4. GET usmUserPulic and check it has randomValue

        // Get the usmUserPublic value
        pdu.setCommand( SnmpAPI.GET_REQ_MSG );
        
        SnmpOID oid = new SnmpOID(randomOID);
        if (oid.toValue() == null) 
            System.err.println("Invalid OID argument: " + randomOID);
        else pdu.addNull(oid);
         
        try {
            // Send PDU and receive response PDU
            pdu = session.syncSend(pdu);
        } catch (SnmpException e) {
            System.err.println("Sending PDU "+e.getMessage());
            System.exit(1);
        }    
        if (pdu == null) {
            // timeout
            System.out.println("Request timed out to: " + opt.remArgs[0] );
            System.exit(1);
        }

        // print and exit
        System.out.println("Response PDU for usmUserPublic  received from " +
                            pdu.getProtocolOptions().getSessionId());
            
        // Check for error in response
        if (pdu.getErrstat() != 0){
            System.out.println("usmUserPublic GET request returned error "
                                    + "User NOT Successfully cloned");
            System.err.println(pdu.getError());
            System.exit(1);
        }
        else 
            // print the response pdu varbinds
            System.out.println(pdu.printVarBinds());

        String userPublic = (pdu.getVariable(0)).toString();
        String tempRandom;
        try{
            tempRandom = new String(random,ENC);
        }catch(Exception e){
            tempRandom = new String(random);
        }
        if(userPublic.equals(tempRandom))
            System.out.println("usmUserPulic value is set appropriately\n");
        else{
            System.out.println("usmUserPulic value is NOT set appropriately");
            System.out.println("User NOT Successfully cloned");
            System.exit(1);
        }


        // Since we are reusing the PDU, we will remove the varbinds
        // and set the reqid to 0.
        surg.removeAllVarBinds(pdu);
        //pdu.setReqid(0);


        // Start the privKeyChange
        if(privPassword.length()>0 && newPrivPassword.length()>0)
            {
            newPrivPassword = values[NEW_PRIV_PASSWORD];
            SnmpPDU pdu1 = new SnmpPDU();
            pdu1.setUserName(userName.getBytes());
            pdu1.setContextName(contextName.getBytes());
            pdu1.setContextID(contextID.getBytes());
            // Step 1. Retrive the USMUserSpinLock
            spinLock = surg.sendSpinLockRequest(pdu1,session);

            if(spinLock < 0){
                System.out.println("Error in retriving SnmpLock");
                System.exit(1);
            }

            System.out.println("Spin lock value retrived successfully\n");


            // Since we are reusing the PDU, we will remove the varbinds
            // and set the reqid to 0.
            surg.removeAllVarBinds(pdu1);
            // Step 3. Send a multiVarBind SET request with the retrived
            // spinlock value, USMUserKeyChange=keyChangeValue and 
            // usmUserPublic=randomValue.

            // Initialize the random value based on the protocol used.
            byte[] priv_random = new byte[16]; // always 16 for Priv


            byte[] oldPrivKey = entry.getPrivKey();
        
            // Generate the keyChange value based on the secret
            // privKey of the clone-from user and the secret key
            // to be used for the new user. Let us call this pkcValue.
            String pkcValue = surg.getKeyChangeValue(newUser,engID,USMUserEntry.MD5_AUTH,newPrivPassword,oldPrivKey,random,true);

        // This is just to verify the keyChange. This will give
        // the original key taking the keyChange. (This is what is 
        // done on the agent side.
            try{
            priv_random = pkcValue.substring(0,16).getBytes(ENC);
            }catch(Exception e){
            priv_random = pkcValue.substring(0,16).getBytes();
            }
            keyChangeOID = USM_ENTRY + "." + PRIV_KEY_CHANGE_COL + "." 
                + firstindex.length + engIDOID + "." 
                + secondIndex.length + newUserOID;

            randomOID = USM_ENTRY + "." + USM_PUBLIC_COL + "." +
                + firstindex.length + engIDOID + "." + 
                secondIndex.length + newUserOID;
                

            setOID1 = new SnmpOID(SPIN_LOCK_OID);
            surg.addvarbind(pdu1, setOID1,"INTEGER",
                            new Integer(spinLock).toString());
        
            setOID2 = new SnmpOID(keyChangeOID); //check this up
            surg.addvarbind(pdu1, setOID2,"STRING",pkcValue);

            setOID3 = new SnmpOID(randomOID);
            try{
                randomString = new String(priv_random,ENC);
            }catch(Exception e){
            randomString = new String(priv_random);
            }
            surg.addvarbind(pdu1, setOID3,"STRING", randomString);

            System.out.println("Sending a request to set the privKeyChange\n");

            pdu1.setCommand( SnmpAPI.SET_REQ_MSG );
            try {
                // Send PDU and receive response PDU
                pdu1 = session.syncSend(pdu1);
            } catch (SnmpException e) {
                System.err.println("Sending PDU"+e.getMessage());
                System.exit(1);
            }    
            if (pdu1 == null) {
            // timeout
                System.out.println("Request timed out to: " + opt.remArgs[0] );
                System.exit(1);
            }

            System.out.println("Response PDU for keyChange  received from " +
                            pdu.getProtocolOptions().getSessionId());

            if (pdu1.getErrstat() != 0){
                System.out.println("KeyChange SET request returned error "
                                    + "User NOT Successfully cloned");
                System.err.println(pdu1.getError());
                System.exit(1);
            }
        else 
            // print the response pdu varbinds
            System.out.println(pdu1.printVarBinds());


        // Since we are reusing the PDU, we will remove the varbinds
        // and set the reqid to 0.
            surg.removeAllVarBinds(pdu1);
        //pdu.setReqid(0);


        // Step 4. GET usmUserPulic and check it has randomValue

        // Get the usmUserPublic value
            pdu1.setCommand( SnmpAPI.GET_REQ_MSG );
        
            oid = new SnmpOID(randomOID);
            if (oid.toValue() == null) 
                System.err.println("Invalid OID argument: " + randomOID);
            else pdu1.addNull(oid);
         
            try {
            // Send PDU and receive response PDU
                pdu1 = session.syncSend(pdu1);
            } catch (SnmpException e) {
                System.err.println("Sending PDU "+e.getMessage());
                System.exit(1);
            }    
            if (pdu1 == null) {
            // timeout
                System.out.println("Request timed out to: " + opt.remArgs[0] );
                System.exit(1);
            }

        // print and exit
            System.out.println("Response PDU for usmUserPublic  received from " +
                        pdu.getProtocolOptions().getSessionId());
            
        // Check for error in response
        if (pdu1.getErrstat() != 0){
            System.out.println("usmUserPublic GET request returned error "
                                    + "User NOT Successfully cloned");
            System.err.println(pdu1.getError());
            System.exit(1);
        }
        else 
            // print the response pdu varbinds
            System.out.println(pdu1.printVarBinds());

        userPublic = (pdu1.getVariable(0)).toString();
        try{
            tempRandom = new String(priv_random,ENC);
        }catch(Exception e){
            tempRandom = new String(priv_random);
        }
        if(userPublic.equals(tempRandom))
            System.out.println("usmUserPulic value is set appropriately\n");
        else{
            System.out.println("usmUserPulic value is NOT set appropriately");
            System.out.println("User NOT Successfully cloned");
            System.exit(1);
        }
        }
    }  // end of if(authPassword.length() > 0 .....)        

    // Step 5. Activate the new user by setting the usmUserStatus=active 

        setOID3 = new SnmpOID(rowStatusOID);
        surg.addvarbind(pdu, setOID3,"INTEGER","1");

        System.out.println("Sending a request to set  rowStatus value to active\n");

        pdu.setCommand( SnmpAPI.SET_REQ_MSG );
        try {
            // Send PDU and receive response PDU
            pdu = session.syncSend(pdu);
        } catch (SnmpException e) {
            System.err.println("Sending PDU"+e.getMessage());
            System.exit(1);
        }    
        if (pdu == null) {
            // timeout
            System.out.println("Request timed out to: " + opt.remArgs[2] );
            System.exit(1);
        }

        System.out.println("Response PDU for set row status active " +
                " received from " + pdu.getProtocolOptions().getSessionId());

        if (pdu.getErrstat() != 0){
            System.out.println("Row Status SET request returned error "
                                    + "User NOT Successfully cloned");
            System.err.println(pdu.getError());
            System.exit(1);
        }
        else{ 
            // print the response pdu varbinds
            System.out.println(pdu.printVarBinds());
            System.out.println("User  S u c c e s s f u l l y  cloned !!!");
        }

        // close session
        session.close();
        // stop api thread
        api.close();
    
        System.exit(0);

    }


    int sendSpinLockRequest(SnmpPDU pdu,SnmpSession session)
    {
        // Get the usmUserSpinLock value. 
        System.out.println("\nSending a request for retriving the " +
                                            " usmUserSpinLock value\n");
        pdu.setCommand( SnmpAPI.GET_REQ_MSG );
        SnmpOID oid = new SnmpOID(SPIN_LOCK_OID);
        if (oid.toValue() == null) 
            System.err.println("Invalid OID argument: " + SPIN_LOCK_OID);
        else 
            pdu.addNull(oid);
         
        try {
            // Send PDU and receive response PDU
            pdu = session.syncSend(pdu);
        } catch (SnmpException e) {
            System.err.println("Sending PDU "+e.getMessage());
            // System.exit(1);
            return -1;
        }    
        if (pdu == null) {
            // timeout
            System.out.println("Request timed out to: remote host ");
            // System.exit(1);
            return -1;
        }

        // print and exit
        System.out.println("Response PDU for usmUserSpinLock received from " +
                            pdu.getProtocolOptions().getSessionId());
            
        // Check for error in response
        if (pdu.getErrstat() != 0){
            System.err.println(pdu.getError());
            //System.exit(1);
            return -1;
        }
        else 
            // print the response pdu varbinds
            System.out.println(pdu.printVarBinds());

        SnmpVarBind vb = pdu.getVariableBinding(0);
        SnmpVar var = vb.getVariable();
        int spinLock = Integer.parseInt(var.toString()); 
        return spinLock;

    }



    String getKeyChangeValue(String user,String engID,
                            int protocol,String password,
                            byte[] keyOld,byte[] random,boolean isPriv)
    {
        byte[] engineID = null;
        try {
            engineID = engID.getBytes(ENC);
        }catch(Exception ex)    {
            engineID = engID.getBytes();
        }
        byte[] localizedKey = USMUtils.password_to_key(protocol,
                                                password.getBytes(),
                                                password.getBytes().length, 
                                                engineID);
        int keyLength = AUTH_MD5_LEN;
        int hashLength = AUTH_MD5_LEN;
        if(protocol == USMUserEntry.SHA_AUTH && !isPriv)
        {
            keyLength = AUTH_SHA_LEN;
            hashLength = AUTH_SHA_LEN;
        }
    
        if(debug){
            System.out.println("The old localized key is " + 
                    USMUtils.printOctets(keyOld, keyOld.length) + "\n");

            System.out.println("The new localized key is " + 
                    USMUtils.printOctets(localizedKey, localizedKey.length) 
                                                                + "\n"); 
        }

        byte[] keyChange = USMUtils.getKeyChange(protocol,true,keyLength,
                                        hashLength,localizedKey,keyOld,random);


        if(debug){
            System.out.println("The keyChange is " + 
                USMUtils.printOctets(keyChange, keyChange.length) + "\n");

            System.out.println("The random is " + 
                    USMUtils.printOctets(random, random.length) + "\n");
        }
        String kChange;
        try{
        kChange =  new String(keyChange,ENC);
        }
        catch(Exception e){
        kChange =  new String(keyChange);
        }
        return kChange;
    }


    void removeAllVarBinds(SnmpPDU pdu)
    {
        int size = pdu.getVariableBindings().size();
        for(int i = 0; i < size; i++)
        {
            pdu.removeVariableBinding(0);
        }
    }

    static public int[] stringToIntegerArray(String str){
        int[] instanceOID = new int[str.length()];
        byte[] temp = null;
        try
        {
            temp = str.getBytes(ENC);
        }
        catch(Exception exp)
        {
            temp = str.getBytes();
        }
        for (int i=0;i<temp.length;i++)
            instanceOID[i] = (int)(temp[i] & 0xff);
        return instanceOID;
    }

    static String intArrayToString(int[] intArray) {
        StringBuffer s = new StringBuffer();
        for (int i=0;i<intArray.length;i++) 
            s.append("."+Integer.toString(intArray[i]));
                return s.toString();
    }



 /** <img SRC="images/v3only.jpg" ALT="v3 only"> adds the varbind with specified oid, type and value to the pdu */
    static void addvarbind(SnmpPDU pdu, SnmpOID oid, String type, String value)
    {        
        byte dataType ;
        if (type.equals("INTEGER")) {
            dataType = SnmpAPI.INTEGER;
        } else if (type.equals("STRING")) {
            dataType = SnmpAPI.STRING;
        } else if (type.equals("GAUGE")) {
            dataType = SnmpAPI.GAUGE;
        } else if (type.equals("TIMETICKS")) {
            dataType = SnmpAPI.TIMETICKS;
        } else if (type.equals("OPAQUE")) {
            dataType = SnmpAPI.OPAQUE;
        } else if (type.equals("IPADDRESS")) {
            dataType = SnmpAPI.IPADDRESS;
        } else if (type.equals("COUNTER")) {
            dataType = SnmpAPI.COUNTER;
        } else if (type.equals("OID")) { 
            dataType = SnmpAPI.OBJID;
        } else if (type.equals("BITS")) { 
            dataType = SnmpAPI.STRING;
        } else { 
            System.err.println("Invalid variable type: " + type);
            return;
        }

        SnmpVar var = null;
        try {
            // create SnmpVar instance for the value and the type
            var = SnmpVar.createVariable( value, dataType );
        }
        catch(SnmpException e){
            System.err.println("Cannot create variable: " + 
                                    oid + " with value: " + value);
            return;
        }
        //create varbind
        SnmpVarBind varbind = new SnmpVarBind(oid, var);
        // add variable binding
        pdu.addVariableBinding(varbind);

    }

}
