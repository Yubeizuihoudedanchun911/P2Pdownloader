package com.com.db.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.com.db.entity.FilePathEntity;

public class DownLoadFilePathDAO {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/mydatabase";
    private static final String USER = "username";
    private static final String PASS = "password";

    private static final String TABLE = "temp";

    private Connection connection;

    public DownLoadFilePathDAO() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void save(FilePathEntity filePathEntity) {
        String sql = "INSERT INTO " + TABLE + " (path, url) VALUES ( ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, filePathEntity.getId());
            preparedStatement.setString(2, filePathEntity.getPath());
            preparedStatement.setString(3, filePathEntity.getUrl());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}