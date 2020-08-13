package db;

public class MySQLDBUtil {
	//这些和database 是match的
	private static final String INSTANCE = "laiproject-instance.caizxjy15pzv.us-east-2.rds.amazonaws.com";
	private static final String PORT_NUM = "3306";
	public static final String DB_NAME = "laiproject";
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "lym2070668";
	public static final String URL = "jdbc:mysql://"
			+ INSTANCE + ":" + PORT_NUM + "/" + DB_NAME
			+ "?user=" + USERNAME + "&password=" + PASSWORD
			+ "&autoReconnect=true&serverTimezone=UTC";

}
