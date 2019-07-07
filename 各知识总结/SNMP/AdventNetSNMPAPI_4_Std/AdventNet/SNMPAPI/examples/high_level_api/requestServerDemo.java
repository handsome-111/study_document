/*
    $Id: requestServerDemo.src,v 1.3 2002/09/09 05:36:28 tonyjpaul Exp $
*/
/*
 * @(#)requestServerDemo.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the COPYRIGHTS file for more details.
 */

/** 
 *  An example of using the SnmpRequestServer object in your code.
 *  This is an applet example.  
 *  Please refer requestServerDemo.html for parameters. 
 **/

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.applet.Applet;

import com.adventnet.snmp.beans.*;

public class requestServerDemo extends Applet implements ActionListener {

    // Some AWT widgets we'll use
    Label l1;
    Button b1;
    TextField t1;

    SnmpRequestServer server;  // The server class instance we'll use

    /** The init method is first called for applets.  **/
    public void init() {
    l1 = new Label ( getParameter("OID") );
    b1 = new Button();
    b1.setLabel("Get Agent Data");

    t1 = new TextField("The data from the agent is shown here");

    //Listen for actions on button
    b1.addActionListener(this);

    //Add Components using the default FlowLayout. 
    add(l1);
    add(t1);
    add(b1);
    }


    /** The start method is called when page with applet is visited.  **/
    public void start() {
    try {  // setup the SnmpRequestServer object as desired 
           // use applet parameters wherever possible for easy changes
        server = new SnmpRequestServer(this);

        // load MIBs to allow us to use names
        if(getParameter("MIBS") != null)
			server.loadMibs( getParameter("MIBS") );  
        if(getParameter("HOSTNAME") != null)
			server.setTargetHost( getParameter("HOSTNAME") );
        if(getParameter("COMMUNITY") != null)
			server.setCommunity( getParameter("COMMUNITY") );
        if(getParameter("SNMP_PORT") != null)
            server.setTargetPort(Integer.parseInt(getParameter("SNMP_PORT")));
        if(getParameter("USERNAME") != null)
			server.setPrincipal( getParameter("USERNAME"));
        if(getParameter("AUTHPASS") != null)
			server.setAuthPassword( getParameter("AUTHPASS"));
        
		String protocol =  getParameter("AUTHPROTOCOL");
        if(protocol!=null) {
            if(protocol.equals("SHA"))
                server.setAuthProtocol(SnmpServer.SHA_AUTH);
            else
                server.setAuthProtocol(SnmpServer.MD5_AUTH);
        }
        if(getParameter("PRIVPASS") != null)
			server.setPrivPassword( getParameter("PRIVPASS"));
        if(getParameter("SEC_LEVEL") != null)
			server.setSecurityLevel((byte)Integer.parseInt(getParameter("SEC_LEVEL")));
        if(getParameter("SNMP_VERSION") != null)
			server.setSnmpVersion(Integer.parseInt(getParameter("SNMP_VERSION")));
        } catch (Exception ex) {
            System.err.println("Error in starting applet: "+ex+":"+ex.getMessage());
    }

	if(server.getSnmpVersion()==3) {
		server.create_v3_tables();
	}

    // We need to add a listener to listen for responses
    ResultAdapter listener = new ResultAdapter() {

      // This method will be invoked when the response is received
        public void setResult( ResultEvent e ) {
        try { 
            t1.setText(e.getStringValue());
        } catch (DataException de) {
            System.err.println("Error in getting agent data: "+de +
                       e.getErrorString());
        }
        }
    };

    server.addResultListener(listener);  // register for responses
    }  


    /** This method is called when the button is clicked **/
    public void actionPerformed(ActionEvent e) {

        RequestEvent evt = // generate event to the SnmpRequestServer
        new RequestEvent(b1, getParameter("OID"), RequestEvent.GET);
        server.addRequest(evt);
    }

    
}
