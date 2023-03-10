const [_, base, gameStyle, gameId, playerColor] = window.location.pathname.split("/");
let pieceMap = JSON.parse(document.getElementById("pieceMapJson").innerHTML);
const chessboard = document.getElementById("chessboard");
const isPlayerWhite = chessboard.classList.contains("render_white");

let blackPieces = [];
let whitePieces = [];
let allSquares = [];
let catSquares = [];

let isWhiteTurn = document.getElementById("turn").classList.contains("white-turn");
let isWhitePlayer = playerColor == "white";
let prevSq = "A1";

const numbers = isPlayerWhite ? "87654321" : "12345678";
const letters = isPlayerWhite ? "ABCDEFGH" : "HGFEDCBA";

function buildBoard() {
	let isLightSquare = true;

	for (let row = 0; row < 8; row++) {
		for (let col = 0; col < 8; col++) {
			let label;
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
	if (gameStyle == "ambiguous") {
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

		let pieceImage = document.createElement("img");
		pieceImage.setAttribute("name", loc);
		pieceImage.setAttribute("src", piecePath[0] + pieceMap[loc] + piecePath[1]);
		pieceImage.alt = pieceMap[loc];
		pieceImage.style = "height: 100%; width:100%; object-fit:contain;";
		pieceImage.addEventListener("dragstart", dragStart);
		
		if (pieceMap[loc][0] == "w"){
			pieceImage.classList.add("white", "piece");
			pieceImage.setAttribute("draggable", isWhiteTurn && isWhitePlayer);
		} else {
			pieceImage.classList.add("black", "piece");
			pieceImage.setAttribute("draggable", !isWhiteTurn && !isWhitePlayer);
		}
		
		document.getElementById(loc).appendChild(pieceImage);
	}
}

function addSquareListeners(){
    allSquares.forEach(sq => {
        sq.addEventListener('dragenter', dragEnter)
        sq.addEventListener('dragover', dragOver);
        sq.addEventListener('dragleave', dragLeave);
        sq.addEventListener('drop', drop);
    });
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

function dragStart(event) {

	event.dataTransfer.setData('text/plain', event.target.name);
	event.target.classList.add("hide");

	document.getElementById(prevSq).classList.remove("highlight");
	document.getElementById(event.target.name).classList.add("highlight");
	prevSq = event.target.name;
	
}

async function drop(event) {
	event.preventDefault();
	
	const startSquarePosition = event.dataTransfer.getData('text/plain');
	const movedPiece = document.getElementsByName(startSquarePosition)[0];
	
	const isEmptyEndSquare = isDroppedOnEmptySquare(event.target);
	const capturedPiece = !isEmptyEndSquare ? event.target : null;
	const endSquare = isEmptyEndSquare ? event.target : capturedPiece.parentElement;
	const endSquarePosition = endSquare.id;
	
	endSquare.classList.remove("drag-over");

	const moveFailConditions = 
		(capturedPiece != null && capturedPiece.alt[1] == "k") ||
		(!isEmptyEndSquare && hasSameColor(movedPiece, capturedPiece)) ||
		(movedPiece.name === endSquarePosition);
	
	if (moveFailConditions){
		movedPiece.classList.remove('hide');
		return false;
	}
	
	let promotionPiece = null; // TODO check pawn promotion; get selection from user
	
	const move = {
		gameId: gameId,
		gameStyle: gameStyle,
		isChecked: null,
		promotionPiece: promotionPiece,
		startPos: startSquarePosition, 
		endPos: endSquarePosition
	};
	
	const moveResponse = await (async function (move) {
		try {
			const response = await fetch("/game/move", {
				method: "post",
				body: JSON.stringify(move),
				headers: { "Content-Type": "application/json; charset=utf-8" },
				cache: "no-cache"
			});
			return await response.json();
		} catch (error) {
			console.error(error);
		}
	})(move);
	
	if (moveResponse.valid === false){
		movedPiece.classList.remove('hide');
		return false;
	}
	
	console.log(moveResponse.pieceMoves);
	movedPiece.classList.remove('hide');

	for (let move of moveResponse.pieceMoves){
		if (move[1] == null){
			const enPassantCapture = document.getElementById(move[0]);
			enPassantCapture.removeChild(enPassantCapture.children[0]);	
		} else {
			let startSquare = document.getElementById(move[0]);
			let endSquare = document.getElementById(move[1]);
			let movedPiece = document.querySelector("img[name='" + move[0] + "']");
			
			if (endSquare.hasChildNodes()) {
				for (let child of endSquare.childNodes) {
					if (child.nodeName === "IMG") {
						endSquare.removeChild(child);
						break;
					}
				}
			}

			endSquare.appendChild(movedPiece);
			movedPiece.name = move[1];
		}
	}
	
	document.getElementById(prevSq).removeAttribute("style", "background-color: rgb(239, 208, 11, 0.5);");
	endSquare.setAttribute("style", "background-color: rgb(239, 208, 11, 0.5);");
	prevSq = endSquarePosition;

	isWhiteTurn = !isWhiteTurn;
	
	if(isWhitePlayer){
		whitePieces.forEach(w => {
			w.draggable = isWhiteTurn && isWhitePlayer;
		});
	} else {
		blackPieces.forEach(b => {
			b.draggable = !isWhiteTurn && !isWhitePlayer;
		});
	}

	if (isWhiteTurn) {
		document.getElementById("turn").classList.remove("black-turn");
		document.getElementById("turn").classList.add("white-turn");		
		document.getElementById("turn-text").innerHTML = "White to Move";

	} else {
		document.getElementById("turn").classList.remove("white-turn");
		document.getElementById("turn").classList.add("black-turn");	
		document.getElementById("turn-text").innerHTML = "Black to Move";
	}
	
	
	
}

function hasSameColor(piece1, piece2) {
	return (piece1.classList.contains("white") && piece2.classList.contains("white")) || 
 		 (piece1.classList.contains("black") && piece2.classList.contains("black"));
}

function isDroppedOnEmptySquare(dropTarget) {
	return dropTarget.id !== "";
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
		image.alt = "C";

		image.style = "height:100%; width: 100%; object-fit:contain;"
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
	if (gameStyle == "obstructive") {
		addObstructiveCats();
	}
	// TODO add gameplay details
}

function setSquareSets() {
	whitePieces = document.querySelectorAll(".white");
	blackPieces = document.querySelectorAll(".black");
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



















