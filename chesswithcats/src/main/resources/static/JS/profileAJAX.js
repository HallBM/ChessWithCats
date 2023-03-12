document.addEventListener("DOMContentLoaded", function() {
	document.getElementById("delete-acct").addEventListener("click", function(event) {
		event.preventDefault();
		const masked = document.getElementsByClassName("mask");
		for (let mask of masked) {
			mask.style.display = "none";
		}
		document.getElementById("delete").style.display = "inline";
		document.getElementById("deleteDB").style.display = "inline";
		document.getElementById("cancel").style.display = "inline";
	});


	document.getElementById("cancel").addEventListener("click", function(event) {
		event.preventDefault();
		document.getElementById("delete").style.display = "none";
		document.getElementById("deleteDB").style.display = "none";
		document.getElementById("cancel").style.display = "none";
	});

	document.getElementById("deleteDB").addEventListener("click", function(event) {
		event.preventDefault();

		fetch("/delete-account", {
			method: "POST",
			headers: { "Content-Type": "application/json; charset=utf-8" },
			cache: "no-cache",
		})
			.then(response => {
				if (response.redirected) {
					window.location.href = response.url;
				}
			})
			.catch(function(error) {
				console.error(error);
			});
	});
});



