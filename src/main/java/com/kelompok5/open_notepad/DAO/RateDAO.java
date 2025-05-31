package com.kelompok5.open_notepad.DAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.kelompok5.open_notepad.entity.Rate;

@Component
public class RateDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NoteDAO noteDAO;

    public float getFromUser(String userID) {
        String sql = "SELECT AVG(rating) AS rate FROM Ratings WHERE username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[] { userID }, (rs, rowNum) -> rs.getFloat("rate"));

        } catch (Exception e) {
            System.out.println("get rate from user : " + e.getMessage());
            return -1;
        }
    }

    public float getFromNote(int moduleID) {
        String sql = "SELECT AVG(rating) AS rate FROM Ratings WHERE moduleID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[] { moduleID }, (rs, rowNum) -> rs.getFloat("rate"));

        } catch (Exception e) {
            System.out.println("get rate from module : " + e.getMessage());
            return -1;
        }

    }

    public void uploadToDatabase(Rate rate) {
        String sqlCheck = "SELECT COUNT(*) FROM Ratings WHERE username = ? AND moduleID = ?";
        int count = jdbcTemplate.queryForObject(sqlCheck, new Object[] { rate.getUserID(), rate.getModuleID() },
                Integer.class);

        if (count > 0) {
            String sqlUpdate = "UPDATE Ratings SET rating = ?, dateRated = ? WHERE username = ? AND moduleID = ?";
            jdbcTemplate.update(sqlUpdate, rate.getRating(), rate.getDateRated(), rate.getUserID(), rate.getModuleID());
        } else {
            String sqlInsert = "INSERT INTO Ratings (username, moduleID, rating, dateRated) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sqlInsert, rate.getUserID(), rate.getModuleID(), rate.getRating(), rate.getDateRated());
        }
        noteDAO.clearCache(); // Hapus cache setiap kali rating berubah
    }

    public Float getAverageRatingByModuleID(String moduleID) {
        String sql = "SELECT AVG(rating) AS avg_rating FROM Ratings WHERE moduleID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[] { moduleID },
                    (rs, rowNum) -> rs.getFloat("avg_rating"));
        } catch (Exception e) {
            System.out.println("Error getting average rating: " + e.getMessage());
            return null;
        }
    }

    public void deleteFromUser(String userID) {
        // Implement the logic to delete the rate from the database
        // This could involve using JDBC or an ORM framework like Hibernate
        // Example: DatabaseConnection.deleteRate(this);
    }

    public void deleteFromModule(int noteID) {
        // Implement the logic to delete the rate from the database
        // This could involve using JDBC or an ORM framework like Hibernate
        // Example: DatabaseConnection.deleteRate(this);
    }

}
