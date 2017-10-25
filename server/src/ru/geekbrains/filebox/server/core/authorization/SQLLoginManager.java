package ru.geekbrains.filebox.server.core.authorization;

import java.sql.*;

public class SQLLoginManager implements LoginManager {

    private Connection connection;
    private Statement statement;

    // подгружаем БД
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

    // возвращаем имя почтивого ящика юзера по логину
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

    // возвращаем доступное место в облаке
    @Override
    public int getSpace() {
        return 0;
    }
    // проверяем логин\пароль,
    @Override
    public boolean isLoginAndPassCorrect(String login, String pass) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE login=? AND pass=? ;")) {

            ps.setString(1, login);
            ps.setString(2, pass);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return true;
                else {
                    return false;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    // лобавляем пользователя
    @Override
    public void addNewUser(String login, String mail, String pass) {

        try {
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (login , mail, pass, space) VALUES (?, ?, ?, ?) ");

            ps.setString(1, login);
            ps.setString(2, mail);
            ps.setString(3, pass);
            ps.setInt(4, 10);
            ps.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    // проверим, не занят ли логин
    public boolean isLoginBusy(String login) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE login=?")) {
            //      ps.setString(1, mail);
            ps.setString(1, login);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next())
                    return true;
                else
                    return false;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // закрываем соединение
    @Override
    public void dispose() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
