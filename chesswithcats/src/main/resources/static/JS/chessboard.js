let numbers = "87654321";
const letters = "ABCDEFGH";
let light = true;

for (let row = 0; row < 8; row++){
  for (let col = 0; col < 8; col++){
    let square = document.createElement("div");
    square.setAttribute("id", letters[col] + numbers[row]);
    square.classList.add("square");
    
    let label; 
    if (col === 0) {
      label = document.createElement("div");
      label.setAttribute("class", "number_label");  
      label.innerHTML = "<strong>"+ numbers[row] + "</strong>";
      label.setAttribute("style","position:absolute; top:0; left:5px; font-size:2vmin;")
      square.appendChild(label);
      square.setAttribute("style","position:relative") 
    }
    if (row === 7) {
      label = document.createElement("div");
      label.setAttribute("class", "letter_label");  
      label.innerHTML = "<strong>"+ letters[col] + "</strong>";
      label.setAttribute("style","position:absolute; bottom:0; right:5px; font-size:2vmin;")
      square.appendChild(label); 
      square.setAttribute("style","position:relative") 
    }   


    if (light){
      square.classList.add("light");
      light = !light;
    
    
    } else {
      square.classList.add("dark");
      light = !light;
    
    
    }
    document.getElementById("chessboard").appendChild(square);
  }
  light = !light;
}

