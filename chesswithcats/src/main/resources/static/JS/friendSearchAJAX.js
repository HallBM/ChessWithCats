document.addEventListener("DOMContentLoaded", function() {
  document.getElementById("search").addEventListener("submit", function(event) {
    event.preventDefault();
    let searchInput = document.getElementById("searchInput").value;
    if (searchInput === null || searchInput === "") {
      document.getElementById("resultsTable").style.display = "none";
    } else {
      fetch("/friends/playerSearch?userInput=" + searchInput, {
        method: "GET",
        headers: { "Content-Type": "application/json; charset=utf-8" },
        cache: "no-cache",
      })
        .then(function(response) {
          if (response.ok) {
            return response.json();
          } else {
            throw new Error(response.statusText);
          }
        })
        .then(function(playerList) {
		  	
	
          let resultsTable = document.getElementById("playerResults");
          resultsTable.innerHTML = "";
          if (playerList === null || playerList.length === 0) {
            resultsTable.innerHTML +=
              '<tr><td>Player(s) not found</td></tr>';
          } else {
            playerList.forEach(function(player) {
              resultsTable.innerHTML +=
                `<tr>
                	<td>
                		<span><a style="color: white" href="/profile/` + player + `">` + player + `</a></span>
                	</td>
                	<td>
                		<form action="/friendrequest/send/` + player + `" method="POST">
                			<button type="submit" name="selectedName" value="` + player + `" style="font-size:1vw; color:green">Request 
                				<i class="fas fa-user-check" style="color:green"></i>
                			</button>
                		</form>
                	</td>
                	<td>
                		<form action="/block/` + player + `" method="POST">
                			<button type="submit" name="selectedName" value="` + player + `" style="font-size:1vw; color:red">Block User 
                				<i class="fas fa-user-slash" style="color:red"></i>
                			</button>
                		</form>
                	</td>
                </tr>`;
            });
            document.getElementById("resultsTable").style.display = "block";
          }
        })
        .catch(function(error) {
          console.error(error);
        });
    }
  });
});