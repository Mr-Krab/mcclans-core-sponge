/*
 * Copyright (c) 2016 riebie, Kippers <https://bitbucket.org/Kippers/mcclans-core-sponge>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package nl.riebie.mcclans.database;

import nl.riebie.mcclans.config.Config;
import nl.riebie.mcclans.enums.DBMSType;

import java.sql.*;

public class DatabaseConnection {
    private static final String MYSQL_DRIVER_NAME = "mysql";
    private static final String SQLITE_DRIVER_NAME = "sqlite";
    private static final String MYSQL_DRIVER_CLASSPATH = "com.mysql.jdbc.Driver";
    private static final String SQLITE_DRIVER_CLASSPATH = "org.sqlite.JDBC";

    private Connection con = null;

    public boolean setupConnection(String server, int port, String database, String user, String password) {
        String driverName;
        String driverClasspath;

        DBMSType dbmsType = DBMSType.getType(Config.getString(Config.DBMS_TYPE));
        if (dbmsType.equals(DBMSType.MYSQL)) {
            driverName = MYSQL_DRIVER_NAME;
            driverClasspath = MYSQL_DRIVER_CLASSPATH;
        } else if (dbmsType.equals(DBMSType.SQLITE)) {
            driverName = SQLITE_DRIVER_NAME;
            driverClasspath = SQLITE_DRIVER_CLASSPATH;
        } else {
            return false;
        }

        String url = "jdbc:" + driverName + "://" + server + ":" + String.valueOf(port) + "/" + database;
        try {
            Class.forName(driverClasspath);
            con = DriverManager.getConnection(url, user, password);
            return true;

        } catch (Exception e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public void executeTransactionStatement(PreparedStatement statement) throws SQLException {
        statement.execute();
    }

    public void executeStatement(String statement) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement(statement);
            preparedStatement.execute();
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet executeQuery(String query) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement(query);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void startTransaction() throws SQLException {
        con.setAutoCommit(false);
    }

    public void commitTransaction() throws SQLException {
        con.commit();
        con.setAutoCommit(true);
    }

    public void cancelTransaction() {
        try {
            con.rollback();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        try {
            con.close();
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
    }

    public boolean isValid() {
        try {
            return con.isValid(5);
        } catch (SQLException e) {
            if (Config.getBoolean(Config.DEBUGGING)) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public Connection connection() {
        return con;
    }
}
