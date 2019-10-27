package com.ibyte.common.core.util;

/**
 * 数据库操作小工具
 *
 * @author li.shangzhi
 */
public class DatabaseUtil {
	private static final int TYPE_ORACLE = 1;

	private static final String PREFIX_ORACLE = "Oracle";

	private static final int TYPE_MYSQL = 2;

	private static final String PREFIX_MYSQL = "MySQL";

	private static final int TYPE_SQLSERVER = 3;

	private static final String PREFIX_SQLSERVER = "SQLServer";

	private static int dbType;

	private static String dialect;

	/** 读-数据库方言，如MySQL55Dialect */
	public static String getDialect() {
		return dialect;
	}

	/** 写-数据库方言，如MySQL55Dialect */
	public static void setDialect(String dialect) {
		DatabaseUtil.dialect = dialect;
		if (dialect.startsWith(PREFIX_ORACLE)) {
			dbType = TYPE_ORACLE;
		} else if (dialect.startsWith(PREFIX_MYSQL)) {
			dbType = TYPE_MYSQL;
		} else if (dialect.startsWith(PREFIX_SQLSERVER)) {
			dbType = TYPE_SQLSERVER;
		}
	}

	/** 当前数据库为Oracle数据库 */
	public static boolean isOracle() {
		return dbType == TYPE_ORACLE;
	}

	/** 当前数据库为MySQL数据库 */
	public static boolean isMySQL() {
		return dbType == TYPE_MYSQL;
	}

	/** 当前数据库为SQLServer数据库 */
	public static boolean isSQLServer() {
		return dbType == TYPE_SQLSERVER;
	}
}
