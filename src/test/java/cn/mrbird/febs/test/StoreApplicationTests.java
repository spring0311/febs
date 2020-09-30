package cn.mrbird.febs.test;


import cc.mrbird.febs.system.service.IMatterService;
import cc.mrbird.febs.system.service.impl.MatterServiceImpl;
import oracle.jdbc.OracleDriver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.sql.*;


@RunWith(SpringRunner.class)
@SpringBootTest()
@SpringBootConfiguration
public class StoreApplicationTests {


    @Test
    public void contextLoads() {
        select();
    }

    @Test
    public void select() {
        Connection conn = getConnection();
        System.out.println("conn:" + conn);
        String sql = "select USERNAME from T_USER";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getString("USERNAME"));
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        Connection conn = null;
        try {
            //oraclr6:  oracleDriver:oracle.jdbc.OracleDriver@894858
            Class.forName("oracle.jdbc.OracleDriver");
            OracleDriver oracleDriver = new OracleDriver();
            System.out.println("oracleDriver:" + oracleDriver);
            //jdbc:oracle:thin:192.168.0.167:1521:ORCL
            conn = DriverManager.getConnection("jdbc:oracle:thin:@//192.168.56.1:1521/orcl", "c##wei", "123456");
        } catch (ClassNotFoundException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conn;
    }


    @Test
    public void updateUrgent() {

    }

}
