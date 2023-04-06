const [_, base, gameStyle, gameId, playerColor] = window.location.pathname.split("/");
const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
const pieceMap = JSON.parse(document.getElementById("pieceMapJson").innerHTML);
const chessboard = document.getElementById("chessboard");
const isPlayerWhite = chessboard.classList.contains("render_white");

let blackPieces = [];
let whitePieces = [];
let allSquares = [];
let catSquares = [];

let isWhiteTurn = document.getElementById("turn").classList.contains("white-turn");
let isWhitePlayer = playerColor == "white";

let blackMoveHighlightSquareId = null;
let whiteMoveHighlightSquareId = null;
let tempMoveHighlightSquareId = null;

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

	if (tempMoveHighlightSquareId != null) {
		document.getElementById(tempMoveHighlightSquareId).classList.remove("highlight");
	}
	document.getElementById(event.target.name).classList.add("highlight");
	tempMoveHighlightSquareId = event.target.name;

}

document.addEventListener("dragend", function(event) {
	const dropSuccessful = (event.dataTransfer.dropEffect !== "none");
	const movedPiece = document.getElementsByName(tempMoveHighlightSquareId)[0];

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

	event.target.classList.remove("drag-over");

	const moveFailConditions =
		(capturedPiece != null && capturedPiece.alt[1] == "k") ||
		(!isEmptyEndSquare && hasSameColor(movedPiece, capturedPiece)) ||
		(movedPiece.name === endSquarePosition);

	if (moveFailConditions) {
		movedPiece.classList.remove("hide");
		return false;
	}
	
	let promotionPiece = null; // TODO check pawn promotion; get selection from user

	if (isPlayerWhite && movedPiece.alt === "wp" && endSquarePosition.charAt(1) === "8") {
		const promotionMenu = document.getElementById("promotion-popup");
		promotionMenu.style.display = "block";

		const selectionPromise = new Promise((resolve) => {
			document.querySelectorAll(".promotion-button").forEach((button) => {
				button.addEventListener("click", () => {
					const selectedPiece = button.value;
					resolve(selectedPiece);
				});
			});
		});
		promotionPiece = await selectionPromise;
		promotionMenu.style.display = "none";
	}

	if (!isPlayerWhite && movedPiece.alt === "bp" && endSquarePosition.charAt(1) === "1") {
		const promotionMenu = document.getElementById("promotion-popup");
		promotionMenu.style.display = "block";

		const selectionPromise = new Promise((resolve) => {
			document.querySelectorAll(".promotion-button").forEach((button) => {
				button.addEventListener("click", () => {
					const selectedPiece = button.value.toLowerCase();
					resolve(selectedPiece);
				});
			});
		});

		promotionPiece = await selectionPromise;
		promotionMenu.style.display = "none";
	}

	const move = {
		gameId: gameId,
		promotionPiece: promotionPiece,
		startPos: startSquarePosition,
		endPos: endSquarePosition
	};

	const moveResponse = await fetch("/game/move", {
		method: "post",
		body: JSON.stringify(move),
		headers: {
			"Content-Type": "application/json; charset=utf-8",
			[header]: token
		},
		cache: "no-cache",
	})
		.then(response => {
			if (response.status === 200) {
				return response.json();
			} else if (response.status === 409) {
				throw new Error("Invalid move. Try again.");
			} else {
				throw new Error("Unexpected error occured");
			}
		})
		.catch(error => {
			console.error(error);
			return null;
		});

	movedPiece.classList.remove("hide");

	if (moveResponse === null) {
		return false;
	}

	makeMove(moveResponse);
}

function makeMove(moveResponse) {
	let endSquare, movedPiece;

	if (moveResponse.moveNotation !== "XXX ") {
		for (let move of moveResponse.pieceMoves) {
			if (move[1] === "ep") {
				const enPassantCapture = document.getElementById(move[0]);
				enPassantCapture.removeChild(enPassantCapture.children[0]);
			} else {
				endSquare = document.getElementById(move[1]);
				movedPiece = document.querySelector("img[name='" + move[0] + "']");

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

		if (tempMoveHighlightSquareId != null) {
			document.getElementById(tempMoveHighlightSquareId).classList.remove("highlight");
		}
		endSquare.classList.add("highlight");
		tempMoveHighlightSquareId = endSquare.id;

		if (isWhiteTurn) {
			if (whiteMoveHighlightSquareId != null) {
				document.getElementById(whiteMoveHighlightSquareId).classList.remove("highlight");
			}
			whiteMoveHighlightSquareId = endSquare.id;
		} else {
			if (blackMoveHighlightSquareId != null) {
				document.getElementById(blackMoveHighlightSquareId).classList.remove("highlight");
			}
			blackMoveHighlightSquareId = endSquare.id;
		}
	}

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

	document.getElementById("move-display").innerHTML += moveResponse.moveNotation;

	document.getElementById("rules").style.display = "none";
	document.getElementById("history").style.display = "block";

	if (moveResponse.moveNotation.substring(moveResponse.moveNotation.length - 5).includes("=")) {
		let promoted = moveResponse.moveNotation.lastIndexOf("=") + 1;
		updateImage(movedPiece, moveResponse.moveNotation.charAt(promoted));
	}	
		
	if (moveResponse.gameOutcome == "CHECKMATE") {
		document.getElementById("background").style.filter = "blur(5px)";
		const popup = document.getElementById("checkmate-popup");
		popup.style.display = "inline-flex";
		const outcome = document.getElementById("outcome");
		outcome.innerHTML = "Checkmate! You " + (!isWhiteTurn === isWhitePlayer ? "WIN" : "LOSE");

		setTimeout(function() {
			// Change the location of the current window to the new page
			window.location.href = "/games";
		}, 5000);
	}
}

function updateImage(movedPiece, promotedPiece){
	promotedPiece = promotedPiece == promotedPiece.toLowerCase() ?
				("b" + promotedPiece) :
				("w" + promotedPiece.toLowerCase());
	let piecePath = getPieceImgPath();	
	movedPiece.src = piecePath[0] + promotedPiece + piecePath[1];
	movedPiece.alt = promotedPiece;
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

const urlEndPoint = "http://localhost:8080/subscribe/" + gameId + "/" + (playerColor === "white" ? "black" : "white");
const eventSource = new EventSource(urlEndPoint);

eventSource.onopen = function() {
	console.log("connection is established");
	document.getElementById("status").innerHTML = "Real-time gameplay";
};

eventSource.onerror = function(event) {
	console.log("connection state: " + eventSource.readyState + ", error: " + event);
	console.log(event);
	//document.getElementById("status").innerHTML = "Refresh to update";
	//eventSource.close();
};

eventSource.onmessage = function(event) {
	console.log(event.move);   //"id: ") + event.lastEventId + ", data: " + event.data);

};

eventSource.addEventListener("move", function(event) {

	if (event.data === null) {
		return;
	}
	const moveResponse = JSON.parse(event.data);
	makeMove(moveResponse);

}, false);

eventSource.addEventListener("close", function(event) {
	eventSource.close();
	console.log("closed");//'id: ' + event.lastEventId + ', data: ' + event.data);
}, false);

function enterGame() {
	buildBoard();
	convertPieceMap();
	addGamePiecesWithDraggableFeatures(getPieceImgPath());
	setSquareSets();
	addSquareListeners();
	finalizeBoard();
}

enterGame();