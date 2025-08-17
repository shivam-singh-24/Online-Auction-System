package com.auction.servlets;

import com.auction.util.DBUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/AuctionServlet")
public class AuctionServlet extends HttpServlet {

    // GET /AuctionServlet -> list all ongoing
    // GET /AuctionServlet?auctionId=123 -> one auction JSON (for polling)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String id = req.getParameter("auctionId");
        try (Connection con = DBUtil.getConnection(); PrintWriter out = resp.getWriter()) {
            if (id == null) {
                String sql = "SELECT * FROM auctions WHERE end_time > NOW() ORDER BY end_time ASC";
                try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                    JSONArray arr = new JSONArray();
                    while (rs.next()) {
                        JSONObject o = new JSONObject();
                        o.put("auction_id", rs.getInt("auction_id"));
                        o.put("title", rs.getString("title"));
                        o.put("current_price", rs.getBigDecimal("current_price"));
                        o.put("end_time", rs.getTimestamp("end_time").toString());
                        arr.put(o);
                    }
                    out.print(arr.toString());
                }
            } else {
                String sql = "SELECT * FROM auctions WHERE auction_id=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, Integer.parseInt(id));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            JSONObject o = new JSONObject();
                            o.put("auction_id", rs.getInt("auction_id"));
                            o.put("title", rs.getString("title"));
                            o.put("description", rs.getString("description"));
                            o.put("current_price", rs.getBigDecimal("current_price"));
                            o.put("end_time", rs.getTimestamp("end_time").toString());
                            out.print(o.toString());
                        } else {
                            resp.setStatus(404);
                            out.print("{\"error\":\"Not found\"}");
                        }
                    }
                }
            }
        } catch (Exception e) {
            resp.setStatus(500);
            e.printStackTrace();
        }
    }

    // Admin creates an auction
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s == null || !"admin".equals(s.getAttribute("role"))) {
            resp.setStatus(403);
            resp.getWriter().println("Forbidden");
            return;
        }
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        String startingPriceStr = req.getParameter("starting_price");
        String endTimeStr = req.getParameter("end_time"); // "yyyy-MM-dd HH:mm"

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO auctions (title, description, starting_price, current_price, end_time, created_by) VALUES (?,?,?,?,?,?)")) {
                int createdBy = (Integer) s.getAttribute("userId");
                ps.setString(1, title);
                ps.setString(2, description);
                ps.setBigDecimal(3, new java.math.BigDecimal(startingPriceStr));
                ps.setBigDecimal(4, new java.math.BigDecimal(startingPriceStr));
                LocalDateTime dt = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                ps.setTimestamp(5, Timestamp.valueOf(dt));
                ps.setInt(6, createdBy);
                ps.executeUpdate();
                con.commit();
                resp.sendRedirect("auctions.jsp");
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().println("Failed to create auction.");
        }
    }
}
