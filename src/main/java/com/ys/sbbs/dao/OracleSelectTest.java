package com.ys.sbbs.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import com.ys.sbbs.entity.User;

public class OracleSelectTest {

	public static void main(String[] args) {
		String driver = "oracle.jdbc.driver.OracleDriver";
//		String url = "jdbc:oracle:thin:@localhost:1521:xe";
		String url = "jdbc:oracle:thin:@localhost:1521/XEPDB1";
		String user = "ysuser";
		String password = "yspass";

		Connection conn = null;
		try {
			Class.forName(driver);
			System.out.println("jdbc driver 로딩 성공");
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("오라클 연결 성공");
			String sql = "select * from users where uname="+"제임스";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()) {
				User u = new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
						LocalDate.parse(rs.getString(5)), rs.getInt(6), rs.getString(7), rs.getString(8));
				System.out.println(u);
			}
			rs.close(); pstmt.close(); conn.close();
			
			
		} catch (ClassNotFoundException e) {
			System.out.println("jdbc driver 로딩 실패");
		} catch (SQLException e) {
			System.out.println("오라클 연결 실패");
		}
		
		/*접속 해제 처리*/
		try {
			conn.close();
			System.out.println("연결 해제");
		} catch (Exception e) {
			System.out.println("해제 오류");
		}
		conn=null;
	}
	

}