
function getGameId(){
    let queryString = window.location.search;
    let urlParams = new URLSearchParams(queryString);
    let gameId = urlParams.get("games");
    return gameId;
}

function getPieceImgPath(gameId){
    let piecePath = [];
    if (gameId === "game1" || gameId === "game0"){
        piecePath.push("../static/Images/RegChessPieces/");
        piecePath.push(".png")
    } else {
        piecePath.push("../static/Images/CatChessPieces/");
        piecePath.push(".gif")
    }
    return piecePath;
}

function getPieceMap(){
    let pieceMap = {};
    pieceMap["A8"] = "br";
    pieceMap["B8"] = "bn";
    pieceMap["C8"] = "bb";
    pieceMap["D8"] = "bq";
    pieceMap["E8"] = "bk";
    pieceMap["F8"] = "bb";
    pieceMap["G8"] = "bn";
    pieceMap["H8"] = "br";
    pieceMap["A7"] = "bp";
    pieceMap["B7"] = "bp";
    pieceMap["C7"] = "bp";
    pieceMap["D7"] = "bp";
    pieceMap["E7"] = "bp";
    pieceMap["F7"] = "bp";
    pieceMap["G7"] = "bp";
    pieceMap["H7"] = "bp";
    
    pieceMap["A1"] = "wr";
    pieceMap["B1"] = "wn";
    pieceMap["C1"] = "wb";
    pieceMap["D1"] = "wq";
    pieceMap["E1"] = "wk";
    pieceMap["F1"] = "wb";
    pieceMap["G1"] = "wn";
    pieceMap["H1"] = "wr";
    pieceMap["A2"] = "wp";
    pieceMap["B2"] = "wp";
    pieceMap["C2"] = "wp";
    pieceMap["D2"] = "wp";
    pieceMap["E2"] = "wp";
    pieceMap["F2"] = "wp";
    pieceMap["G2"] = "wp";
    pieceMap["H2"] = "wp";

    return pieceMap;
}

function addPiecesWithFeatures(pieceMap, piecePath){

    for (let location in pieceMap){
      let image = document.createElement("img");
      image.setAttribute("src", piecePath[0] + pieceMap[location] + piecePath[1]);
      image.setAttribute("height", "100%");
      image.setAttribute("name", location);
      image.setAttribute("onclick", "clickToMove");
      image.addEventListener('click', clickToMove);
      image.addEventListener('dragstart', dragStart);
      if (pieceMap[location][0] === "b"){
        image.setAttribute("class", "black piece");
        image.setAttribute("draggable", "false");
      } else {
        image.setAttribute("class", "white piece");
        image.setAttribute("draggable", "true");
      }
      document.getElementById(location).appendChild(image);  
    }

}

function addSquareListeners(squares){
    squares.forEach(sq => {
        sq.addEventListener('dragenter', dragEnter)
        sq.addEventListener('dragover', dragOver);
        sq.addEventListener('dragleave', dragLeave);
        sq.addEventListener('drop', drop);
    });
}

function dragStart(event) {
    if (event.target.getAttribute("draggable") == "false"){
        return false;
    } 
    
    event.dataTransfer.setData('text/plain', event.target.name);
    setTimeout(() => {
        event.target.classList.add('hide');
    }, 0);

    document.getElementById(prevSq).removeAttribute("style","background-color: rgb(239, 208, 11, 0.5);");
    document.getElementById(event.target.name).setAttribute("style","background-color: rgb(239, 208, 11, 0.5);");
    prevSq = event.target.name;
}

function dragEnter(event) {
    event.preventDefault();
    event.target.classList.add('drag-over');
}

function dragOver(event) {
    event.preventDefault();
    event.target.classList.add('drag-over');
}

function dragLeave(event) {
    event.target.classList.remove('drag-over');
}

