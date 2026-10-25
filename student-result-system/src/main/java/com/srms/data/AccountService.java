package com.srms.data;

import com.srms.config.Database;
import com.srms.model.Account;
import com.srms.security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public final class AccountService {

    private AccountService() {
    }

    public static Account authenticate(String username, String password) throws SQLException {
        String sql = """
                SELECT username, password_hash, role, full_name, registration_no
                FROM accounts
                WHERE username = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()
                        || !PasswordUtil.verify(password, result.getString("password_hash"))) {
                    return null;
                }

                return new Account(
                        result.getString("username"),
                        password,
                        result.getString("role").toLowerCase(Locale.ROOT),
                        result.getString("full_name"),
                        result.getString("registration_no"));
            }
        }
    }
}
