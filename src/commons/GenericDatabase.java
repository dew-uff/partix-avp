package commons;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import localqueryprocessor.dynamicrangegenerator.avp.partitiontuner.Range;

public class GenericDatabase extends AbstractDatabase implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArrayList<Range> loadPartitionedColumns(DatabaseProperties prop, String tableName, Connection con, int type) throws SQLException {
        //System.out.println("Generic Database loading partitioned columns info...");
        ArrayList<Range> rangeList = new ArrayList<Range>();
		tableName = tableName.toLowerCase();

		if (prop.containsKey(tableName.toLowerCase())) {
			String[] values = ((String) prop.get(tableName)).split(",");			
			for (int i = 0; i < values.length; i++) {
								
				String limit = "";
				java.sql.Statement stMax = con.createStatement();
				stMax.setMaxRows(1);
				ResultSet rsMax = con.createStatement().executeQuery("SELECT "+values[i]+" as maxValue FROM "+tableName+" ORDER BY "+values[i]+" DESC "+limit);
				long rangeEnd = 0;
                String rangeMax = null;
				if (type == PARTITIONED_COLUMNS){
				    if(rsMax.next())
				        rangeEnd = rsMax.getLong("maxValue");
                }else if (type == ORDER_BY_COLUMNS){
                    if(rsMax.next()){
                        rangeMax = rsMax.getString("maxValue");
                    }
                }
                    
				
				java.sql.Statement stMin = con.createStatement();
				stMin.setMaxRows(1);				
				ResultSet rsMin = stMin.executeQuery("SELECT "+values[i]+" as minValue FROM "+tableName+" ORDER BY "+values[i]+" ASC "+limit);
				long rangeInit = 0;
                String rangeMin = null;
                if (type == PARTITIONED_COLUMNS){
                    if(rsMin.next())
                        rangeInit= rsMin.getLong("minValue");
                }else if (type == ORDER_BY_COLUMNS){
                    if(rsMin.next()){
                        rangeMin = rsMin.getString("minValue");
                    }    
                }
                    
				java.sql.Statement stCardinality = con.createStatement();
				//stCardinality.setMaxRows(1);				
				ResultSet rsCardinality = stCardinality.executeQuery("select count(*) as reltuples from "+tableName);
				long cardinality = 0;
				if(rsCardinality.next()) 
					cardinality = rsCardinality.getLong("reltuples");
				
				Range range = new Range(values[i],tableName,rangeInit,rangeEnd,cardinality);
                range.setRangeMax(rangeMax);
                range.setRangeMin(rangeMin);
				rangeList.add(range);
			}
		}		
		return rangeList;
	}
}
