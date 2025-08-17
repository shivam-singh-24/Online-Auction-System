<%@ page import="javax.servlet.http.*,javax.servlet.*" %>
<%
  HttpSession s = request.getSession(false);
  if (s == null || !"admin".equals(s.getAttribute("role"))) {
    response.sendRedirect("login.html"); return;
  }
%>
<!doctype html>
<html>
<head><meta charset="UTF-8"><title>Create Auction</title><link rel="stylesheet" href="css/style.css"></head>
<body>
<h2>Create Auction</h2>
<form method="post" action="AuctionServlet">
  <label>Title <input name="title" required></label><br>
  <label>Description <textarea name="description"></textarea></label><br>
  <label>Starting Price <input name="starting_price" type="number" step="0.01" required></label><br>
  <label>End Time (yyyy-MM-dd HH:mm) <input name="end_time" placeholder="2025-08-20 18:30" required></label><br>
  <button type="submit">Create</button>
</form>
</body>
</html>
