# Chess

## About the Project



This project is an implementation of the classic game of chess, developed as part of the Programming Paradigms course at the Faculty of Mathematics, University of Belgrade. 

It supports all standard chess rules, including special moves such as castling, en passant, and pawn promotion.

The game records the entire move history, allowing players to review the completed game move by move. It also features a chess clock, with multiple time controls available.

When launching the application, players first select the desired game duration. To make a move, simply click on the piece you want to move, followed by one of its legal destination squares.

### Gameplay Demo

![Gameplay Demo](https://github.com/Pesic1704/Chess/blob/main/docs/simple_game.gif)

### Move History Demo

After a game has ended, players can review previous board positions by selecting individual moves from the move history panel on the left.

![Move History Demo](https://github.com/Pesic1704/Chess/blob/main/docs/move_history.gif)

---

## Technologies Used

- Kotlin
- Compose Multiplatform
- Gradle
- IntelliJ IDEA

---

## Prerequisites

- Java 21 (Temurin or OpenJDK 21)
- Linux desktop environment

---

## Building and Running from Source

1. Clone the repository:

```bash
git clone https://github.com/Pesic1704/Chess.git
cd Chess
```

2. From the project root directory, run:

```bash
chmod +x gradlew
./gradlew clean run
```

> **Note:** Make sure that JDK 21 is installed and configured correctly. Otherwise, Gradle may fail to build or run the application.

---

## Authors

- **Petar Pešić** 
- **Tadija Matić**  

