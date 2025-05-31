package com.kelompok5.open_notepad;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kelompok5.open_notepad.DAO.AdminDAO;
import com.kelompok5.open_notepad.DAO.FileDAO;
import com.kelompok5.open_notepad.DAO.SessionDAO;
import com.kelompok5.open_notepad.DAO.UserDAO;
import com.kelompok5.open_notepad.entity.Admin;
import com.kelompok5.open_notepad.entity.File;
import com.kelompok5.open_notepad.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final Security security;
    private final UserDAO userDAO;
    private final SessionDAO sessionDAO;
    private final FileDAO fileDAO;
    private final AdminDAO adminDAO;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public AccountController(Security security, @Qualifier("userDAO") UserDAO userDAO, SessionDAO sessionDAO,
            FileDAO fileDAO, @Qualifier("adminDAO") AdminDAO adminDAO) {
        this.adminDAO = adminDAO;
        this.fileDAO = fileDAO;
        this.sessionDAO = sessionDAO;
        this.userDAO = userDAO;
        this.security = security;

    }

    // Authentication API
    @PostMapping("/signin") // login
    public ResponseEntity<Map<String, String>> signIn(@RequestBody Map<String, String> requestData,
            HttpServletRequest request, HttpSession session) {
        String username = requestData.get("username");
        String password = requestData.get("password");
        if (security.isSessionValid(session, request)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already logged in"));
        }
        //check if there is another session
        // delete session from database
        if (sessionDAO.getFromDatabase(username) != null) {
            try {
                sessionDAO.deleteSession(username);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Error deleting session"));
            }
        }


        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username and password are required"));
        }
        // Login function
        String sql = "SELECT isAdmin FROM Accounts WHERE username = ?";
        boolean isAdmin;
        Object userOrAdmin = null;
        try {
            isAdmin = jdbcTemplate.queryForObject(sql, new Object[] { username }, (rs, rowNum) -> {
                return rs.getBoolean("isAdmin");
            });
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "account not found"));
        }

        // if statement
        if (isAdmin) {
            Admin admin = adminDAO.getFromDatabase(username);
            if (admin == null) {
                // If the admin is not found, return an error message
                return ResponseEntity.badRequest().body(Map.of("message", "account not found"));
            }
            // Check if the password matches
            if (!security.passwordIsValid(password, admin.getHashedPassword(), admin.getSalt())) {
                // If the password does not match, return an error message
                return ResponseEntity.badRequest().body(Map.of("message", "invalid password"));
            }
            userOrAdmin = admin;
        } else {
            User user = userDAO.getFromDatabase(username);
            if (user == null) {
                // If the user is not found, return an error message
                return ResponseEntity.badRequest().body(Map.of("message", "account not found"));
            }
            // Check if the password matches
            if (!security.passwordIsValid(password, user.getHashedPassword(), user.getSalt())) {
                // If the password does not match, return an error message
                return ResponseEntity.badRequest().body(Map.of("message", "invalid password"));
            }
            userOrAdmin = user;
        }
        // if login success, set the session attribute
        session.setMaxInactiveInterval(60 * 60 * 24);
        session.setAttribute("username", username);
        session.setAttribute("user", userOrAdmin); // set to user or admin object
        // upload session to database
        String sessionID = session.getId();
        session.setMaxInactiveInterval(60 * 60 * 24); // 1 day
        try {
            sessionDAO.uploadToDatabase(sessionID, username, request.getHeader("User-Agent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        // return success message
        return ResponseEntity.ok().body(Map.of("message", "User logged in successfully"));
    }

    @PostMapping("/signup") // register
    public ResponseEntity<Map<String, String>> signUp(@RequestBody Map<String, String> requestData,
        HttpServletRequest request, HttpSession session) {
        String username = requestData.get("username");
        String password = requestData.get("password");
        String email = requestData.get("email");
        String firstName = requestData.get("firstName");
        String lastName = requestData.get("lastName");
        if (security.isSessionValid(session, request)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User already logged in"));
        }
        if (username == null || password == null || email == null || firstName == null || lastName == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "All fields are required"));
        }
        // Hash password and salt it
        String salt = security.generateSalt();
        String hashedPassword = security.hashPassword(password, salt);

        // using UserDAO to register the user
        try {
            userDAO.register(username, hashedPassword, salt, email, firstName, lastName);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        // if register success, set the session attribute
        session.setMaxInactiveInterval(60 * 60 * 24);
        session.setAttribute("username", username);
        // upload session to database
        String sessionID = session.getId();
        try {
            sessionDAO.uploadToDatabase(sessionID, username, request.getHeader("User-Agent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        return ResponseEntity.ok().body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/logout") // logout
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpSession session) {
        if (!security.isSessionValid(session, request)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not logged in"));
        }

        String username = (String) session.getAttribute("username");
        // delete session from database
        try {
            sessionDAO.deleteSession(username);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        return ResponseEntity.ok().body(Map.of("message", "User logged out successfully"));
    }

    @PostMapping("/edit") // edit profile 
    public ResponseEntity<Map<String, String>> editProfile(
        @RequestParam(value = "file", required = false) MultipartFile file,
        @RequestParam("email") String newEmail,
        @RequestParam("firstName") String newFirstName,
        @RequestParam("lastName") String newLastName,
        @RequestParam("password") String newPassword,
        @RequestParam(value = "aboutMe", required = false) String newAboutMe,
        @RequestParam(value = "instagram", required = false) String newInstagram,
        @RequestParam(value = "linkedin", required = false) String newLinkedin,
        HttpServletRequest request, HttpSession session) {
        if (!security.isSessionValid(session, request)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not logged in"));
        }
        String username = (String) session.getAttribute("username");
        System.out.println("Profile edit request from user: " + username);

        // Check if the user exists in the database
        User user = userDAO.getFromDatabase(username);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

        String hashedPassword;
        String salt;

        // Get the new values from the request
        if (newEmail.equals("")) {
            newEmail = user.getEmail();
        }
        if (newFirstName.equals("")) {
            newFirstName = user.getFirstName();
        }
        if (newLastName.equals("")) {
            newLastName = user.getLastName();
        }
        if (newPassword.equals("")) {
            hashedPassword= user.getHashedPassword();
            salt = user.getSalt();
        }else{
            salt = security.generateSalt();
            hashedPassword = security.hashPassword(newPassword, salt);
        }
        System.out.println(" Username: " + username + 
            ", Email: " + newEmail + ", First Name: " + newFirstName + ", Last Name: " + newLastName);
        //Check if aboutMe, instagram, and linkedin are empty
        if (newAboutMe.equals("")) {
            newAboutMe = user.getAboutMe();
        }
        if (newInstagram.equals("")) {
            newInstagram = user.getInstagram();
        }
        if (newLinkedin.equals("")) {
            newLinkedin = user.getLinkedin();
        }

        // Check if the file is not empty
        if (file != null) {
            System.out.println("file is not empty \nfile name :" + file.getOriginalFilename());
            // Check if the file is an image
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {   
                return ResponseEntity.badRequest().body(Map.of("message", "File is not an image file"));
            }
            // Check if the file size is less than 10MB
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(Map.of("message", "File size is too large"));
            }

            // Delete the old profile picture if it exists
            // Get Profile Picture ID from database
            File oldProfilePicture = null;
            String sql = "SELECT * FROM Files WHERE fileID = (SELECT fileID FROM Accounts WHERE username = ?)";
            try {
                oldProfilePicture = jdbcTemplate.queryForObject(sql, new Object[] { username },
                        (rs, rowNum) -> new File(rs.getInt("fileID"), rs.getString("name"), rs.getString("type"),
                                rs.getLong("size"), rs.getString("path")));
            } catch (EmptyResultDataAccessException e) {
                // No profile picture found, continue
            }
            if (oldProfilePicture != null) {
                // set fikeID attribute from accounts to null
                String updateSQL = "UPDATE Accounts SET fileID = NULL WHERE username = ?";
                jdbcTemplate.update(updateSQL, username);
                // Delete the old profile picture file from the server
                Path oldFilePath = Paths.get(oldProfilePicture.getPath());
                try {
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Error deleting old profile picture"));
                }
                // Delete the old profile picture from the database
                try {
                    fileDAO.DeleteFromDatabase(oldProfilePicture.getFileID());
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Error deleting old profile picture from database"));
                }

            }


            // Create a new file name and upload directory
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path uploadDir = Paths.get("uploads/photos");

            // Create a file object and set the file properties
            File profilePicture = new File(-1, fileName, uploadDir.toString(), file.getSize(), contentType);
            profilePicture.setName(fileName);
            profilePicture.setPath("uploads/photos/" + fileName);
            profilePicture.setType(contentType);
            profilePicture.setSize(file.getSize());

            // Save the file to the server
            try {
                Files.createDirectories(uploadDir);
                file.transferTo(uploadDir.resolve(fileName));
            } catch (IOException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Error uploading file"));
            }

            // Save the file information to the database
            try {
                fileDAO.UploadToDatabase(profilePicture);
            } catch (Exception e) {
                System.out.println("Error uploading file to database: " + e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("message", "Error uploading photo to database"));
            }

            // Getting just FileID in database
            int ID = fileDAO.getFileID(fileName);
            profilePicture.setFileID(ID);

            //Attach the file to the user
            try {
                userDAO.uploadProfilePicture(username, ID);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Error uploading profile picture"));
            }
        }

        // Update the user in the database
        try {
            userDAO.updateInfo(username, newEmail, newFirstName, newLastName, hashedPassword, salt);
            userDAO.editDetails(username, newAboutMe, newInstagram, newLinkedin);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error updating user info"));
        }
        return ResponseEntity.ok().body(Map.of("message", "User profile updated successfully"));
    }

    // Delete account
    @PostMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteAccount(@RequestBody Map<String, String> requestData,
            HttpServletRequest request, HttpSession session) {
        if (!security.isSessionValid(session, request) || requestData.get("password") == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not logged in or password not provided"));
        }

        String password = requestData.get("password");
        String username = (String) session.getAttribute("username");

        // Ambil user dari database
        User user = userDAO.getFromDatabase(username);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }
        // Hash password and salt
        String hashedPassword = security.hashPassword(password, user.getSalt());

        
        //Delete user from database
        try {
            userDAO.deleteAccount(username, hashedPassword);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error deleting user: " + e.getMessage()));
        }
        // Delete session from database
        session.invalidate();

        return ResponseEntity.ok().body(Map.of("message", "User " + username + " account deleted successfully"));
    }

    @GetMapping("/info") // get user info
    public ResponseEntity<Map<String, String>> getUserInfo(HttpServletRequest request, HttpSession session) {
        if (!security.isSessionValid(session, request)) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not logged in"));
        }
        String username = (String) session.getAttribute("username");
        User user = userDAO.getFromDatabase(username);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }
        return ResponseEntity.ok().body(user.getInfo(username));
    }

    @GetMapping("/profile-photo") // Get profile photo
    public ResponseEntity<Resource> getProfilePhoto(HttpServletRequest request, HttpSession session)
            throws MalformedURLException, IOException {
        // Check if the user is logged in
        if (!security.isSessionValid(session, request)) {
            return ResponseEntity.badRequest().build();
        }
        // Get the username from the session and create a new User object
        String username = (String) session.getAttribute("username");

        // Get the user from the database
        File profilePhoto = null;
        String sql = "SELECT * FROM Files WHERE fileID = (SELECT fileID FROM Accounts WHERE username = ?)";
        try {
            profilePhoto = jdbcTemplate.queryForObject(sql, new Object[] { username }, (rs, rowNum) -> {
                File file = new File(rs.getInt("fileID"), rs.getString("name"), rs.getString("type"),
                        rs.getLong("size"), rs.getString("path"));
                return file;
            });

        } catch (EmptyResultDataAccessException e) {
            profilePhoto = new File(-1, "profile.png", "image/png", 0, "uploads/photos/profile.png");
        }

        // Check if the profile photo exists
        Path filePath;
        filePath = Paths.get("uploads/photos").resolve(profilePhoto.getName());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        Resource fileResource = new UrlResource(filePath.toUri());
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        System.out.println("File type: " + contentType);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(fileResource);
    }

    /*
     * @GetMapping("/download-file/{filename}")//Download file
     * public ResponseEntity<Resource> downloadPhoto(@PathVariable String filename)
     * throws IOException {
     * Path filePath = Paths.get("uploads/pdf").resolve(filename);
     * if (!Files.exists(filePath)) {
     * return ResponseEntity.notFound().build();
     * }
     * 
     * Resource fileResource = new UrlResource(filePath.toUri());
     * return ResponseEntity.ok()
     * .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
     * fileResource.getFilename() + "\"")
     * .contentType(MediaType.APPLICATION_OCTET_STREAM)
     * .body(fileResource);
     * }
     */
}