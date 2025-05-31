package com.kelompok5.open_notepad.DAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.kelompok5.open_notepad.entity.Bookmark;
import com.kelompok5.open_notepad.entity.Note;

@Component
public class BookmarkDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NoteDAO noteDAO;

    public int getFromUser(String userID) {
        String sql = "SELECT COUNT(*) AS bookmark FROM Bookmarks WHERE username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[] { userID }, (rs, rowNum) -> rs.getInt("bookmark"));

        } catch (Exception e) {
            return -1;
        }
    }

    public int getFromNote(int moduleID) {
        String sql = "SELECT COUNT(*) AS bookmark FROM Bookmarks WHERE moduleID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[] { moduleID }, (rs, rowNum) -> rs.getInt("bookmark"));

        } catch (Exception e) {
            return -1;
        }

    }

    public void uploadToDatabase(Bookmark bookmark) {
        String sql = "INSERT INTO Bookmarks (username, moduleID, dateBookmarked) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, bookmark.getUserID(), bookmark.getModuleID(), bookmark.getDateBookmarked());
    }

    public void deleteBookmark(String username, String moduleID) {
        String sql = "DELETE FROM Bookmarks WHERE username = ? AND moduleID = ?";
        jdbcTemplate.update(sql, username, moduleID);
    }

    public boolean bookmarkExists(String username, String moduleID) {
        String sql = "SELECT COUNT(*) FROM Bookmarks WHERE username = ? AND moduleID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[] { username, moduleID }, Integer.class);
        return count != null && count > 0;
    }

    public List<Map<String, Object>> getBookmarkedNotes(String username) {
        String sql = "SELECT n.moduleID AS id, " +
                "       n.name AS name, " +
                "       n.course, " +
                "       n.major, " +
                "       a.username AS username, " +
                "       COALESCE(AVG(r.rating), 0) AS rating, " +
                "       COALESCE(v.total_views, 0) AS views " +
                "FROM Bookmarks b " +
                "JOIN Notes n ON b.moduleID = n.moduleID " +
                "LEFT JOIN Ratings r ON n.moduleID = r.moduleID " +
                "LEFT JOIN ( " +
                "    SELECT v.moduleID, COUNT(*) AS total_views " +
                "    FROM Views v " +
                "    GROUP BY v.moduleID " +
                ") v ON n.moduleID = v.moduleID " +
                "LEFT JOIN Accounts a ON n.username = a.username " +
                "WHERE b.username = ? AND n.visibility = 1 " +
                "GROUP BY n.moduleID, n.name, n.course, n.major, a.username, v.total_views";

        try {
            return jdbcTemplate.queryForList(sql, username);
        } catch (Exception e) {
            System.out.println("Failed query from database: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void deleteFromModule(int noteID) {
        // Implement the logic to delete the bookmark from the database
        // This could involve using JDBC or an ORM framework like Hibernate
        // Example: DatabaseConnection.deleteBookmark(this);
    }

}
