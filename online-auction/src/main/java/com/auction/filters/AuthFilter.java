package com.auction.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;

@WebFilter({ "/auctions.jsp", "/bid.jsp", "/admin.jsp", "/AuctionServlet", "/BidServlet" })
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("userId") == null) {
            ((HttpServletResponse) response).sendRedirect("login.html");
            return;
        }
        chain.doFilter(request, response);
    }
}
