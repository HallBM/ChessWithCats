document.addEventListener("DOMContentLoaded", function() {
	
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
	const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
	
	document.getElementById("search").addEventListener("submit", function(event) {
		event.preventDefault();
		let searchInput = document.getElementById("searchInput").value;
		if (searchInput === null || searchInput === "") {
			document.getElementById("resultsTable").style.display = "none";
		} else {
			fetch("/friends/playerSearch?userInput=" + searchInput, {
				method: "GET",
				headers: {
					"Content-Type": "application/json; charset=utf-8",
					[header]: token
				},
				cache: "no-cache"
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
 							<input type="hidden" name="_csrf" value="` + token + `" />
                			<button type="submit" name="selectedName" value="` + player + `" class="btn btn-success">Request 
                			</button>
                		</form>
                	</td>
                	<td>
                		<form action="/block/` + player + `" method="POST">
		<input type="hidden" name="_csrf" value="` + token + `" />
                			<button type="submit" name="selectedName" value="` + player + `" class="btn btn-danger">Block User 
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