<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Live Auctions</title>
  <link rel="stylesheet" href="css/style.css">
  <style>
    .card { border: 1px solid #ccc; padding: 15px; margin: 10px; border-radius: 5px; }
  </style>
</head>
<body>
<h2>Live Auctions</h2>
<div id="list">Loading auctions...</div>

<script>
function loadAuctions() {
    fetch('AuctionServlet')
      .then(response => response.json())
      .then(arr => {
          const container = document.getElementById('list');
          
          // Check if the response is a valid array
          if(!Array.isArray(arr) || arr.length === 0) {
              container.innerHTML = '<p>No auctions available.</p>';
              return;
          }

          // Generate HTML for each auction
          container.innerHTML = arr.map(a => {
              const endTime = new Date(a.end_time).toLocaleString();
              const auctionLink = 'bid.jsp?auctionId=' + encodeURIComponent(a.auction_id);



              return `
                <div class="card">
                  <h3>${a.title}</h3>
                  <p>Current Price: ₹${a.current_price}</p>
                  <p>Ends: ${endTime}</p>
                  <a href="${auctionLink}">Open</a>
                </div>
              `;
          }).join('');
      })
      .catch(err => {
          console.error('Error fetching auctions:', err);
          document.getElementById('list').innerHTML = '<p>Error loading auctions.</p>';
      });
}

// Initial load
loadAuctions();

// Refresh every 5 seconds
setInterval(loadAuctions, 5000);
</script>

<hr>
<a href="admin.jsp">Admin: Create Auction</a>
</body>
</html>
