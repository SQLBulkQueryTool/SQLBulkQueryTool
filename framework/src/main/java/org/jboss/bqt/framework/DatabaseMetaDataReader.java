package org.jboss.bqt.framework;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class DatabaseMetaDataReader {	

	private Connection connection;
	
	private String catalog_pattern = "%";
	private String schema_pattern = "%";
	private String table_pattern = "%";
	private String[] table_types = new String[] {"TABLE","VIEW"};
	

	private boolean selectstar = false;
	private boolean selectcolumns = true;

	private List<String> queries = null;

	public DatabaseMetaDataReader(Connection connection, Properties properties) {
		this.connection = connection;
		
		if (properties != null) {
			catalog_pattern = properties.getProperty(ConfigPropertyNames.DATABASE_METADATA_OPTIONS.CATALOG_PATTERN, "%");
			schema_pattern = properties.getProperty(ConfigPropertyNames.DATABASE_METADATA_OPTIONS.SCHEMA_PATTERN, "%");
			table_pattern = properties.getProperty(ConfigPropertyNames.DATABASE_METADATA_OPTIONS.TABLENAME_PATTERN, "%");
			String tt = properties.getProperty(ConfigPropertyNames.DATABASE_METADATA_OPTIONS.TABLE_TYPES, "TABLE,VIEW");
			
			table_types = StringUtils.split(tt, ","); //$NON-NLS-1$
		}
	}

	public void createSelectStar(boolean selectstar) {
		this.selectstar = selectstar;
	}

	public void createSelectColumns(boolean selectcolumns) {
		this.selectcolumns = selectcolumns;
	}

	@SuppressWarnings("unchecked")
	public List<String> getQueries() throws Exception {
		if (queries != null)
			return queries;

		ResultSet rs = null;

		try {

			DatabaseMetaData dbmd = connection.getMetaData();

			rs = dbmd.getTables(this.catalog_pattern, this.schema_pattern, this.table_pattern, this.table_types);
			queries = loadQueries(rs, dbmd);

			FrameworkPlugin.LOGGER.debug("DatabaseMetadataReader:  processed loading queries " + queries.size());
		} catch (SQLException e) {
			// SQLException has child exceptions/ navigate the children
			FrameworkPlugin.LOGGER.error(e, "Error reading DatabaseMetadata when trying to build queries");			
			queries = Collections.EMPTY_LIST;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Throwable t) {

				}
			}

		}
		this.connection = null;

		return queries;

	}

	private List<String> loadQueries(ResultSet results, DatabaseMetaData dbmd)
			throws SQLException {
		List<String> queries = new ArrayList<String>();

		int queryCnt = 0;
		// Walk through each row of results	

		for (int i = 1; results.next(); i++) {
			queryCnt++;
			
			String tname = results.getString("TABLE_NAME");
			String schemaname = results.getString("TABLE_SCHEM");
			String catname = results.getString("TABLE_CAT");

			if (this.selectcolumns) {
				if (tname != null) {

						String sql = createSQL(catname, schemaname, tname, dbmd);
						queries.add(sql);

				}
			}

			if (this.selectstar) {
				queries.add("Select * From " + tname);
			}
		}

		return queries;

	}

	private String createSQL(String catname, String schemaname, String tname,
			DatabaseMetaData dbmd) throws SQLException {

		// Walk through each row of results to get each column in the query
		
		List<String[]> rows = new ArrayList<String[]>();

		StringBuffer sb = new StringBuffer("Select ");
		Set<String> cnames = new HashSet<String>();

		ResultSet results = null;
		try {
			results = dbmd.getColumns((catname != null ? catname : "%"),
					(schemaname != null ? schemaname : "%"), tname,
					"%");
			

			for (int row = 1; results.next(); row++) {
				
				String[] rowValue = new String[3];
	
				
				rowValue[1] = results.getString("TABLE_SCHEM");
				rowValue[0] = results.getString("TABLE_CAT");
				rowValue[2] = results.getString("COLUMN_NAME");
				
				rows.add(rowValue);	
			}
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (Throwable t) {

				}
			}
		}
			
		int i=0;
		for (String[] rowValue : rows) {

			i++;
			boolean isSelectable = true;
//			boolean isSelectable = isColumnSelectable(rowValue[0], rowValue[1],
//					tname, rowValue[2], dbmd);

			if (!isSelectable) {
				FrameworkPlugin.LOGGER.trace("Column {0} is not selectable", rowValue[2]);
				continue;
			}

			if (i > 1) {
				sb.append(", ");
			}

			if (cnames.contains(rowValue[2])) {
				FrameworkPlugin.LOGGER.trace("Duplicate Column {0} in table {1}", rowValue[2],  tname);
			}
			cnames.add(rowValue[2]);

			sb.append(rowValue[2]);
		}

		sb.append(" From " + tname);
		return sb.toString();
	}

//	private boolean isColumnSelectable(String catalogname, String schemaname,
//			String tablename, String columname, DatabaseMetaData dbmd)
//			throws SQLException {
//
//		ResultSet rs = null;
//		try {
//			rs = dbmd.getColumnPrivileges((catalogname != null ? catalogname
//					: "%"), (schemaname != null ? schemaname : "%"), tablename,
//					columname);
//
//			String priviledge = rs.getString("PRIVILEGE");
//
//			if (priviledge != null && priviledge.toUpperCase().equals("SELECT")) {
//				return true;
//			}
//			return false;
//
//		} finally {
//			if (rs != null) {
//				try {
//					rs.close();
//				} catch (Throwable t) {
//
//				}
//			}
//		}
//
//	}

}
