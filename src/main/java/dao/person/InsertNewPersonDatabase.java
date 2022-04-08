package dao.person;

import resource.Person;

import java.sql.*;

/**
 * @author Riccardo Tumiati
 */
public class InsertNewPersonDatabase {
    private static final String STATEMENT = "INSERT INTO person VALUES (?,?,?,?,?,?,?,?,?)";

    private final Connection conn;
    private final Person p;

    public InsertNewPersonDatabase(final Connection conn, final Person p) {
        this.conn = conn;
        this.p = p;
    }

    public void execute() throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(STATEMENT)) {
            ps.setString(1, p.getEmail());
            ps.setString(2, p.getName());
            ps.setString(3, p.getSurname());
            ps.setString(4, p.getPsw());
            ps.setString(5, p.getTaxCode());
            ps.setDate(6, p.getBirthDate());
            ps.setString(7, p.getTelephone());
            ps.setString(8, p.getAddress());
            ps.setString(9, p.getAvatarPath());
            ps.execute();
        } finally {
            conn.close();
        }
    }
}