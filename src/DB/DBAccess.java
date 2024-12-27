package DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DBAccess {
    private Connection con;

    // Constructor: Tạo kết nối cơ sở dữ liệu
    public DBAccess() {
        MyConnection mycon = new MyConnection();
        con = mycon.getConnection(); // Lấy kết nối từ MyConnection
    }
    public int executeUpdate(String sql, Object... params) {
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            setParameters(pstmt, params);
            return pstmt.executeUpdate(); // Thực thi câu lệnh UPDATE/INSERT/DELETE
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; // Nếu có lỗi, trả về -1
        }
    }

   
    public ResultSet executeQuery(String sql, Object... params) {
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            setParameters(pstmt, params);
            return pstmt.executeQuery(); // Trả về ResultSet của câu lệnh SELECT
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Nếu có lỗi, trả về null
        }
    }
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]); // Thiết lập tham số vào PreparedStatement
        }
    }

   
    public Connection getConnection() {
        return con;
    }

  
    public void close() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Không thể đóng kết nối cơ sở dữ liệu!");
        }
    }
}
