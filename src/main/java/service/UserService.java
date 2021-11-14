package service;

import config.ConnectionSingleton;
import model.User;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IUserService {

    public static final String SELECT_FROM_USERS = "select * from users";
    public static final String SELECT_ID_NAME_EMAIL_COUNTRY_FROM_USER_USERS = "select id,name,email,country from user.users where id = ?";
    public static final String INSERT_INTO_USER_USERS_VALUES = "insert into user.users VALUES (?,?,?,?);";
    public static final String UPDATE_USERS_SET_NAME_EMAIL_COUNTRY_WHERE_ID = "update users set name = ?,email = ?,country = ? where id = ?;";
    public static final String DELETE_FROM_USERS_WHERE_ID = "delete from users where id = ?;";

    private static final String SQL_INSERT = "insert into employee (name, salary, create_date) values (?,?,?)";
    private static final String SQL_UPDATE = "update employee set salary=? where name=?";
    private static final String SQL_TABLE_CREATE = "create table employee(" +
            " id serial," +
            " name varchar(100) not null ," +
            " salary numeric(15,2) not null ," +
            " create_date timestamp," +
            " primary key (id)" +
            ")";
    private static final String SQL_TABLE_DROP = "drop table if exists employee";

    //    public static void main(String[] args) throws SQLException {
//        UserService userService = new UserService();
//        userService.listUser();
//        userService.selectUser(1);
//       userService.insertUser(new User(4, "em", "conmeno@gmail.com", "china"));
//       userService.updateUser(new User(4, "codi", "okcon@gmail.com", "germany"));
//        userService.deleteUser(3);
//    }
    @Override
    public List<User> listUser() throws SQLException {
        List<User> userList = new ArrayList<>();
        Connection connection = ConnectionSingleton.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_FROM_USERS);
        System.out.println(statement);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String email = rs.getString("email");
            String country = rs.getString("country");
            userList.add(new User(id, name, email, country));
        }
        return userList;
    }

    @Override
    public User selectUser(int id) throws SQLException {
        User user = null;
        Connection connection = ConnectionSingleton.getConnection();
        PreparedStatement statement;
        try {
            statement = connection.prepareStatement(SELECT_ID_NAME_EMAIL_COUNTRY_FROM_USER_USERS);
            statement.setInt(1, id);
            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String country = rs.getString("country");
                user = new User(id, name, email, country);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }


    @Override
    public void insertUser(User user) throws SQLException {
        System.out.println(SELECT_FROM_USERS);
        Connection connection = ConnectionSingleton.getConnection();
        PreparedStatement statement = connection.prepareStatement(INSERT_INTO_USER_USERS_VALUES);
        statement.setInt(1, user.getId());
        statement.setString(2, user.getName());
        statement.setString(3, user.getEmail());
        statement.setString(4, user.getCountry());
        System.out.println(statement);
        statement.executeUpdate();
    }

    @Override
    public boolean updateUser(User user) throws SQLException {
        System.out.println("update");
        boolean rowUpdated;
        Connection connection = ConnectionSingleton.getConnection();
        PreparedStatement statement = connection.prepareStatement(UPDATE_USERS_SET_NAME_EMAIL_COUNTRY_WHERE_ID);
        statement.setString(1, user.getName());
        statement.setString(2, user.getEmail());
        statement.setString(3, user.getCountry());
        statement.setInt(4, user.getId());
        rowUpdated = statement.executeUpdate() > 0;
        return rowUpdated;
    }

    @Override
    public boolean deleteUser(int id) throws SQLException {
        boolean rowDeleted;
        System.out.println("delete");
        Connection connection = ConnectionSingleton.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE_FROM_USERS_WHERE_ID);
        statement.setInt(1, id);
        rowDeleted = statement.executeUpdate() > 0;
        return rowDeleted;
    }

    @Override
    public User getUserById(int id) {
        User user = null;
        String query = "{CALL GET_USER_BY_ID(?);}";

//        step1: thiet lap ket noi
        Connection connection = ConnectionSingleton.getConnection();

        try {
            // Bước 2: Tạo một câu lệnh bằng đối tượng kết nối
            CallableStatement callableStatement = connection.prepareCall(query);
            callableStatement.setInt(1, id);

            // Bước 3: Thực thi truy vấn hoặc cập nhật truy vấn
            ResultSet rs = callableStatement.executeQuery();

            // Bước 4: Xử lý đối tượng ResultSet.
            while (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String country = rs.getString("country");
                user = new User(id, name, email, country);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public void insertUserStore(User user) throws SQLException {
        String query = "{call inser_user2(?,?,?,?)}";

        Connection connection = ConnectionSingleton.getConnection();
        CallableStatement callableStatement = connection.prepareCall(query);
        callableStatement.setInt(1, user.getId());
        callableStatement.setString(2, user.getName());
        callableStatement.setString(3, user.getEmail());
        callableStatement.setString(4, user.getCountry());
        System.out.println(callableStatement);
        callableStatement.executeUpdate();
    }

    @Override
    public void addUserTransaction(User user, int[] permission) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmtAssignment = null;
        ResultSet rs = null;
        try {
            conn = ConnectionSingleton.getConnection(); //ket noi sql
            conn.setAutoCommit(false); //set autocommit = false
            pstmt = conn.prepareStatement(INSERT_INTO_USER_USERS_VALUES, Statement.RETURN_GENERATED_KEYS); //chen user
            pstmt.setInt(1, user.getId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getCountry());
            int rowAffected = pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys(); //get user id
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }

            if (rowAffected == 1) {
                String sqlPivot = "INSERT INTO user_permision (user_id,permision_id) values(?,?)";
                pstmtAssignment = conn.prepareStatement(sqlPivot);
                for (int permisionId : permission) {
                    pstmtAssignment.setInt(1, userId);
                    pstmtAssignment.setInt(2, permisionId);
                    pstmtAssignment.executeUpdate();
                }
                conn.commit();
            } else conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println(e.getMessage());
                }
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            if (pstmtAssignment != null) {
                try {
                    pstmtAssignment.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    @Override
    public void insertUpdateWithoutTransaction() {
        try {
            Connection connection = ConnectionSingleton.getConnection();
            Statement statement = connection.createStatement();
            PreparedStatement psInsert = connection.prepareStatement(SQL_INSERT);
            PreparedStatement psUpdate = connection.prepareStatement(SQL_UPDATE);

            statement.execute(SQL_TABLE_DROP);
            statement.execute(SQL_TABLE_CREATE);

            psInsert.setString(1, "quynh");
            psInsert.setBigDecimal(2, new BigDecimal(10));
            psInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            psInsert.execute();

            psInsert.setString(1, "ngan");
            psInsert.setBigDecimal(2, new BigDecimal(20));
            psInsert.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            psInsert.execute();

            psUpdate.setBigDecimal(1, new BigDecimal(999.99));
            psUpdate.setString(2, "quynh");
            psUpdate.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
