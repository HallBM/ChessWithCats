document.addEventListener("DOMContentLoaded", function() {
    const username = document.getElementById("username");
    const msg = document.getElementById("msg");
    const username_msg = document.getElementById("username-msg");
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
	const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
    
    username.addEventListener("focusout", function() {
        if (username.value == null || username.value == "") {
            msg.style.display = "none";
        } else {
            fetch("/username", {
                method: "Post",
                body: JSON.stringify({userInput: username.value}),
                headers: {
                    "Content-Type": "application/json; charset=utf-8",
                    [header]: token
                },
                cache: "no-cache",
            })
            .then(response => response.json())
            .then(ifExists => {
                if (ifExists) {
                    msg.style.display = "block";
                } else {
                    msg.style.display = "none";
                }
            })
            .catch(error => console.error("Error:", error));
        }
    });
    
    username.addEventListener("focus", function() {
        setTimeout(function(){
			msg.style.display = "none";
			if (username_msg != null) {
				username_msg.remove();
			}
		},750);
    });

});