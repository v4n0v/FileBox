package ru.geekbrains.filebox.server.core.authorization;

import java.sql.*;

public class SQLLoginManager implements LoginManager {

    private Connection connection;
    private Statement statement;


    @Override
    public void init() {


        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:fileBox.db");
            statement = connection.createStatement();
            System.out.println("DB init");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }




    @Override
    public String getMail(String login) {

        try (PreparedStatement ps = connection.prepareStatement("SELECT mail FROM users WHERE login=?")) {
            ps.setString(1, login);
            try (ResultSet resultSet = ps.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getString(1);
                } else {
                    return null;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
//        return null;
    }

    @Override
    public int getSpace() {
        return 0;
    }

    @Override
    public String getLogin(String mail, String pass) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT login FROM users WHERE pass=? ;")) {
      //      ps.setString(1, mail);
            ps.setString(1, pass);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
//                    if (resultSet.getString(1) == mail && resultSet.getString(2) == pass){
//                    if (resultSet.getString(1) == mail && resultSet.getString(2) == pass){
                        return resultSet.getString(1);
//                    }
                    else {
                    return null;
                }
//                else {
//                    return null;
//                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkLogin(String login, String pass) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT login FROM users WHERE pass=? ;")) {
            //      ps.setString(1, mail);
            ps.setString(1, pass);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
//                    if (resultSet.getString(1) == mail && resultSet.getString(2) == pass){
//                    if (resultSet.getString(1) == mail && resultSet.getString(2) == pass){
                    return true;
//                    }
                else {
                    return false;
                }
//                else {
//                    return null;
//                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public void dispose() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
