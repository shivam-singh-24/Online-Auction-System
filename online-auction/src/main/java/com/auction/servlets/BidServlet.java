package com.auction.servlets;

import com.auction.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.*;

@WebServlet("/BidServlet")
public class BidServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        HttpSession s = req.getSession(false);
        PrintWriter out = resp.getWriter();
        if (s == null || s.getAttribute("userId") == null) {
            resp.setStatus(401);
            out.println("Login required.");
            return;
        }

        int userId = (Integer) s.getAttribute("userId");
        int auctionId = Integer.parseInt(req.getParameter("auctionId"));
        BigDecimal bidAmount = new BigDecimal(req.getParameter("bidAmount"));

        try (Connection con = DBUtil.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Lock row to avoid race
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT current_price, end_time FROM auctions WHERE auction_id=? FOR UPDATE")) {
                    ps.setInt(1, auctionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            out.println("Auction not found.");
                            con.rollback();
                            return;
                        }
                        BigDecimal current = rs.getBigDecimal("current_price");
                        Timestamp end = rs.getTimestamp("end_time");
                        if (end.before(new Timestamp(System.currentTimeMillis()))) {
                            out.println("Auction ended.");
                            con.rollback();
                            return;
                        }
                        if (bidAmount.compareTo(current) <= 0) {
                            out.println("Bid must be higher than current price.");
                            con.rollback();
                            return;
                        }
                    }
                }

                // Insert bid
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO bids (auction_id, user_id, bid_amount) VALUES (?,?,?)")) {
                    ps.setInt(1, auctionId);
                    ps.setInt(2, userId);
                    ps.setBigDecimal(3, bidAmount);
                    ps.executeUpdate();
                }

                // Update current price
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE auctions SET current_price=? WHERE auction_id=?")) {
                    ps.setBigDecimal(1, bidAmount);
                    ps.setInt(2, auctionId);
                    ps.executeUpdate();
                }

                con.commit();
                out.println("Bid placed successfully!");
            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                out.println("Failed to place bid.");
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Server error.");
        }
    }
}
