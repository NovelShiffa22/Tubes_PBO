package com.kelompok5.open_notepad;

import com.kelompok5.open_notepad.DAO.BookmarkDAO;
import com.kelompok5.open_notepad.DAO.NoteDAO;
import com.kelompok5.open_notepad.entity.Note;
import com.kelompok5.open_notepad.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {
    @Autowired
    private BookmarkDAO bookmarkDAO;

    private final Security security;
    private final NoteDAO noteDAO;

    public PageController(Security security, NoteDAO noteDAO) {
        this.security = security;
        this.noteDAO = noteDAO;
    }

    @GetMapping("/")
    public String home(HttpSession session, HttpServletRequest request) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(HttpSession session, HttpServletRequest request) {
        if (security.isSessionValid(session, request)) {
            return "redirect:/";
        }
        return "authPage";
    }

    @GetMapping("/user/profile")
    public String profile(HttpSession session, HttpServletRequest request) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }
        return "profile";
    }

    @GetMapping("/user/notes/upload")
    public String uploadNotes(HttpSession session, HttpServletRequest request) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }
        return "upload";
    }

    @GetMapping("/user/notes")
    public String myNotes(HttpSession session, HttpServletRequest request) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }
        return "myNote";
    }

    @GetMapping("/user/notes/bookmark")
    public String savedNotes(HttpSession session, HttpServletRequest request, Model model) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> notes = bookmarkDAO.getBookmarkedNotes(user.getUsername());
        model.addAttribute("savedNotes", notes);
        return "savedNotes";
    }

    @GetMapping("/user/profile/edit")
    public String editProfile(HttpSession session, HttpServletRequest request) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }
        return "editProfile";
    }

    @GetMapping("/note/view/{id}")
    public String notesPage(HttpSession session, HttpServletRequest request, @PathVariable("id") int id, Model model) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }

        Note note = noteDAO.getFromDatabase(id);
        if (note == null) {
            return "redirect:/";
        }

        model.addAttribute("noteID", id);
        model.addAttribute("note", note);
        return "note";
    }

    @GetMapping("/admin")
    public String adminPage(HttpSession session, HttpServletRequest request) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }
        if (!security.isAdmin(session)) {
            return "redirect:/";
        }
        return "admin";
    }

    @GetMapping("/admin/profile")
    public String adminProfile(HttpSession session, HttpServletRequest request) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }
        if (!security.isAdmin(session)) {
            return "redirect:/";
        }
        return "adminProfile";
    }

    @GetMapping("/admin/profile/edit")
    public String adminEditProfile(HttpSession session, HttpServletRequest request) {
        if (!security.isSessionValid(session, request)) {
            return "redirect:/login";
        }
        if (!security.isAdmin(session)) {
            return "redirect:/";
        }
        return "editAdminProfile";
    }
}
