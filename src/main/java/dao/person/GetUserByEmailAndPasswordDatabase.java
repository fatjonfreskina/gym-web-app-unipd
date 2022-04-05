package dao.person;
import resource.Person;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetUserByEmailAndPasswordDatabase {
    private static final String STATEMENT = """
    SELECT * FROM person
    WHERE email = ? 
    AND  psw = ?;
    """;
    private final Connection connection;
    private final Person person;


    public GetUserByEmailAndPasswordDatabase(final Connection connection, final Person person) {
        this.connection = connection;
        this.person = person;
    }

    public List<Person> execute() throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        final List<Person> result = new ArrayList<>();

        try {
            pstmt = connection.prepareStatement(STATEMENT);
            pstmt.setString(1, person.getEmail());
            pstmt.setString(2, person.getPsw());
            rs = pstmt.executeQuery();

            if (rs.next())
            {
                result.add(new Person(
                        rs.getString("email"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getString("psw"), //password
                        rs.getString("taxCode"),
                        rs.getDate("birthDate"),
                        rs.getString("telephone"),
                        rs.getString("address"),
                        rs.getString("avatarPath"))
                );
            }
        } finally {
            if (rs != null)
            {
                rs.close();
            }
            if (pstmt != null)
            {
                pstmt.close();
            }
            connection.close();
        }
        return result;
    }
}