/* $Id: getTableInfo.java,v 1.3 2002/09/09 05:36:28 tonyjpaul Exp $ */ 
/*
 * @(#)getTableInfo.java
 * Copyright (c) 1996-2002 AdventNet, Inc. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 */

/**
 *  An example to get the information about the table by giving 
 *  the table column oid and mib file as input.
 */  
import com.adventnet.snmp.beans.*;
import com.adventnet.snmp.mibs.*;
import com.adventnet.snmp.snmp2.*;

import java.util.*;

public class getTableInfo {
	public static void main(String args[]){
		    			
		// check for mib_file and column oid
	    if (args.length<2){ 
			System.out.println("Usage : getTableInfo ColumnOID MIB_FILE");
			System.exit(0);
		}
		SnmpTarget target=new SnmpTarget();		
		//column oid
		String oid = args[0];
		try{
		target.loadMibs(args[1]);
		}catch(Exception ex)
		{
			System.out.println(ex);
		}
		
		MibOperations mibOps = target.getMibOperations();
		SnmpOID rootoid = mibOps.getSnmpOID(oid);
		MibNode node = mibOps.getMibNode(rootoid);
		MibNode tnode=null;
		Vector indeces =null;			
		Vector items=null;		
		if(node != null)
       	{
			tnode =node.getParent().getParent();
			if(tnode.isTable()){
      			System.out.println("Table name is       : "+tnode.getLabel());
				System.out.println("Table OID is        : "+mibOps.getSnmpOID(tnode.getLabel()));
			}
			else{
       			System.out.println("Not a Column OID");
				System.exit(0);
			}
			indeces =node.getIndexes(target.getMibOperations());   			 			
				System.out.println("Table Index Names are:");
				for(int i=0; i<indeces.size();i++)					
			    	System.out.print(indeces.elementAt(i)+","); 						 
				System.out.println();
		}
		else
		    System.out.println("Invalid column oid");   		
	
			 
		if(tnode!=null){
			System.out.println("Table Items :");		
			items=tnode.getTableItems(); 		
			if(items!=null)
				for(int i=0;i<items.size();i++)
					System.out.println(items.elementAt(i));
			else
			     System.out.println("items");			
		}
		if(items!=null){
		boolean b=false;
		for(int i=0;i<items.size();i++){
			SnmpOID columnoid=mibOps.getSnmpOID((String)items.elementAt(i)); 			
			MibNode columnMibNode=mibOps.getMibNode(columnoid);
			String colType = columnMibNode.getSyntax().toString();
			if (colType.equals("RowStatus")){
				System.out.println("Column of type RowStatus :"+columnMibNode.toString());				
				b=true;
			}
		}		
		if(!b)
			System.out.println("There is no column with type RowStatus");
		}		
		System.exit(0);
    }	
}
         
