package dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import resource.LectureTimeSlot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GetLectureTimeSlotsByCourseDatabase {
  private static final String STATEMENT = "SELECT * FROM lecturetimeslot WHERE courseEditionId = ? and courseName = ?";
  private final Connection connection;
  private final LectureTimeSlot lectureTimeSlot;

  public GetLectureTimeSlotsByCourseDatabase(final Connection connection, final LectureTimeSlot lectureTimeSlot) {
    this.connection = connection;
    this.lectureTimeSlot = lectureTimeSlot;
  }

  public List<LectureTimeSlot> execute() throws SQLException {
    PreparedStatement ps = null;
    ResultSet rs = null;
    List<LectureTimeSlot> result = new ArrayList<>();
    try {
      ps = connection.prepareStatement(STATEMENT);
      ps.setInt(1, lectureTimeSlot.getCourseEditionId());
      ps.setString(2, lectureTimeSlot.getCourseName());

      rs = ps.executeQuery();

      while (rs.next()) {
        String roomName = rs.getString("roomname");
        Date date = rs.getDate("date");
        Time startTime = rs.getTime("starttime");
        int courseEditionId = rs.getInt("courseeditionid");
        String courseName = rs.getString("coursename");
        String substitution = rs.getString("substitution");
        LectureTimeSlot lts = new LectureTimeSlot(roomName, date, startTime, courseEditionId, courseName, substitution);
        result.add(lts);
      }
    }  finally {
      if (rs != null)
        rs.close();
      if (ps != null)
        ps.close();
      connection.close();
    }
    return result;
  }
}
