
const chessboard = document.getElementById("chessboard");
const isPlayerWhite = chessboard.classList.contains("render_white");

const numbers = isPlayerWhite ? "87654321" : "12345678";
const letters = isPlayerWhite ? "ABCDEFGH" : "HGFEDCBA";

let isLightSquare = true;
let label;
let index = 0;

for (let row = 0; row < 8; row++){
  for (let col = 0; col < 8; col++){
    let square = document.createElement("div");
    square.setAttribute("id", letters[col] + numbers[row]);
    square.classList.add("square", isLightSquare ? "light" : "dark");
    
    if (col === 0) {
      label = document.createElement("div");
      label.classList.add("number_label");  
      label.innerHTML = "<strong>"+ numbers[row] + "</strong>";
      label.setAttribute("style","position:absolute; top:0; left:5px; font-size:2vmin;")
      square.appendChild(label); 
      square.style.position = "relative"; 
    }
    
    if (row === 7) {
      label = document.createElement("div");
      label.classList.add("letter_label");  
      label.innerHTML = "<strong>"+ letters[col] + "</strong>";
      label.setAttribute("style","position:absolute; bottom:0; right:5px; font-size:2vmin;")
      square.appendChild(label); 
      square.style.position = "relative"; 
    }   

	if (row === 0 && col === 0) {
  		square.style.borderTopLeftRadius = "5px"; 
	} else if (row === 0 && col === 7) {
  		square.style.borderTopRightRadius = "5px"; 
	} else if (row === 7 && col === 0) {
  		square.style.borderBottomLeftRadius = "5px"; 
	} else if (row === 7 && col === 7) {
  		square.style.borderBottomRightRadius = "5px"; 
	}

    chessboard.appendChild(square);
    index++;
    isLightSquare = !isLightSquare;
  }
  isLightSquare = !isLightSquare;
  
}

