package com.kelompok5.open_notepad.DAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.kelompok5.open_notepad.entity.File;
import com.kelompok5.open_notepad.entity.Note;

@Component
public class NoteDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<Map<String, Object>> cachedNotes = null;

    public void clearCache() {
        cachedNotes = null;
    }

    public List<Map<String, Object>> getAllnotes() {
        if (cachedNotes != null) {
            return cachedNotes; // Return cached data
        }

        String sql = "SELECT n.moduleID AS id, " +
                "       n.name AS name, " +
                "       n.course, " +
                "       n.major, " +
                "       a.username AS username, " +
                "       COALESCE(AVG(r.rating), 0) AS rating, " +
                "       COALESCE(v.total_views, 0) AS views " +
                "FROM Notes n " +
                "LEFT JOIN Ratings r ON n.moduleID = r.moduleID " +
                "LEFT JOIN ( " +
                "    SELECT v.moduleID, COUNT(*) AS total_views " +
                "    FROM Views v " +
                "    GROUP BY v.moduleID " +
                ") v ON n.moduleID = v.moduleID " +
                "LEFT JOIN Accounts a ON n.username = a.username " +
                "WHERE n.visibility = 1 " +
                "GROUP BY n.moduleID, n.name, n.course, n.major, a.username, v.total_views";

        try {
            cachedNotes = jdbcTemplate.queryForList(sql); // Cache the result
        } catch (Exception e) {
            System.out.println("Failed query from database: " + e.getMessage());
            return null;
        }

        return cachedNotes;
    }

    public List<Map<String, Object>> searchInCachedNotes(String noteName) {
        if (cachedNotes == null) {
            getAllnotes(); // Load data into cache if not already loaded
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> note : cachedNotes) {
            String name = (String) note.get("name");
            if (name != null && name.toLowerCase().contains(noteName.toLowerCase())) {
                result.add(note);
            }
        }
        return result;
    }

    public int searchNoteID(String noteName) {
        String sql = "SELECT moduleID FROM Notes WHERE noteName = ?";
        // Querry to get note ID by name
        try {
            return jdbcTemplate.queryForObject(sql, new Object[] { noteName }, Integer.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve note ID from the database");
        }
    }

    public List<Map<String, Object>> searchByNames(String noteName) {
        String sql = "SELECT * FROM Notes WHERE noteName LIKE ?";
        // Querry to get some notes
        List<Map<String, Object>> notes = jdbcTemplate.queryForList(sql, "%" + noteName + "%");
        return notes;
    }

    public void uploadToDatabase(Note note) {
        String sql = "INSERT INTO Notes (name, description, course, major, visibility, dateUploaded, username, fileID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        // Querry to insert note
        try {
            jdbcTemplate.update(sql, note.getTitle(), note.getDescription(), note.getCourse(), note.getMajor(),
                    note.isVisibility(), note.getUploadDate(), note.getOwnerID(), note.getFile().getFileID());
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload note data to the database");
        }
    }

    public void updateToDatabase(Note note) {
        String sql = "UPDATE Notes SET name = ?, description = ?, course = ?, major = ? WHERE moduleID = ?";
        // Querry to update note
        try {
            jdbcTemplate.update(sql, note.getTitle(), note.getDescription(), note.getCourse(), note.getMajor(),
                    note.getModuleID());
        } catch (Exception e) {
            System.out.println("Error updating note: " + e.getMessage());
            throw new RuntimeException("Failed to update note in the database");
        }
    }

    public Note getFromDatabase(int noteID) {
        // Beri alias pada kolom name dari Notes dan Files
        String sql = "SELECT Notes.*, Files.fileID, Files.name AS file_name, Files.type, Files.size, Files.path " +
                "FROM Files INNER JOIN Notes ON Files.fileID = Notes.fileID " +
                "WHERE Notes.moduleID = ?";

        Note noted;
        try {
            noted = jdbcTemplate.queryForObject(sql, new Object[] { noteID }, (rs, rowNum) -> {
                Note note = new Note();
                note.setModuleID(rs.getInt("moduleID"));
                note.setOwnerID(rs.getString("username"));
                note.setTitle(rs.getString("name"));
                note.setDescription(rs.getString("description"));
                note.setCourse(rs.getString("course"));
                note.setMajor(rs.getString("major"));
                note.setUploadDate(rs.getDate("dateUploaded"));
                note.setVisibility(rs.getBoolean("visibility"));
                note.setFile(new File(
                        rs.getInt("fileID"),
                        rs.getString("file_name"),
                        rs.getString("type"),
                        rs.getLong("size"),
                        rs.getString("path")));
                return note;
            });
        } catch (Exception e) {
            System.out.println("Error retrieving note: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve note from the database");
        }
        return noted;
    }

    public void deleteFromDatabase(int noteID) {
        // Querry for deleting existing File
        String sqlFile = "SELECT fileID FROM Notes WHERE moduleID = ?";
        int fileID;
        try {
            fileID = jdbcTemplate.queryForObject(sqlFile, new Object[] { noteID }, Integer.class);
        } catch (Exception e) {
            System.out.println("Error deleting file associated with the note: " + e.getMessage());
            throw new RuntimeException("Failed to delete file associated with the note from the database");
        }

        String sql = "DELETE FROM Notes WHERE moduleID = ?";
        // Querry to delete note by ID
        try {
            jdbcTemplate.update(sql, noteID);
            jdbcTemplate.update("DELETE FROM Files WHERE fileID = ?", fileID);
        } catch (Exception e) {
            System.out.println("Error deleting note: " + e.getMessage());
            throw new RuntimeException("Failed to delete note from the database");
        }
    }

    public List<Map<String, Object>> filterNotes(String course, String sortBy, String sortOrder, boolean IF, boolean DS,
            boolean RPL, boolean IT) {
        StringBuilder sql = new StringBuilder(
                "SELECT n.moduleID AS id, " +
                        "       n.name AS name, " +
                        "       n.course AS course, " +
                        "       n.major AS major, " +
                        "       a.username AS username, " +
                        "       COALESCE(AVG(r.rating), 0) AS rating, " +
                        "       COALESCE(v.total_views, 0) AS views " +
                        "FROM Notes n " +
                        "LEFT JOIN Ratings r ON n.moduleID = r.moduleID " +
                        "LEFT JOIN ( " +
                        "    SELECT v.moduleID, COUNT(*) AS total_views " +
                        "    FROM Views v " +
                        "    GROUP BY v.moduleID " +
                        ") v ON n.moduleID = v.moduleID " +
                        "LEFT JOIN Accounts a ON n.username = a.username " +
                        "WHERE n.visibility = 1 ");

        List<Object> params = new ArrayList<>();

        // Tambahkan filter berdasarkan Major
        if (IF || DS || RPL || IT) {
            sql.append("AND (");
            boolean first = true;
            if (IF) {
                sql.append("n.major = ?");
                params.add("S1 Informatika");
                first = false;
            }
            if (DS) {
                if (!first)
                    sql.append(" OR ");
                sql.append("n.major = ?");
                params.add("S1 Data Sains");
                first = false;
            }
            if (RPL) {
                if (!first)
                    sql.append(" OR ");
                sql.append("n.major = ?");
                params.add("S1 Rekayasa Perangkat Lunak");
                first = false;
            }
            if (IT) {
                if (!first)
                    sql.append(" OR ");
                sql.append("n.major = ?");
                params.add("S1 Teknologi Informasi");
            }
            sql.append(") ");
        }

        // Tambahkan filter berdasarkan Course
        if (course != null && !course.equalsIgnoreCase("All")) {
            sql.append("AND n.course = ? ");
            params.add(course);
        }

        sql.append("GROUP BY n.moduleID, n.name, n.course, n.major, a.username, v.total_views, n.dateUploaded ");

        // Tambahkan pengurutan berdasarkan Sort By dan Sort Order
        if (sortBy != null && !sortBy.isEmpty()) {
            sql.append("ORDER BY ");
            switch (sortBy.toLowerCase()) {
                case "letter":
                    sql.append("n.name ");
                    break;
                case "rating":
                    sql.append("rating ");
                    break;
                case "date":
                    sql.append("n.dateUploaded ");
                    break;
                case "view":
                    sql.append("views ");
                    break;
                default:
                    sql.append("n.name "); // Default sorting by name
            }
            if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
                sql.append("DESC ");
            } else {
                sql.append("ASC ");
            }
        }

        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to filter notes: " + e.getMessage());
        }
    }

}