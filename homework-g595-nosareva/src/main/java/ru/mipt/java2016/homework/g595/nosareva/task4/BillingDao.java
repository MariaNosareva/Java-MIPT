package ru.mipt.java2016.homework.g595.nosareva.task4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.mipt.java2016.homework.g595.nosareva.task1.CalculatorAlpha;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class BillingDao {
    private static final Logger LOG = LoggerFactory.getLogger(BillingDao.class);

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void postConstruct() {
        jdbcTemplate = new JdbcTemplate(dataSource, false);
        initSchema();
    }

    public void initSchema() {
        LOG.trace("Initializing schema");
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS billing");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS billing.users " +
                "(username VARCHAR NOT NULL PRIMARY KEY, " +
                "password VARCHAR, " +
                "enabled BOOLEAN)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS billing.functions " +
                "(username VARCHAR NOT NULL, " +
                "name VARCHAR NOT NULL, " +
                "parameters VARCHAR, " +
                "body VARCHAR, " +
                "PRIMARY KEY (username, name))");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS billing.variables " +
                "(username VARCHAR NOT NULL, " +
                "name VARCHAR NOT NULL," +
                "value DOUBLE NOT NULL, " +
                "PRIMARY KEY (username, name))");
        try {
            jdbcTemplate.update("INSERT INTO billing.users VALUES ('username', 'password', TRUE)");
        } catch (Exception ex) {
            LOG.trace(ex.getMessage());
        }
    }


    public BillingUser loadUser(String username) throws EmptyResultDataAccessException {
        LOG.trace("Querying for user " + username);
        return jdbcTemplate.queryForObject(
                "SELECT username, password, enabled FROM billing.users WHERE username = ?",
                new Object[]{username},
                new RowMapper<BillingUser>() {
                    @Override
                    public BillingUser mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new BillingUser(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getBoolean("enabled")
                        );
                    }
                }
        );
    }

    public CalculatorAlpha loadFunction(String username, String function) {
        return jdbcTemplate.queryForObject(
                "SELECT body, parameters FROM billing.functions WHERE username = ? AND name = ?",
                new Object[]{username, function},
                new RowMapper<CalculatorAlpha>() {
                    @Override
                    public CalculatorAlpha mapRow(ResultSet rs, int i) throws SQLException {
                        String paramsString = rs.getString("parameters");
                        List<String> parameters = Arrays.stream(
                                paramsString.split("&")).collect(Collectors.toList());
                        return new CalculatorAlpha(null, null,
                                new ArrayList<>(parameters), rs.getString("body"));
                    }
                });
    }

    public Double loadVariable(String username, String variableName) {
        return jdbcTemplate.queryForObject(
                "SELECT value FROM billing.variables WHERE username = ? AND name = ?",
                new Object[]{username, variableName},
                new RowMapper<Double>() {
                    @Override
                    public Double mapRow(ResultSet rs, int i) throws SQLException {
                        return rs.getDouble("value");
                    }
                });
    }


    public ArrayList<String> loadFunctions(String username) {
        List<Map<String, Object>> temp;
        try {
            temp = jdbcTemplate.queryForList(
                    "SELECT name FROM billing.functions WHERE username = ?", username);
        } catch (EmptyResultDataAccessException ex) {
            temp = new ArrayList<>();
        }

        return temp.stream().map(mp ->
                (String) mp.get("name")).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<String> loadVariables(String username) {
        List<Map<String, Object>> temp;
        try {
            temp = jdbcTemplate.queryForList(
                    "SELECT name FROM billing.variables WHERE username = ?", username);
        } catch (EmptyResultDataAccessException ex) {
            temp = new ArrayList<>();
        }

        return temp.stream().map(mp ->
                (String) mp.get("name")).collect(Collectors.toCollection(ArrayList::new));
    }

    public void insertVariable(String username, String variableName, Double value) {
        try {
            jdbcTemplate.update("INSERT INTO billing.variables VALUES (?, ?, ?)",
                    username, variableName, value);
        } catch (Exception ex) {
            LOG.trace(ex.getMessage());
        }
    }

    public void insertFunction(String username, String functionName, ArrayList<String> parameters,
                               String body) {
        try {
            StringBuilder params = new StringBuilder();
            for (int i = 0; i < parameters.size(); i++) {
                if (i != 0) {
                    params.append('&');
                }
                params.append(parameters.get(i));
            }
            jdbcTemplate.update("INSERT INTO billing.functions VALUES (?, ?, ?, ?)",
                    username, functionName, params.toString(), body);
        } catch (Exception ex) {
            LOG.trace(ex.getMessage());
        }
    }

    public void deleteVariable(String username, String varName) {
        try {
            jdbcTemplate.update("DELETE FROM billing.variables WHERE username = ? AND name = ?", username, varName);
        } catch (Exception ex) {
            LOG.trace(ex.getMessage());
        }
    }

    public void deleteFunction(String username, String funcName) {
        try {
            jdbcTemplate.update("DELETE FROM billing.functions WHERE username = ? AND name = ?", username, funcName);
        } catch (Exception ex) {
            LOG.trace(ex.getMessage());
        }
    }

    public void insertUser(String username, String password) {
        try {
            jdbcTemplate.update("INSERT INTO billing.users VALUES (?, ?, ?)", username, password, true);
        } catch (EmptyResultDataAccessException ex) {
            LOG.trace(ex.getMessage());
        }
    }
}
