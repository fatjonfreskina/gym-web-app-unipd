package dao.lecturetimeslot;

import constants.Constants;
import resource.LectureTimeSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Get all LectureTimeSlot between the two specified dates
 *
 * @author Riccardo Forzan
 */
public class GetAllLectureTimeSlotDatabase {
    private static final String STATEMENT = "SELECT * FROM lecturetimeslot WHERE date BETWEEN ? AND ?";
    private final Connection connection;
    private final Date startDate;
    private final Date endDate;

    public GetAllLectureTimeSlotDatabase(Connection connection, Date startDate, Date endDate) {
        this.connection = connection;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public List<LectureTimeSlot> execute() throws SQLException {
        ArrayList<LectureTimeSlot> resultSet = new ArrayList<>();
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = connection.prepareStatement(STATEMENT);
            ps.setDate(1, startDate);
            ps.setDate(2, endDate);
            rs = ps.executeQuery();
            while (rs.next()) {
                String roomName = rs.getString(Constants.LECTURETIMESLOT_ROOMNAME);
                Date date = rs.getDate(Constants.LECTURETIMESLOT_DATE);
                Time startTime = rs.getTime(Constants.LECTURETIMESLOT_STARTTIME);
                int courseEditionId = rs.getInt(Constants.LECTURETIMESLOT_COURSEEDITIONID);
                String courseName = rs.getString(Constants.LECTURETIMESLOT_COURSENAME);
                String substitution = rs.getString(Constants.LECTURETIMESLOT_SUBSTITUTION);
                resultSet.add(new LectureTimeSlot(roomName, date, startTime, courseEditionId, courseName, substitution));
            }
        } finally {
            connection.close();
        }
        return resultSet;
    }

}
