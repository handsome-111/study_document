/* $Id: snmpvacmconfigure.src,v 1.3.2.2 2004/03/17 10:05:30 vivek Exp $ */
/*
 * @(#)snmpvacmconfigure.java
 * Copyright (c) 1996-2004 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 * This is an example program used for adding entries to
 * the VACM tables, namely "vacmSecurityToGroupTable",
 * "vacmAccessTable" and "vacmViewTreeFamilyTable".
 */

import java.util.StringTokenizer;

import com.adventnet.snmp.snmp2.*;
import com.adventnet.snmp.snmp2.vacm.*;
import com.adventnet.snmp.snmp2.usm.*;


public class snmpvacmconfigure
{
    static private final int CREATEACCESS       = 1;
    static private final int DELETEACCESS       = 2;
    static private final int CREATESEC2GROUP    = 3;
    static private final int DELETESEC2GROUP    = 4;
    static private final int CREATEVIEW         = 5;
    static private final int DELETEVIEW         = 6;
    static private final int HELP               = 7;
    static private final int QUIT               = 8;

    private final int CREATE_AND_GO         = 4;
    private final String securityToGroupTableEntry = ".1.3.6.1.6.3.16.1.2.1.";
    private final String vacmAccessEntry = ".1.3.6.1.6.3.16.1.4.1.";
    private final String vacmFamilyEntry = ".1.3.6.1.6.3.16.1.5.2.1.";

    private String params;

    static private final String accessHelp =
"\n\n" +
"------------------------------------------------------------------------\n" +
"                           \"vacmAccessTable\"\n" +
"------------------------------------------------------------------------\n" +
"S.No ColumnName               Data Type     Size      Possible values\n" +
"------------------------------------------------------------------------\n" +
"1.   vacmGroupName            OCTET STRING  1 .. 32          -\n" +
"2.   vacmAccessContextPrefix  OCTET STRING  0 .. 32          -\n" +
"3.   vacmAccessSecurityModel  INTEGER         -       0 .. 2147483647\n" +
"4.   vacmAccessSecurityLevel  INTEGER         -       1(noAuthNoPriv)\n" +
"                                              -       2(authNoPriv)\n" +
"                                              -       3(authPriv)\n" +
"5.   vacmAccessContextMatch   INTEGER         -       1(exact)\n" +
"                                              -       2(prefix)\n" +
"6.   vacmAccessReadViewName   OCTET STRING  0 .. 32          -\n" +
"7.   vacmAccessWriteViewName  OCTET STRING  0 .. 32          -\n" +
"8.   vacmAccessNotifyViewName OCTET STRING  0 .. 32          -\n" +
"------------------------------------------------------------------------\n";

    
    static private final String securityToGroupHelp =
"\n\n" +
"------------------------------------------------------------------------\n" +
"                           \"vacmSecurityToGroupTable\"\n" +
"------------------------------------------------------------------------\n" +
"S.No ColumnName                     Data Type     Size      Possible values\n" +
"------------------------------------------------------------------------\n" +
"1.   vacmSecurityModel              INTEGER         -       0 .. 2147483647\n"+
"2.   vacmSecurityName               OCTET STRING  1 .. 32          -\n" +
"3.   vacmGroupName                  OCTET STRING  1 .. 32          -\n" +
"4.   vacmSecurityToGroupStorageType INTEGER         -       1(other)\n" +
"                                                            2(volatile)\n" +
"                                                            3(nonVolatile)\n" +
"                                                            4(permanent)\n" +
"                                                            5(readOnly)\n" +
"------------------------------------------------------------------------\n";


    static private final String familyTableHelp =
"\n\n" +
"------------------------------------------------------------------------------\n" +
"                           \"vacmViewTreeFamilyTable\"\n" +
"------------------------------------------------------------------------------\n" +
"S.No ColumnName                  Data Type          Size      Possible values\n" +
"------------------------------------------------------------------------------\n" +
"1.   vacmViewTreeFamilyViewName  OCTET STRING       1 .. 32          -\n" +
"2.   vacmViewTreeFamilySubtree   OBJECT IDENTIFIER    -              -\n" +
"3.   vacmViewTreeFamilyMask      OCTET STRING       0 .. 16          -\n" +
"------------------------------------------------------------------------------\n" ;


