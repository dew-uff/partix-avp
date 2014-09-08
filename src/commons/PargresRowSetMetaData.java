package commons;

import java.io.Serializable;
import java.sql.ResultSetMetaData;

import javax.sql.rowset.RowSetMetaDataImpl;


public class PargresRowSetMetaData extends RowSetMetaDataImpl implements Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3258416140135970361L;
	//private Logger logger = Logger.getLogger(PargresRowSetMetaData.class);

	public PargresRowSetMetaData(ResultSetMetaData meta) {
		try {
	
			this.setColumnCount(meta.getColumnCount());
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				this.setColumnType(i, meta.getColumnType(i));
/*				if (meta.getColumnDisplaySize(i) < 0)
					this.setColumnDisplaySize(i, 0);
				else
					this
							.setColumnDisplaySize(i, meta.getColumnDisplaySize(i));*/
				this.setColumnName(i, meta.getColumnName(i));
				this.setColumnLabel(i, meta.getColumnLabel(i));
				this.setColumnTypeName(i, meta.getColumnTypeName(i));
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			e.getMessage();
		}
	}
}
