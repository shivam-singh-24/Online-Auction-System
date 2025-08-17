package com.auction.servlets;

import com.auction.util.DBUtil;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con
                        .prepareStatement("SELECT user_id,password_hash,role FROM users WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && BCrypt.checkpw(password, rs.getString("password_hash"))) {
                    HttpSession session = req.getSession(true);
                    session.setAttribute("userId", rs.getInt("user_id"));
                    session.setAttribute("role", rs.getString("role"));
                    resp.sendRedirect("auctions.jsp");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        resp.getWriter().println("Invalid credentials");
    }
}
