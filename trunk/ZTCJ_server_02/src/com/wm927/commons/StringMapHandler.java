package com.wm927.commons;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;

public class StringMapHandler implements ResultSetHandler<Map<String, Object>>{
	/**
     * The RowProcessor implementation to use when converting rows
     * into Maps.
     */
    private final RowProcessor convert;
    /**
     * Singleton processor instance that handlers share to save memory.  Notice
     * the default scoping to allow only classes in this package to use this
     * instance.
     */
    static final RowProcessor ROW_PROCESSOR = new StringBasicRowProcessor();
    /**
     * Creates a new instance of MapHandler using a
     * <code>BasicRowProcessor</code> for conversion.
     */
    public StringMapHandler() {
    	this(ROW_PROCESSOR);
    }

    /**
     * Creates a new instance of MapHandler.
     *
     * @param convert The <code>RowProcessor</code> implementation
     * to use when converting rows into Maps.
     */
    public StringMapHandler(RowProcessor convert) {
        super();
        this.convert = convert;
    }

    /**
     * Converts the first row in the <code>ResultSet</code> into a
     * <code>Map</code>.
     * @param rs <code>ResultSet</code> to process.
     * @return A <code>Map</code> with the values from the first row or
     * <code>null</code> if there are no rows in the <code>ResultSet</code>.
     *
     * @throws SQLException if a database access error occurs
     *
     * @see org.apache.commons.dbutils.ResultSetHandler#handle(java.sql.ResultSet)
     */
    public Map<String, Object> handle(ResultSet rs) throws SQLException {
        //return rs.next() ? this.convert.toMap(rs) : null;
        //return  this.convert.toMap(rs) ;//不管是否有数据，都必须返回该有的字段，只是内容为空
        Map<String, Object> result = new HashMap<String,Object>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();
        
        //传来RS可能不存在记录，但是为了得到有记录的东西，则需要执行这段
        if(rs.next()){
        	for (int i = 1; i <= cols; i++) {
         	   result.put(rsmd.getColumnLabel(i).toLowerCase(), rs.getString(i) == null ? "" : rs.getString(i));
            }
        }else{
        	for (int i = 1; i <= cols; i++) {
          	   result.put(rsmd.getColumnLabel(i).toLowerCase(), "");
             }
        }
      
        return result;
    }
   
}
