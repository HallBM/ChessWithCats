const [_, games, gameStyle, gameNumber, playerColor] = window.location.pathname.split("/");
let pieceMap = JSON.parse(document.getElementById("pieceMapJson").innerHTML);
const chessboard = document.getElementById("chessboard");
const isPlayerWhite = chessboard.classList.contains("render_white");

let blackSquares = [];
let whiteSquares = [];
let allSquares = [];
let catSquares = [];
let allPieces = [];

let isWhiteTurn = true;
let prevSq = "A1";

const numbers = isPlayerWhite ? "87654321" : "12345678";
const letters = isPlayerWhite ? "ABCDEFGH" : "HGFEDCBA";

function buildBoard() {
	let isLightSquare = true;
	let label;

	for (let row = 0; row < 8; row++) {
		for (let col = 0; col < 8; col++) {
			let square = document.createElement("div");
			square.setAttribute("id", letters[col] + numbers[row]);
			square.classList.add("square", isLightSquare ? "light" : "dark");

			if (col === 0) {
				label = document.createElement("div");
				label.classList.add("number_label");
				label.innerHTML = "<strong>" + numbers[row] + "</strong>";
				label.setAttribute("style", "position:absolute; top:0; left:5px; font-size:2vmin;")
				square.appendChild(label);
				square.style.position = "relative";
			}

			if (row === 7) {
				label = document.createElement("div");
				label.classList.add("letter_label");
				label.innerHTML = "<strong>" + letters[col] + "</strong>";
				label.setAttribute("style", "position:absolute; bottom:0; right:5px; font-size:2vmin;")
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
			isLightSquare = !isLightSquare;
		}
		isLightSquare = !isLightSquare;
	}
}

function getPieceImgPath() {
	let piecePath = [];
	if (gameStyle === "AMBIGUOUS") {
		piecePath.push("/Images/CatChessPieces/");
		piecePath.push(".gif");
	} else {
		piecePath.push("/Images/RegChessPieces/");
		piecePath.push(".png");
	}
	return piecePath;
}

function convertPieceMap() {

	for (let loc in pieceMap) {
		if (pieceMap[loc] === "C") {
			catSquares.push(loc);
		} else {
			pieceMap[loc] = (pieceMap[loc] == pieceMap[loc].toLowerCase()) ?
				("b" + pieceMap[loc]) :
				("w" + pieceMap[loc].toLowerCase());
		}
	}
}

function addGamePiecesWithDraggableFeatures(piecePath) {

	for (let loc in pieceMap) {

		if (pieceMap[loc] === "C") {
			continue;
		}

		let image = document.createElement("img");
		image.setAttribute("src", piecePath[0] + pieceMap[loc] + piecePath[1]);
		image.setAttribute("height", "100%");
		image.setAttribute("name", loc);
		image.setAttribute("onclick", "clickToMove");
		image.addEventListener('click', clickToMove);
		image.addEventListener('dragstart', dragStart);
		if (pieceMap[loc][0] === "b") {
			image.setAttribute("class", "black piece");
			image.setAttribute("draggable", "false");
		} else {
			image.setAttribute("class", "white piece");
			//TODO makes impossible to move; need to disable while testing move transmission from FE to BE
			//if (playerColor == 'white'){
			image.setAttribute("draggable", "true");
			//} else {
			//	image.setAttribute("draggable", "false");	
			//}
		}
		document.getElementById(loc).appendChild(image);
	}
}

function addSquareListeners() {
	allSquares.forEach(sq => {
		sq.addEventListener('dragenter', dragEnter)
		sq.addEventListener('dragover', dragOver);
		sq.addEventListener('dragleave', dragLeave);
		sq.addEventListener('drop', drop);
	});
}

function dragStart(event) {
	if (event.target.getAttribute("draggable") == "false") {
		return false;
	}

	event.dataTransfer.setData('text/plain', event.target.name);
	setTimeout(() => {
		event.target.classList.add('hide');
	}, 0);

	document.getElementById(prevSq).removeAttribute("style", "background-color: rgb(239, 208, 11, 0.5);");
	document.getElementById(event.target.name).setAttribute("style", "background-color: rgb(239, 208, 11, 0.5);");

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

	if (event.target.id == prev_id || movedPiece.name == event.target.name) {
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

	document.getElementById(prevSq).removeAttribute("style", "background-color: rgb(239, 208, 11, 0.5);");
	event.target.setAttribute("style", "background-color: rgb(239, 208, 11, 0.5);");
	prevSq = event.target.id;

	isWhiteTurn = !isWhiteTurn;
	//if(playerColor == 'white'){//TODO makes impossible to move; need to disable while testing move transmission from FE to BE
	whiteSquares.forEach(w => {
		w.setAttribute('draggable', isWhiteTurn);
	});
	//} else {
	blackSquares.forEach(b => {
		b.setAttribute('draggable', !isWhiteTurn);
	});
	//}

	if (isWhiteTurn) {
		document.getElementById("turn").setAttribute("style", "color : rgb(230, 230, 230);");
		document.getElementById("turn-color").setAttribute("style", "background-color : white; border-color: black;");
		document.getElementById("turn-text").innerHTML = "White to Move";

	} else {
		document.getElementById("turn").setAttribute("style", "color : black;");
		document.getElementById("turn-color").setAttribute("style", "background-color : black; border-color: white;");
		document.getElementById("turn-text").innerHTML = "Black to Move";
	}
}

function clickToMove(event) {
	if (event.target.getAttribute("draggable") == "false") {
		return false;
	}
	document.getElementById(prevSq).removeAttribute("style", "background-color: rgb(239, 208, 11, 0.5);");
	document.getElementById(event.target.name).setAttribute("style", "background-color: rgb(239, 208, 11, 0.5);");
	prevSq = event.target.name;
}

function addObstructiveCats() {

	const noOfImages = 51; // update for more images
	let obstructiveCatPaths = new Set();

	while (obstructiveCatPaths.size < 4) {
		obstructiveCatPaths.add("/Gifs/" + "i" + (Math.floor(Math.random() * noOfImages) + 1) + ".gif");
	}

	obstructiveCatPaths = [...obstructiveCatPaths];

	for (let index = 0; index < 4; index++) {
		let image = document.createElement("img");
		image.src = obstructiveCatPaths[index];

		if (image.clientWidth > image.clientHeight) {
			image.setAttribute("height", "100%");
		} else {
			image.setAttribute("width", "100%");
		}

		image.draggable = false;

		let catSquare = document.getElementById(catSquares[index]);
		catSquare.removeEventListener('dragenter', dragEnter);
		catSquare.removeEventListener('dragover', dragOver);
		catSquare.removeEventListener('dragleave', dragLeave);
		catSquare.removeEventListener('drop', drop);
		catSquare.appendChild(image);
	}
}

function finalizeBoard() {
	if (gameStyle === "OBSTRUCTIVE") {
		addObstructiveCats();
	}
	// TODO add gameplay details
}

function setSquareSets() {
	whiteSquares = document.querySelectorAll(".white");
	blackSquares = document.querySelectorAll(".black");
	allPieces = document.querySelectorAll(".piece");
	allSquares = document.querySelectorAll(".square");
}

function displayGame() {
	buildBoard();
	convertPieceMap();
	addGamePiecesWithDraggableFeatures(getPieceImgPath());
	setSquareSets();
	addSquareListeners();
	finalizeBoard();
}

displayGame();


