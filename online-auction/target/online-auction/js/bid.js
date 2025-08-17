function refreshAuction() {
    const id = document.getElementById('auctionId').value;
    fetch('AuctionServlet?auctionId=' + encodeURIComponent(id))
      .then(r => r.json())
      .then(a => {
        if (a.error) return;
        document.getElementById('title').innerText = a.title;
        document.getElementById('desc').innerText = a.description || '';
        document.getElementById('currentPrice').innerText = '₹' + a.current_price;
        document.getElementById('endTime').innerText = a.end_time;
      });
  }
  
  function placeBid() {
    const id = document.getElementById('auctionId').value;
    const amt = document.getElementById('bidAmount').value;
    fetch('BidServlet', {
      method: 'POST',
      headers: {'Content-Type':'application/x-www-form-urlencoded'},
      body: `auctionId=${encodeURIComponent(id)}&bidAmount=${encodeURIComponent(amt)}`
    })
    .then(r=>r.text())
    .then(msg=>{
      alert(msg);
      refreshAuction();
    });
  }
  
  refreshAuction();
  setInterval(refreshAuction, 2000); // live-ish polling
  