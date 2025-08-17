package com.auction.servlets;

import com.auction.util.DBUtil;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (username, password_hash, email, role) VALUES (?,?,?, 'user')")) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, email);
            ps.executeUpdate();
            resp.sendRedirect("login.html");
        } catch (SQLIntegrityConstraintViolationException dup) {
            resp.getWriter().println("Username or email already exists.");
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("Registration failed.");
        }
    }
}
