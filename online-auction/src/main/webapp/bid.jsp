<%@ page import="java.util.*" %>
<%
  String id = request.getParameter("auctionId");
  if (id == null) { id = "1"; }
%>
<!doctype html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Bid</title>
  <link rel="stylesheet" href="css/style.css">
  <style>
    #auctionBox { border: 1px solid #ccc; padding: 15px; margin: 10px; border-radius: 5px; }
  </style>
</head>
<body>
<h2>Auction</h2>
<input type="hidden" id="auctionId" value="<%=id%>">
<div id="auctionBox">
  <h3 id="title">Loading...</h3>
  <p id="desc"></p>
  <p>Current: <span id="currentPrice">₹--</span></p>
  <p>Ends: <span id="endTime">--</span></p>
</div>

<h3>Place Bid</h3>
<input id="bidAmount" type="number" step="0.01" min="0">
<button onclick="placeBid()">Bid</button>

<script>
const auctionId = document.getElementById('auctionId').value;

function loadAuction() {
    fetch('AuctionServlet?auctionId=' + encodeURIComponent(auctionId))
    .then(res => res.json())
    .then(a => {
        if (a.error) {
            document.getElementById('auctionBox').innerHTML = '<p>' + a.error + '</p>';
            return;
        }
        document.getElementById('title').textContent = a.title;
        document.getElementById('desc').textContent = a.description;
        document.getElementById('currentPrice').textContent = '₹' + a.current_price;
        document.getElementById('endTime').textContent = new Date(a.end_time).toLocaleString();
    })
    .catch(err => console.error(err));
}

// Place bid function
function placeBid() {
    const bidAmount = document.getElementById('bidAmount').value;
    if (!bidAmount || bidAmount <= 0) {
        alert('Enter a valid bid amount.');
        return;
    }

    fetch('BidServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'auction_id=' + encodeURIComponent(auctionId) + '&bid_amount=' + encodeURIComponent(bidAmount)
    })
    .then(res => res.text())
    .then(msg => {
        alert(msg);
        loadAuction(); // refresh current price
    })
    .catch(err => console.error(err));
}

// Initial load & refresh every 5 seconds
loadAuction();
setInterval(loadAuction, 5000);
</script>
</body>
</html>
