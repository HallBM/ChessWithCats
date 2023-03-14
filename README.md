# Chess with Cats!

A web application that provides  a unique and fun twist on the classic game of chess. 
In this game, cats have taken over the chess board and are now an integral part of the gameplay. 
Players can choose from four different versions of the game, each with its own set of rules and challenges. 

# Versions Used

- Spring Boot v3.0.2
- Maven v3.0.1
- Hibernate v6.1.6
- Java v19
- Thymeleaf v3.1.1
- Spring Security v6.0.1
- MySQL v8.0
- Tomcat v9.0

# Features

- Player registration and login using Spring Security.
- Friend and game requests.
- Leaderboard to track your progress and compete with others.
- Four versions of chess to choose from!

# Chess Variations
### Classic Chess: 
Play a traditional game of chess without cats... how sad!
### Obstructive Cats: 
Cats have taken over your chess board. Play chess around these obstructive kitties!
    - Pawns can jump over kitties in the same column.
    - Kitties are obstructive for other pieces.
    - Otherwise, classic rules of chess apply.
### Ambiguous Cats: 
The chess pieces are cats. But let's be honest - cats all look the same. Play chess while trying to keep track of your pieces!
    - The pieces correspond to the opening board in a classic chess game.
    - You only have three attempts to make a move before the move is forfeited.
    - Otherwise, classic rules of chess apply.
### Defiant Cats: 
Cats never like being told what to do. Play cat chess with determined and stubborn felines that may stay where you place them... or may go where they please!
    - There is a 33% chance that a moved piece will migrate to an adjacent square.
    - Move migrations need not follow piece move constraints - kitties need to be free!
    - Forced chess moves will always be followed.
    - Otherwise, classic rules of chess apply.
    
# Getting Started
1. Clone the repository to your local machine.
2. Import the Maven project into your preferred IDE. 
3. Make sure that you have Spring Boot, Maven, Tomcat server and MySQL database installed on your machine.
4. Set up the MySQL database ```CREATE DATABASE chesswithcats;```
5. Configure the MySQL database by updating the 'application.properties' file with your MySQL credentials.
6. Build and run the game as a Spring Boot application
