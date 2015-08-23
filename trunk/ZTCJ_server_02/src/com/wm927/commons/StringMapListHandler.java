package com.wm927.commons;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

public class StringMapListHandler extends AbstractListHandler<Map<String, Object>>{
	/**
	 * <code>ResultSetHandler</code> implementation that converts a
	 * <code>ResultSet</code> into a <code>List</code> of <code>Map</code>s.
	 * This class is thread safe.
	 *
	 * @see org.apache.commons.dbutils.ResultSetHandler
	 */

	    /**
	     * The RowProcessor implementation to use when converting rows
	     * into Maps.
	     */
	    private final RowProcessor convert;

	    /**
	     * Creates a new instance of MapListHandler using a
	     * <code>BasicRowProcessor</code> for conversion.
	     */
	    public StringMapListHandler() {
	        this(StringArrayHandler.ROW_PROCESSOR);
	    }

	    /**
	     * Creates a new instance of MapListHandler.
	     *
	     * @param convert The <code>RowProcessor</code> implementation
	     * to use when converting rows into Maps.
	     */
	    public StringMapListHandler(RowProcessor convert) {
	        super();
	        this.convert = convert;
	    }

	    /**
	     * Converts the <code>ResultSet</code> row into a <code>Map</code> object.
	     * @param rs <code>ResultSet</code> to process.
	     * @return A <code>Map</code>, never null.
	     *
	     * @throws SQLException if a database access error occurs
	     *
	     * @see org.apache.commons.dbutils.handlers.AbstractListHandler#handle(ResultSet)
	     */
	    @Override
	    protected Map<String, Object> handleRow(ResultSet rs) throws SQLException {
	        return this.convert.toMap(rs);
	    }
}