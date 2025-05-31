package com.kelompok5.open_notepad;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.kelompok5.open_notepad.DAO.BookmarkDAO;
import com.kelompok5.open_notepad.DAO.NoteDAO;
import com.kelompok5.open_notepad.DAO.RateDAO;
import com.kelompok5.open_notepad.entity.Bookmark;
import com.kelompok5.open_notepad.entity.Rate;

import org.springframework.beans.factory.annotation.Autowired;

import com.kelompok5.open_notepad.entity.User;

@RestController
@RequestMapping("/interface") // ⇒ endpoint: /interface/...
@Validated
public class InterfaceController {

    @Autowired
    private BookmarkDAO bookmarkDAO;

    @Autowired
    private RateDAO rateDAO;

    @Autowired
    private NoteDAO noteDAO;

    /*
     * -------------------------------------------------
     * Lifecycle hook ― memastikan Spring mem-bootstrap
     * -------------------------------------------------
     */
    @PostConstruct
    public void init() {
        System.out.println("✅ InterfaceController loaded");
    }

    /*
     * ---------------------------
     * DTO untuk /interface/rate
     * ---------------------------
     */
    public static class RateRequest {
        @NotBlank(message = "userID tidak boleh kosong")
        private String userID;

        @NotBlank(message = "moduleID tidak boleh kosong")
        private String moduleID;

        @Min(value = 1, message = "Rating minimal 1")
        @Max(value = 5, message = "Rating maksimal 5")
        private int rating;

        /*-- getter & setter --*/
        public String getUserID() {
            return userID;
        }

        public void setUserID(String v) {
            this.userID = v;
        }

        public String getModuleID() {
            return moduleID;
        }

        public void setModuleID(String v) {
            this.moduleID = v;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int v) {
            this.rating = v;
        }
    }

    /*
     * -------------------------
     * GET /interface/downloadnote
     * -------------------------
     */
    @GetMapping("/downloadnote")
    public ResponseEntity<ApiResponse> downloadNote(@RequestParam String userID,
            @RequestParam String moduleID) {

        // TODO: simpan log download ke DB jika diperlukan
        String msg = "Catatan " + moduleID + " berhasil di-download oleh " + userID;
        return ResponseEntity.ok(new ApiResponse("success", msg));
    }

    /*
     * -------------------------
     * POST /interface/rate
     * Body JSON → RateRequest
     * -------------------------
     */
    @PostMapping("/rate")
    public ResponseEntity<ApiResponse> rateNote(@RequestBody Rate rate, HttpSession session) {
        Object userObj = session.getAttribute("user");
        if (!(userObj instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("error", "User not logged in"));
        }

        rate.setUserID(user.getUsername());

        try {
            // Upsert rating dan hapus cache di NoteDAO
            rateDAO.uploadToDatabase(rate);

            Float avgRating = rateDAO.getAverageRatingByModuleID(rate.getModuleID());
            if (avgRating == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse("error", "Gagal menghitung rata-rata rating"));
            }

            String msg = "Rating " + rate.getRating() +
                    " disimpan untuk " + rate.getModuleID() +
                    " oleh " + rate.getUserID();
            return ResponseEntity.ok(new ApiResponse("success", msg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("error", "Gagal menyimpan rating"));
        }
    }

    @GetMapping("/rate/average")
    public ResponseEntity<?> getAverageRating(@RequestParam String moduleID) {
        Float avg = rateDAO.getAverageRatingByModuleID(moduleID);
        if (avg == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get average rating"));
        }
        return ResponseEntity.ok(Map.of("rate", avg));
    }

    /*
     * -------------------------
     * POST /interface/bookmark
     * Param: ?moduleID=...
     * Sesi harus berisi attribute "user"
     * -------------------------
     */
    @PostMapping("/bookmark")
    public ResponseEntity<ApiResponse> bookmarkNote(HttpSession session,
            @RequestParam String moduleID) {

        Object userObj = session.getAttribute("user");
        if (!(userObj instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("error", "User not logged in"));
        }
        User user = (User) userObj;

        // ✅ Simpan ke database
        Bookmark bookmark = new Bookmark(user.getUsername(), moduleID);
        bookmarkDAO.uploadToDatabase(bookmark);

        String msg = "Catatan " + moduleID + " dibookmark oleh " + user.getUsername();
        return ResponseEntity.ok(new ApiResponse("success", msg));
    }

    @GetMapping("/bookmark/status")
    public ResponseEntity<Map<String, Object>> checkBookmarkStatus(HttpSession session,
            @RequestParam String moduleID) {
        Object userObj = session.getAttribute("user");
        if (!(userObj instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "User not logged in"));
        }

        boolean exists = bookmarkDAO.bookmarkExists(user.getUsername(), moduleID);
        return ResponseEntity.ok(Map.of("status", "success", "bookmarked", exists));
    }

    @DeleteMapping("/bookmark")
    public ResponseEntity<ApiResponse> unbookmarkNote(HttpSession session,
            @RequestParam String moduleID) {
        Object userObj = session.getAttribute("user");
        if (!(userObj instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("error", "User not logged in"));
        }
        User user = (User) userObj;

        bookmarkDAO.deleteBookmark(user.getUsername(), moduleID);

        String msg = "Bookmark untuk catatan " + moduleID + " oleh " + user.getUsername() + " telah dihapus";
        return ResponseEntity.ok(new ApiResponse("success", msg));
    }

    /*
     * -------------------------
     * Wrapper respons standar
     * -------------------------
     */
    public static class ApiResponse {
        private final String status;
        private final String message;

        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