    private SnmpAPI api;
    private SnmpSession session;
    
    private int timeout;
    private int retries;

    private String remoteHost;
    private int remotePort;

    private int version;

    private String contextName;

    private String userName;
    private String authPassword;
    private int authProtocol;
    private String privPassword;
    private String optionString;
    private int action;

    private String helpNumber;

    public snmpvacmconfigure() throws SnmpException
    {
        api = new SnmpAPI();
        session = new SnmpSession(api);
        session.open();
        timeout = 5000;
        retries = 0;
        remoteHost = null;
        remotePort = 161;
        version = 0;
        contextName = null;

        userName = null;
        authPassword = null;
        authProtocol = USMUserEntry.NO_AUTH;
        privPassword = null;
        params = null;
        optionString = 
        "\n\n---------------------------------------\n" +
        "1. createAccess\n" +
        "2. deleteAccess\n" +
        "3. createSec2Group\n" +
        "4. deleteSec2Group\n" +
        "5. createView\n" +
        "6. deleteView\n" +
        "7. Help\n" +
        "8. Quit\n" +
        "Enter a Choice :";

        helpNumber = "";
    }

    public void setRemoteHost(String host)
    {
        this.remoteHost = host;
    }

    public void setRemotePort(int remotePort)
    {
        this.remotePort = remotePort;
    }
    
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public void setRetries(int retries)
    {
        this.retries = retries;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public void setAuthPassword(String authPassword)
    {
        this.authPassword = authPassword;
    }

    public void setAuthProtocol(int authProtocol)
    {
        this.authProtocol = authProtocol;
    }

    public void setPrivPassword(String privPassword)
    {
        this.privPassword = privPassword;
    }

    public void initDatabase(String driver, String url,
    String user, String password)throws SnmpException
    {
        try
        {
            api.initJdbcParams(driver, url, user, password);
        }
        catch(Exception exp)
        {
            throw new SnmpException(exp.toString());
        }
    }

    public void createUSMEntry() throws SnmpException
    {
        if(userName == null || userName.length() == 0)
        {
            throw new SnmpException("Invalid UserName.");
        }
        if(authPassword != null && authProtocol == USMUserEntry.NO_AUTH)
        {
            throw new SnmpException(
            "Specify the authProtocol if authPassword is specified.");
        }
        if(privPassword != null && authPassword == null)
        {
            throw new SnmpException(
            "An user cannot be \"noAuth,Priv\", " +
            "hence specify the authPassword");
        }
        USMUtils.init_v3_params(userName, authProtocol, authPassword,
            privPassword, remoteHost, remotePort, session);
        System.out.println("\n\nSuccessfully created the USM entry for " +
            remoteHost + ":" + remotePort + ":" + userName + ".");
    }

    public void close()
    {
        session.close();
        api.close();
    }


    public void processConfiguration()
    {
        int action = -1;
        String line = "";
        do
        {
            try
            {
                helpNumber = "";
                System.out.print(optionString);
                StringTokenizer st = new StringTokenizer(readLine());
                int tokens = st.countTokens();
                line = st.nextToken();
                if(tokens > 1)
                {
                    helpNumber = st.nextToken();
                }
                try
                {
                    action = Integer.parseInt(line);
                    if(!isValidOption(action))
                    {
                        System.out.println("\nInvalid option. " + action);
                        continue;
                    }
                    performAction(action);
                }
                catch(NumberFormatException exp)
                {
                    System.out.println("Invalid Option. " + line);
                    continue;
                }
            }
            catch(Exception exp)
            {
            }
        }while(action != QUIT);
    }

    private void performAction(int action)
    {
        switch(action)
        {
            case CREATEACCESS:
            {
                createAccess();
                break;
            }
            case DELETEACCESS:
            {
                deleteAccess();
                break;
            }
            case CREATESEC2GROUP:
            {
                createSecurityToGroup();
                break;
            }
            case DELETESEC2GROUP:
            {
                deleteSecurityToGroup();
                break;
            }
            case CREATEVIEW:
            {
                createView();
                break;
            }
            case DELETEVIEW:
            {
                deleteView();
                break;
            }
            case HELP:
            {
                help();
                break;
            }
        }
    }

    private SnmpPDU getSnmpPDU()
    {
        SnmpPDU pdu = new SnmpPDU();
        UDPProtocolOptions pdu_opt = new UDPProtocolOptions(remoteHost, remotePort);
        pdu.setProtocolOptions(pdu_opt);
        pdu.setVersion(version);
        pdu.setCommand(api.SET_REQ_MSG);
        if(version == SnmpAPI.SNMP_VERSION_3)
        {
            if(userName != null)
            {
                pdu.setUserName(userName.getBytes());
            }
            if(contextName != null)
            {
                pdu.setContextName(contextName.getBytes());
            }
        }
        return pdu;
    }

    private void createAccess()
    {
        try
        {
            if(params == null)
            {
                System.out.println(
                "Please enter the following in a single line " +
                "seperated by space:\n" +
                "GROUPNAME PREFIX SECURITYMODEL SECURITYLEVEL MATCH " +
                "READ WRITE NOTIFY ");
            //  + "STORAGE_TYPE");
            }
            String s = (params == null)? readLine() : params;
            StringTokenizer st = new StringTokenizer(s, " ");

            String groupName = st.nextToken();
            String prefix = st.nextToken();
            int model = Integer.parseInt(st.nextToken());
            int level = Integer.parseInt(st.nextToken());
            int match = Integer.parseInt(st.nextToken());
            String readView = st.nextToken();
            String writeView = st.nextToken();
            String notifyView = st.nextToken();
        //  int storageType = Integer.parseInt(st.nextToken());
            String index = getStringIndex(groupName) + "." +
                getStringIndex(prefix) + "." +
                model + "." + level;

            SnmpPDU pdu = getSnmpPDU();
            
            SnmpVarBind varbind = new SnmpVarBind(
                new SnmpOID(vacmAccessEntry + "9." + index),
                new SnmpInt(4));
            pdu.addVariableBinding(varbind);

            varbind = new SnmpVarBind(
                new SnmpOID(vacmAccessEntry + "4." + index),
                new SnmpInt(match));
            pdu.addVariableBinding(varbind);

            varbind = new SnmpVarBind(
                new SnmpOID(vacmAccessEntry + "5." + index),
                new SnmpString(readView));
            pdu.addVariableBinding(varbind);

            varbind = new SnmpVarBind(
                new SnmpOID(vacmAccessEntry + "6." + index),
                new SnmpString(writeView));
            pdu.addVariableBinding(varbind);

            varbind = new SnmpVarBind(
                new SnmpOID(vacmAccessEntry + "7." + index),
                new SnmpString(notifyView));
            pdu.addVariableBinding(varbind);

/*          varbind = new SnmpVarBind(
                new SnmpOID(vacmAccessEntry + "8." + index),
                new SnmpInt(storageType));
            pdu.addVariableBinding(varbind);*/

            send(pdu);
            System.out.println(
                "Successfully added an entry to the VacmAccessTable.");
        }
        catch(Exception exp)
        {
            System.out.println(
            "Unable to create an entry in VacmAccessTable. " + exp);
        }
    }

    private void deleteAccess()
    {
        try
        {
            if(params == null)
            {
                System.out.println(
                "Please enter the following in a single line " +
                "seperated by space:\n" +
                "GROUPNAME CONTEXTPREFIX SECURITYMODEL SECURITYLEVEL");
            }
            String s = (params == null)? readLine() : params;
            StringTokenizer st = new StringTokenizer(s, " ");
            String groupName = st.nextToken();
            String contextPrefix = st.nextToken();
            int securityModel = Integer.parseInt(st.nextToken());
            int securityLevel = Integer.parseInt(st.nextToken());
            String index = vacmAccessEntry + "9." +
                getStringIndex(groupName) + "." +
                getStringIndex(contextPrefix) + "." + securityModel + "." +
                securityLevel;
            SnmpOID indexOID = new SnmpOID(index);
            SnmpInt indexValue = new SnmpInt(6);
            SnmpPDU pdu = getSnmpPDU();
            SnmpVarBind varbind = new SnmpVarBind(indexOID, indexValue);
            pdu.addVariableBinding(varbind);

            send(pdu);
            System.out.println("Successfully deleted the entry " +
                "from the VacmAccessTable.");
        }
        catch(Exception exp)
        {
            System.out.println("Problem encountered while deleting access: " +
                exp.toString());
        }
    }

    private void createSecurityToGroup()
    {
        if(params == null)
        {
            System.out.println(
            "Please enter the following in a single line " +
            "seperated by space:\n" +
            "MODEL SECURITYNAME  GROUPNAME");
        //  + "STORAGE_TYPE");
        }
        String s = (params == null)? readLine() : params;
        StringTokenizer st = new StringTokenizer(s, " ");
        try
        {
            int model = Integer.parseInt(st.nextToken());
            String securityName = st.nextToken();
            String groupName = st.nextToken();
        //  int storageType = Integer.parseInt(st.nextToken());
            String index = model + "." + getStringIndex(securityName);

            SnmpPDU pdu = getSnmpPDU();

            String rowStatusOID = securityToGroupTableEntry + "5." + index;
            SnmpOID oid = new SnmpOID(rowStatusOID);
            SnmpInt rowStatusValue = new SnmpInt(4);
            SnmpVarBind varbind = new SnmpVarBind(oid, rowStatusValue);
            pdu.addVariableBinding(varbind);

            String groupNameOID = securityToGroupTableEntry + "3." + index;
            oid = new SnmpOID(groupNameOID);
            SnmpString str = new SnmpString(groupName);
            varbind = new SnmpVarBind(oid, str);
            pdu.addVariableBinding(varbind);

/*
            String storageTypeOID = securityToGroupTableEntry + "4." + index;
            oid = new SnmpOID(groupNameOID);
            SnmpInt storageTypeValue = new SnmpInt(storageType);
            varbind = new SnmpVarBind(oid, storageTypeValue);
            pdu.addVariableBinding(varbind);*/

            send(pdu);
            System.out.println("Successfully added an entry " +
                "to the SecurityToGroupTable.");
        }
        catch(Exception exp)
        {
            System.out.println(
            "Unable to create an entry in the securityToGroupTable. " + exp);
        }
    }

    private void deleteSecurityToGroup()
    {
        if(params == null)
        {
            System.out.println(
            "Please enter the following in a single line " +
            "seperated by space:\n" +
            "MODEL SECURITYNAME");
        }
        String s = (params == null)? readLine() : params;
        StringTokenizer st = new StringTokenizer(s, " ");
        try
        {
            int model = Integer.parseInt(st.nextToken());
            String securityName = st.nextToken();
            String index = model + "." + getStringIndex(securityName);
            String rowStatusOID = securityToGroupTableEntry + "5." + index;
            SnmpPDU pdu = getSnmpPDU();
            SnmpOID rowStatus = new SnmpOID(rowStatusOID);
            SnmpInt rowStatusValue = new SnmpInt(6);
            SnmpVarBind varbind = new SnmpVarBind(rowStatus, rowStatusValue);
            pdu.addVariableBinding(varbind);

            send(pdu);
            System.out.println("Successfully deleted an entry from the " +
                "SecurityToGroypTable.");
        }
        catch(Exception exp)
        {
            System.out.println(
            "Unable to delete an entry from securityToGroupTable. " +
            exp.toString());
        }
    }

    private void createView()
    {
        if(params == null)
        {
            System.out.println(
            "Please enter the following in a single line " +
            "seperated by space:\n" +
            "NAME SUBTREE MASK");
        //  + "STORAGE_TYPE");
        }
        try
        {
            String s = (params == null) ? readLine() : params;
            StringTokenizer st = new StringTokenizer(s, " ");
            String viewName = st.nextToken();
            String subtree = st.nextToken();
            SnmpOID subtreeOID = new SnmpOID(subtree);
            int[] subids = (int[])subtreeOID.toValue();
            if(subids != null)
            {
                subtree = subids.length + subtreeOID.toString();
            }
            else
            {
                throw new Exception("Invalid SUBTREE: " + subtree);
            }
            String mask = st.nextToken();
        //  int storageType = Integer.parseInt(st.nextToken());

            SnmpPDU pdu = getSnmpPDU();

            String index = getStringIndex(viewName) + "." + subtree;

            SnmpOID rowStatusOID = new SnmpOID(vacmFamilyEntry+"6."+index);
            SnmpInt rowStatus = new SnmpInt(4);
            SnmpVarBind varbind = new SnmpVarBind(rowStatusOID, rowStatus);
            pdu.addVariableBinding(varbind);

            SnmpOID maskOID = new SnmpOID(vacmFamilyEntry + "3." + index);
            SnmpString maskValue = new SnmpString(mask);
            varbind = new SnmpVarBind(maskOID, maskValue);
            pdu.addVariableBinding(varbind);

            SnmpOID familyTypeOID = new SnmpOID(vacmFamilyEntry+"4."+index);
            SnmpInt familyTypeValue = new SnmpInt(1);
            varbind = new SnmpVarBind(familyTypeOID, familyTypeValue);
            pdu.addVariableBinding(varbind);
/*
            SnmpOID storageTypeOID = new SnmpOID(vacmFamilyEntry+"5."+index);
            SnmpInt storageTypeValue = new SnmpInt(storageType);
            varbind = new SnmpVarBind(storageTypeOID, storageTypeValue);
            pdu.addVariableBinding(varbind);*/

            send(pdu);
            System.out.println("Successfully added an entry to the " +
                "VacmViewTreeFamilyTable.");
        }
        catch(Exception exp)
        {
            System.out.println(
            "Unable to create an entry in VacmViewTreeFamilyTable. " +
            exp.toString());
        }
    }

    private void deleteView()
    {
        if(params == null)
        {
            System.out.println(
            "Please enter the following in a single line " +
            "seperated by space:\n" +
            "NAME SUBTREE");
        }
        try
        {
            String s = (params == null) ? readLine() : params ;
            StringTokenizer st = new StringTokenizer(s, " ");
            String viewName = st.nextToken();
            String subtree = st.nextToken();
            SnmpOID subtreeOID = new SnmpOID(subtree);
            int[] subids = (int[])subtreeOID.toValue();
            if(subids != null)
            {
                subtree = subids.length + subtreeOID.toString();
            }
            else
            {
                System.out.println("Invalid SUBTREE: " + subtree);
            }
            String index = getStringIndex(viewName) + "." + subtree ;

            SnmpPDU pdu = getSnmpPDU();

            SnmpOID rowStatusOID = new SnmpOID(vacmFamilyEntry+"6."+index);
            SnmpInt rowStatusValue = new SnmpInt(6);
            SnmpVarBind varbind = new SnmpVarBind(rowStatusOID,rowStatusValue);
            pdu.addVariableBinding(varbind);

            send(pdu);
            System.out.println("Successfully deleted an entry from the " +
                "VacmViewTreeFamilyTable.");
        }
        catch(Exception exp)
        {
            System.out.println(
            "Unable to delete an entry from the VacmViewTreeFamilyTable. " +
            exp.toString());
        }
    }

    private void help()
    {
        int num = 0;
        try
        {
            if(!helpNumber.equals(""))
            {
                num = Integer.parseInt(helpNumber);
            }
        }
        catch(Exception exp)
        {
        }
        if(num == 0 || num == CREATEACCESS || num == DELETEACCESS)
        {
            System.out.print(accessHelp);
        }
        if(num == 0 || num == CREATESEC2GROUP || num == DELETESEC2GROUP)
        {
            System.out.print(securityToGroupHelp);
        }
        if(num == 0 || num == CREATEVIEW || num == DELETEVIEW)
        {
            System.out.print(familyTableHelp);
        }
    }

    private void send(SnmpPDU pdu) throws Exception
    {
        SnmpPDU res_pdu = session.syncSend(pdu);
        if(res_pdu != null)
        {
            if(res_pdu.getErrindex() != 0 || res_pdu.getErrstat() != 0)
            {
                throw new Exception(res_pdu.getError());
            }
        }
        else
        {
            throw new Exception("SNMP SET Request has timed out.");
        }
    }

    private String getStringIndex(String stringIndexValue)
    {
        int len = stringIndexValue.length();
        StringBuffer sb = new StringBuffer();
        sb.append(len);
        for(int i=0;i<len;i++)
        {
            sb.append(".");
            sb.append( (int)(stringIndexValue.charAt(i) & 0xff) );
        }
        return sb.toString();
    }

    private boolean isValidOption(int option)
    {
        return (option == CREATEACCESS || option == DELETEACCESS ||
            option == CREATESEC2GROUP || option == DELETESEC2GROUP ||
            option == DELETESEC2GROUP || option == CREATEVIEW ||
            option == DELETEVIEW || option == HELP || option == QUIT);
    }

    private String readLine()
    {
        char newLine = '\n';
        char ch;
        char[] array = new char[10];
        int i=0;
        String line = "";
        try
        {
            do
            {
                if(i == array.length)
                {
                    char[] dummy = new char[array.length * 2];
                    System.arraycopy(array, 0, dummy, 0, array.length);
                    array = dummy;
                }
                ch = (char)(System.in.read() & 0xff);
                array[i++] = ch;
            }
            while(ch != newLine);
            line = new String(array, 0, i-1);
        }
        catch(Exception exp)
        {
        }
        return line;
    }

    public void setDebug(boolean bool)
    {
        api.setDebug(bool);
    }

    public static void main(String[] args)
    {
        int DEBUG           = 0;
        int PORT            = 1;
        int RETRIES         = 2;
        int TIMEOUT         = 3;
    //  int STORAGE_TYPE    = 4;
        int VERSION         = 4;
        int USERNAME        = 5;
        int AUTHPROTOCOL    = 6;
        int AUTHPASSWORD    = 7;
        int PRIVPASSWORD    = 8;
        int CONTEXTNAME     = 9;
        int CONTEXTID       = 10;
        int DB_DRIVER       = 11;
        int DB_URL          = 12;
        int DB_USER         = 13;
        int DB_PASSWORD     = 14;

        int HOST            = 0;

        String usage =
            "\nsnmpget [-d] [-p port] [-r retries] [-t timeout]\n" +
        //  "[-st storage_type]\n" +
            "[-v version(v1/v2/v3)]" + 
            "[-u userName]\n" +
            "[-a auth_protocol] [-w auth_password] [-s priv_password]\n" +
            "[-n contextName] [-i contextID]\n" +
            "[-DB_driver database_driver]\n" +
            "[-DB_url database_url]\n" +
            "[-DB_username database_username]\n" +
            "[-DB_password database_password]\n" +
            "host [command]\n\n" +
            "COMMAND\n" +
            "createAccess GROUPNAME PREFIX SECURITYMODEL SECURITYLEVEL MATCH READ WRITE NOTIFY \n" +
        //  "[-st storage_type]\n" +
            "deleteAccess GROUPNAME CONTEXTPREFIX SECURITYMODEL SECURITYLEVEL\n" +
            "createSecurityToGroup MODEL SECURITYNAME GROUPNAME \n" +
        //  "[-st storage_type]\n" +
            "deleteSecurityToGroup MODEL SECURITYNAME\n" +
            "createView NAME SUBTREE MASK [-type familyType]\n" +
        //  "[-st storage_type]\n" + 
            "deleteView NAME SUBTREE\n";

        String options[] =
        {
            "-d", "-p", "-r", "-t"
        //  "-st"
            ,"-v"
            ,"-u", "-a", "-w", "-s", "-n", "-i",
            "-DB_driver", "-DB_url", "-DB_username", "-DB_password"
        };

        String values[] =
        {
            "None", null, null, null
        //  null
            ,null
            ,null, null, null, null, null, null,
            null, null, null, null
        };

        ParseOptions opt = new ParseOptions(args, options, values, usage);
        if(opt.remArgs.length == 0)
        {
            System.out.println("Host field is Mandatory.");
            System.out.println(usage);
            System.exit(1);
        }

        snmpvacmconfigure vacmConfig = null;
        try
        {
            vacmConfig = new snmpvacmconfigure();
        }
        catch(SnmpException exp)
        {
            System.out.println(exp.toString());
            System.exit(1);
        }

        vacmConfig.setRemoteHost(opt.remArgs[HOST]);
        if(values[VERSION] != null)
        {
            if(values[VERSION].equals("v1"))
            {
                vacmConfig.setVersion(SnmpAPI.SNMP_VERSION_1);
            }
            else if(values[VERSION].equals("v2"))
            {
                vacmConfig.setVersion(SnmpAPI.SNMP_VERSION_2);
            }
            else if(values[VERSION].equals("v3"))
            {
                vacmConfig.setVersion(SnmpAPI.SNMP_VERSION_3);
            }
        }
        if(vacmConfig.version == SnmpAPI.SNMP_VERSION_3)
        {
            if(values[USERNAME] == null)
            {
                System.out.println(
                "UserName should be specified in case of SNMP_3.");
                System.exit(1);
            }
            vacmConfig.setUserName(values[USERNAME]);

            vacmConfig.setAuthPassword(values[AUTHPASSWORD]);
            if(values[AUTHPROTOCOL] != null)
            {
                if(values[AUTHPROTOCOL].equals("MD5"))
                {
                    vacmConfig.setAuthProtocol(USMUserEntry.MD5_AUTH);
                }
                else if(values[AUTHPROTOCOL].equals("SHA"))
                {
                    vacmConfig.setAuthProtocol(USMUserEntry.SHA_AUTH);
                }
                else
                {
                    System.out.println("Invalid AuthProtocol. " +
                    "It can be either MD5 or SHA: " + values[AUTHPROTOCOL]);
                    System.exit(1);
                }
            }
            vacmConfig.setPrivPassword(values[PRIVPASSWORD]);
            try
            {
                vacmConfig.createUSMEntry();
            }
            catch(Exception exp)
            {
                System.out.println("Could not create USM Entry.\n" +
                "Please check the parameters: " + exp.toString());
                System.exit(1);
            }

            vacmConfig.setContextName(values[CONTEXTNAME]);
        }

        if(values[TIMEOUT] != null)
        {
            try
            {
                vacmConfig.setTimeout(Integer.parseInt(values[TIMEOUT]));
            }
            catch(NumberFormatException nfe)
            {
                System.out.println("Invalid timeout value:"+values[TIMEOUT] );
                System.exit(1);
            }
        }

        if(values[RETRIES] != null)
        {
            try
            {
                vacmConfig.setRetries(Integer.parseInt(values[RETRIES]));
            }
            catch(NumberFormatException nfe)
            {
                System.out.println("Invalid retries value:"+values[RETRIES] );
                System.exit(1);
            }
        }

        if(values[PORT] != null)
        {
            try
            {
                vacmConfig.setRemotePort(Integer.parseInt(values[PORT]));
            }
            catch(NumberFormatException nfe)
            {
                System.out.println("Invalid port:" + values[PORT]);
                System.exit(1);
            }
        }

        if(values[DEBUG].equals("Set"))
        {
            vacmConfig.setDebug(true);
        }

        if(opt.remArgs.length > 1)
        {
            String s = opt.remArgs[1];
            if(s.equals("createAccess"))
            {
                if(opt.remArgs.length == 10)
                {
                    StringBuffer sb = new StringBuffer();
                    for(int i=2;i<opt.remArgs.length;i++)
                    {
                        sb.append(opt.remArgs[i] + " ");
                    }
                /*  if(values[STORAGE_TYPE] != null)
                    {
                        sb.append(values[STORAGE_TYPE]);
                    }
                    else
                    {
                        sb.append("3");
                    }*/
                    vacmConfig.params = sb.toString();
                    vacmConfig.createAccess();
                }
                else
                {
                    System.out.println(usage);
                }
            }
            else if(s.equals("deleteAccess"))
            {
                if(opt.remArgs.length == 6)
                {
                    StringBuffer sb = new StringBuffer();
                    for(int i=2;i<opt.remArgs.length;i++)
                    {
                        sb.append(opt.remArgs[i] + " ");
                    }
                    vacmConfig.params = sb.toString();
                    vacmConfig.deleteAccess();
                }
                else
                {
                    System.out.println(usage);
                }
            }
            else if(s.equals("createSecurityToGroup"))
            {
                if(opt.remArgs.length == 5)
                {
                    StringBuffer sb = new StringBuffer();
                    for(int i=2;i<opt.remArgs.length;i++)
                    {
                        sb.append(opt.remArgs[i] + " ");
                    }
                /*  if(values[STORAGE_TYPE] != null)
                    {
                        sb.append(values[STORAGE_TYPE]);
                    }
                    else
                    {
                        sb.append("3");
                    }*/
                    vacmConfig.params = sb.toString();
                    vacmConfig.createSecurityToGroup();
                }
                else
                {
                    System.out.println(usage);
                }
            }
            else if(s.equals("deleteSecurityToGroup"))
            {
                if(opt.remArgs.length == 4)
                {
                    StringBuffer sb = new StringBuffer();
                    for(int i=2;i<opt.remArgs.length;i++)
                    {
                        sb.append(opt.remArgs[i] + " ");
                    }
                    vacmConfig.params = sb.toString();
                    vacmConfig.deleteSecurityToGroup();
                }
                else
                {
                    System.out.println(usage);
                }
            }
            else if(s.equals("createView"))
            {
                if(opt.remArgs.length == 5)
                {
                    StringBuffer sb = new StringBuffer();
                    for(int i=2;i<opt.remArgs.length;i++)
                    {
                        sb.append(opt.remArgs[i] + " ");
                    }
                /*  if(values[STORAGE_TYPE] != null)
                    {
                        sb.append(values[STORAGE_TYPE]);
                    }
                    else
                    {
                        sb.append("3");
                    }*/
                    vacmConfig.params = sb.toString();
                    vacmConfig.createView();
                }
                else
                {
                    System.out.println(usage);
                }
            }
            else if(s.equals("deleteView"))
            {
                if(opt.remArgs.length == 4)
                {
                    StringBuffer sb = new StringBuffer();
                    for(int i=2;i<opt.remArgs.length;i++)
                    {
                        sb.append(opt.remArgs[i] + " ");
                    }
                    vacmConfig.params = sb.toString();
                    vacmConfig.deleteView();
                }
                else
                {
                    System.out.println(usage);
                }
            }
            else
            {
                System.out.println(
                "COMMAND should be any one of the following:");
                System.out.println(
                "createAccess deleteAccess createSecurityToGroup");
                System.out.println(
                "deleteSecurityToGroup createView deleteView");
            }
            System.exit(1);
        }

        vacmConfig.processConfiguration();

        vacmConfig.close();
        System.exit(1);
    }
}
