package edu.whu.hyk;

import edu.whu.hyk.encoding.Encoder;
import edu.whu.hyk.model.Point;
import edu.whu.hyk.model.PostingList;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;

public class DBTest {
    @Test
    public void build(){
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:syd.db");
            String sql = "select * from sample limit 100;";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.isClosed()) {
                    System.out.println("no result is found!");
                }
                while (rs.next()) {
                    System.out.println(rs.getInt("pid"));
                }
                rs.close();
            } catch (SQLException e) {
                throw new IllegalStateException(e.getMessage());

            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
}
