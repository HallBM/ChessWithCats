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

		if (pieceMap[loc][0] == "w") {
			pieceImage.classList.add("white", "piece");
			pieceImage.setAttribute("draggable", isWhiteTurn && isWhitePlayer);
		} else {
			pieceImage.classList.add("black", "piece");
			pieceImage.setAttribute("draggable", !isWhiteTurn && !isWhitePlayer);
		}

		document.getElementById(loc).appendChild(pieceImage);
	}
}

function addSquareListeners() {
	allSquares.forEach(sq => {
		sq.addEventListener("dragenter", dragEnter)
		sq.addEventListener("dragover", dragOver);
		sq.addEventListener("dragleave", dragLeave);
		sq.addEventListener("drop", drop);
	});
}

function dragEnter(event) {
	event.preventDefault();
	event.target.classList.add("drag-over");
}

function dragOver(event) {
	event.preventDefault();
	event.target.classList.add("drag-over");
}

function dragLeave(event) {
	event.target.classList.remove("drag-over");
}

function dragStart(event) {

	event.dataTransfer.setData("text/plain", event.target.name);
	event.target.classList.add("hide");

	document.getElementById(prevSq).classList.remove("highlight");
	document.getElementById(event.target.name).classList.add("highlight");
	prevSq = event.target.name;

	
}

document.addEventListener("dragend", function(event) {
	const dropSuccessful = (event.dataTransfer.dropEffect !== "none");
	const movedPiece = document.getElementsByName(prevSq)[0];
	
	if (!dropSuccessful) {
			movedPiece.classList.remove("hide");
	} 

});

async function drop(event) {
	event.preventDefault();

	const startSquarePosition = event.dataTransfer.getData("text/plain");
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

	if (moveFailConditions) {
		movedPiece.classList.remove("hide");
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

	const moveResponse = await (async function(move) {
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

	if (moveResponse.valid === false) {
		movedPiece.classList.remove("hide");
		return false;
	}

	movedPiece.classList.remove("hide");

	for (let move of moveResponse.pieceMoves) {
		if (move[1] == null) {
			const enPassantCapture = document.getElementById(move[0]);
			enPassantCapture.removeChild(enPassantCapture.children[0]);
		} else {
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

	if (isWhitePlayer) {
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

	document.getElementById("move-display").innerHTML += moveResponse.officialChessMove;

	document.getElementById("rules").style.display = "none";
	document.getElementById("history").style.display = "block";

	if (moveResponse.gameOutcome == "CHECKMATE") {
		document.getElementById("background").style.filter = "blur(5px)";
		const popup = document.getElementById("checkmate-popup");
		popup.style.display = "inline-flex";

		setTimeout(function() {
			// Change the location of the current window to the new page
			window.location.href = "/games";
		}, 5000);
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
		catSquare.removeEventListener("dragenter", dragEnter);
		catSquare.removeEventListener("dragover", dragOver);
		catSquare.removeEventListener("dragleave", dragLeave);
		catSquare.removeEventListener("drop", drop);
		catSquare.appendChild(image);
	}
}

function finalizeBoard() {
	if (gameStyle == "obstructive") {
		addObstructiveCats();
	}

	if (!document.getElementById("move-display").textContent.trim()) {
		document.getElementById("rules").style.display = "block";

		switch (gameStyle) {
			case "classic":
				document.getElementById("rule-display").innerHTML = `Play a classic game of chess! The usual rules apply! 
				<br> <br><br>***NOTE*** <br>You are in Developer mode: Check is counted as checkmate.`;
				break;
			case "obstructive":
				document.getElementById("rule-display").innerHTML = `Play chess around these obstructive kitties! <br> 1) 
				Pawn can jump over kitties in the same column. <br> 2) Kitties are obstructive for other pieces. <br> 3) 
				Otherwise, the usual rules of chess apply. <br><br><br> ***NOTE*** <br>You are in Developer mode: Check is counted as checkmate.`;
				break;
			case "ambiguous":
				document.getElementById("rule-display").innerHTML = `Play chess while trying to keep track of your 
				kitty pieces! <br> 1) The pieces all look the same, but they're not! <br> 2) Pieces correspond to opening 
				board in a classic chess game. <br> 3) You only have 3 attempts to make a move, otherwise the move is forfeited. 
				<br> 4) The usual rules of chess apply. <br><br><br>***NOTE***<br>You are in Developer mode: Check is counted as checkmate.;`
				break;
			case "defiant":
				document.getElementById("rule-display").innerHTML = `Play chess with defiant kitty pieces!<br>1) There is a 
				33% chance that a moved piece will migrate to an adjacent square.<br>2) Move migrations need not follow piece 
				move constraints - kitties need to be free!<br>3) Forced moves will always be followed<br>4) Otherwise, the usual 
				rules of chess apply.<br><br><br>***NOTE***<br>You are in Developer mode: Pieces not yet defiant! Check is counted as checkmate.`;
				break;
			default:
				document.getElementById("rule-display").innerHTML = "";
		}
		document.getElementById("history").style.display = "none";
	} else {
		document.getElementById("rules").style.display = "none";
		document.getElementById("history").style.display = "block";
	}
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



















