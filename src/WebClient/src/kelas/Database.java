package kelas;

public class Database {
	static String user = "root";
	static String pass = "";
	public static final String uploadDir = "C:/Users/harits/workspace/IF3110-Tubes-III/WebClient/WebContent/res/barang/";//"F:/git/IF3110-Tubes-II/uploaded/";
	public static final String WebServiceURL = /*"http://localhost:8080/KLK-WebService/";*/ "http://afternoon-river-3681.herokuapp.com/";
	
	public static String getUser() {
		return Database.user;
	}
	public static void setUser(String user) {
		Database.user = user;
	}
	public static String getPass() {
		return pass;
	}
	public static void setPass(String pass) {
		Database.pass = pass;
	}
}