function drop(event) {

    event.target.classList.remove('drag-over');

    let prev_id = event.dataTransfer.getData('text/plain');
    
    const movedPiece = document.getElementsByName(prev_id)[0];
    
    if (event.target.id == prev_id || movedPiece.name == event.target.name){
        return false;
    }
    
    // if moved onto an occupied square (square contains a piece)
    if (event.target.children.length > 0 && 
      ((event.target.children[0].classList.contains("white") && movedPiece.classList.contains("white")) ||
       (event.target.children[0].classList.contains("black") && movedPiece.classList.contains("black")) ||
       (event.target.children[0].classList.contains("black") && movedPiece.classList.contains("white")) || //TODO remove when handling 'capture' logic
       (event.target.children[0].classList.contains("white") && movedPiece.classList.contains("black"))    //TODO remove when handling 'capture' logic
       )) {
        return false;    
    }
    
    // if moved directly onto another piece
    if ((event.target.classList.contains("white") && movedPiece.classList.contains("white")) ||
        (event.target.classList.contains("black") && movedPiece.classList.contains("black")) ||
        (event.target.classList.contains("black") && movedPiece.classList.contains("white")) || //TODO remove when handling capture logic
        (event.target.classList.contains("white") && movedPiece.classList.contains("black"))    //TODO remove when handling capture logic 
        ) {
            return false;    
    }   
    
    event.target.appendChild(movedPiece);
 
    movedPiece.setAttribute("name", event.target.id);

    movedPiece.classList.remove('hide');

    document.getElementById(prevSq).removeAttribute("style","background-color: rgb(239, 208, 11, 0.5);");
    event.target.setAttribute("style","background-color: rgb(239, 208, 11, 0.5);");
    prevSq = event.target.id;

    whiteTurn = !whiteTurn;
    
    white.forEach(w => {
        w.setAttribute('draggable', whiteTurn);
    });
    
    black.forEach(b => {
        b.setAttribute('draggable', !whiteTurn);
    });

    if (whiteTurn){
        document.getElementById("turn").setAttribute("style","color : rgb(230, 230, 230);");
        document.getElementById("turn-color").setAttribute("style", "background-color : white; border-color: black;");
        document.getElementById("turn-text").innerHTML = "White to Move";

    } else {
        document.getElementById("turn").setAttribute("style","color : black;");
        document.getElementById("turn-color").setAttribute("style", "background-color : black; border-color: white;");
        document.getElementById("turn-text").innerHTML = "Black to Move";
    }
}

function clickToMove(event){
    if (event.target.getAttribute("draggable") == "false"){
        return false;
    } 
    
    document.getElementById(prevSq).removeAttribute("style","background-color: rgb(239, 208, 11, 0.5);");
    document.getElementById(event.target.name).setAttribute("style","background-color: rgb(239, 208, 11, 0.5);");
    prevSq = event.target.name;
}

function addObstructiveCats(){

    let numbers = "6543";
    const noOfImages = 51; // update for more images 
    let numOfCats = 4;
    let catsAndLoc = new Set();
    let gifPath = "../static/Gifs/"

    while (numOfCats != 0){
        let loc, randNum, randLet, randImg;

        do{
            randNum = numbers[Math.floor(Math.random()*4)]
            randLet = letters[Math.floor(Math.random()*8)]
            loc = randLet + randNum;
        } while (catsAndLoc.has(randNum)||catsAndLoc.has(randLet))

        catsAndLoc.add(randNum);
        catsAndLoc.add(randLet);

        do{
            randImg = Math.floor(Math.random()*noOfImages) +1;
            randImg = gifPath + "i" + randImg + ".gif";
        } while (catsAndLoc.has(randImg))

        catsAndLoc.add(randImg);

        let image = document.createElement("img");
        image.setAttribute("src", randImg);

        if (image.clientWidth > image.clientHeight){
            image.setAttribute("height", "100%");
        } else {
            image.setAttribute("width", "100%");
        }

        image.setAttribute("draggable", "false");
        document.getElementById(loc).removeEventListener('dragenter', dragEnter)
        document.getElementById(loc).removeEventListener('dragover', dragOver);
        document.getElementById(loc).removeEventListener('dragleave', dragLeave);
        document.getElementById(loc).removeEventListener('drop', drop);
        document.getElementById(loc).appendChild(image);

        numOfCats--;
    }
}

function finalizeBoard(){

    if (gameId === "game0"){

    } else if (gameId === "game1"){
        addObstructiveCats();;
    } else if (gameId === "game2"){
    
    } else {
    
    }
}

const white = document.querySelectorAll('.white');
const black = document.querySelectorAll('.black');
const pieces = document.querySelectorAll('.piece');
const squares = document.querySelectorAll('.square');
let gameId, prevSq, whiteTurn;

function getNewGame(){
    gameId = getGameId();
    getPieceImgPath(gameId);
    addPiecesWithFeatures(getPieceMap(), getPieceImgPath(gameId)); 

    prevSq = "A1";
    whiteTurn = true;

    addSquareListeners(squares);
    finalizeBoard();
}

getNewGame();