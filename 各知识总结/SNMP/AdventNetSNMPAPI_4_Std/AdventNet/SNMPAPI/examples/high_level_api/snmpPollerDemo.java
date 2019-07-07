/*
    $Id: snmpPollerDemo.src,v 1.3 2002/09/09 05:36:28 tonyjpaul Exp $
*/
/*
 * @(#)snmpPollerDemo.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the COPYRIGHTS file for more details.
 */

/** 
 *  An example of using the SnmpPoller object in your code.
 *  This is an applet example.  
 *  Please refer snmpPollerDemo.html for parameters.
 **/

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.applet.Applet;

import com.adventnet.snmp.beans.*;


public class snmpPollerDemo extends Applet implements ActionListener {

    // Some AWT widgets we'll use
    Label l1;
    Button b1;
    TextField t1;

    SnmpPoller poller; // The SNMP poller instance we'll use

    /** The init method is first called for applets.  **/
    public void init() {
        l1 = new Label ( getParameter("OID") );
        b1 = new Button();
        b1.setLabel("Stop Polling");

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

      try {  // setup the poller object as desired - use applet parameters
        poller = new SnmpPoller(this);

        // load MIB to allow us to use names
        showStatus("Loading MIBs...");  // show something on the status bar
        if(getParameter("MIBS") != null)
			poller.loadMibs( getParameter("MIBS") );  
        showStatus("Loading MIBs done.");
        if(getParameter("HOSTNAME") != null)
			poller.setTargetHost( getParameter("HOSTNAME") );
        if(getParameter("COMMUNITY") != null)
			poller.setCommunity( getParameter("COMMUNITY") );
        if(getParameter("OID") != null)
			poller.setObjectID( getParameter("OID") );
        if(getParameter("SNMP_PORT") != null )
            poller.setTargetPort(Integer.parseInt(getParameter("SNMP_PORT")));

        if(getParameter("USERNAME") != null)
			poller.setPrincipal( getParameter("USERNAME"));
        if(getParameter("AUTHPASS") != null)
			poller.setAuthPassword( getParameter("AUTHPASS"));
        String protocol =  getParameter("AUTHPROTOCOL");
        if(protocol!=null) {
        if(protocol.equals("SHA"))
            poller.setAuthProtocol(SnmpServer.SHA_AUTH);
        else
            poller.setAuthProtocol(SnmpServer.MD5_AUTH);
        }
        if(getParameter("PRIVPASS") != null)
			poller.setPrivPassword( getParameter("PRIVPASS"));
        //poller.setSecurityLevel((byte)Integer.parseInt(getParameter("SEC_LEVEL")));
        if(getParameter("SNMP_VERSION") != null)
			poller.setSnmpVersion(Integer.parseInt(getParameter("SNMP_VERSION")));

    } catch (Exception ex) {
        System.err.println("Error in starting applet: "+ex+":"+ex.getMessage());
    }
      
	if(poller.getSnmpVersion()==3) {
		poller.create_v3_tables();
	}

    // We need to add a listener to listen for responses
    ResultAdapter listener = new ResultAdapter() {

      // This method will be invoked when the response is received
        public void setResult( ResultEvent e ) {
        try { 
            t1.setText(e.getStringValue());
        } catch (DataException de) {
            t1.setText("Error in getting agent data: "+de +
                       e.getErrorString());
        }
        }
    };

    poller.addResultListener(listener);  // listen for response events
    }  

    /** This method is called when the button is clicked **/
    public void actionPerformed(ActionEvent e) {
        
      if (poller.getPollingStatus()) {  // if polling is going on, stop it
        poller.stopPolling();
        b1.setLabel("Start Polling");

      } else { // else restart it
        poller.restartPolling();
        b1.setLabel("Stop Polling");
    } 
    }
    
}
