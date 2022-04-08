package dao.emailconfirmation;

import resource.Person;

import java.sql.*;

/**
 * @author Francesco Caldicezzi
 */
public class DeleteEmailConfirmationByPersonDatabase {
    private static final String STATEMENT = "DELETE FROM emailconfirmation WHERE person=?";

    private final Connection connection;
    private final Person person;


    public DeleteEmailConfirmationByPersonDatabase(final Connection connection, final Person person) {
        this.connection = connection;
        this.person = person;
    }

    public void execute() throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement(STATEMENT)) {
            ps.setString(1, person.getEmail());
            ps.execute();
        } finally {
            connection.close();
        }
    }

}
