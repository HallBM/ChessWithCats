document.addEventListener('DOMContentLoaded', function() {
    const username = document.getElementById('username');
    const msg = document.getElementById('msg');
    
    username.addEventListener('focusout', function(e) {
        if (username.value == null || username.value == '') {
            msg.style.display = 'none';
        } else {
            fetch('/username', {
                method: 'Post',
                body: JSON.stringify({userInput: username.value}),
                headers: {
                    'Content-Type': 'application/json; charset=utf-8'
                }
            })
            .then(response => response.json())
            .then(ifExists => {
                if (ifExists) {
                    msg.style.display = 'block';
                } else {
                    msg.style.display = 'none';
                }
            })
            .catch(error => console.error('Error:', error));
        }
    });
    
    username.addEventListener('focus', function(e) {
        setTimeout(function(){msg.style.display = 'none';},750);
    });
});