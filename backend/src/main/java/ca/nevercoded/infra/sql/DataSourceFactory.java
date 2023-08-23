package ca.nevercoded.infra.sql;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {

    private static final MysqlConnectionPoolDataSource dataSource;

    static {
        dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setURL("jdbc:mysql://localhost:3306/bookstore");
        dataSource.setUser("root");
        dataSource.setPassword("root");
    }

    public static DataSource create() {
        return dataSource;
    }
}
