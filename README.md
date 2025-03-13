# Maze Game

A 2D maze adventure game built in Java with Swing.

## Game Overview

This maze game challenges players to navigate through procedurally generated mazes while collecting coins, keys, and treasures while avoiding spike traps. The game features:

- Multiple difficulty levels
- Animated characters and objects
- Sound effects and background music
- Health system
- Score tracking

## Game Elements

### World Elements
- **Maze Walls**: Define the boundaries of the maze
- **Floor**: Navigable paths
- **Exit**: Reach this to complete the level
- **Spike Traps**: Hazards that damage the player

### Collectibles
- **Coins**: Scattered throughout the maze, often in trails
- **Keys**: Required to unlock the exit
- **Treasures**: Special items that award bonus points

## Controls

- **Arrow Keys**: Move the player character (up, down, left, right)

## Gameplay Systems

### Audio System
The game includes a robust audio system (`AudioPlayer.java`) with features like:
- Background music
- Sound effects
- Volume control
- Audio looping

### Health System
Players have a health bar that decreases when taking damage from traps. The visual representation updates based on current health.

### Animation
Game objects feature smooth animations:
- Coins have spinning animations
- Spikes have movement animations
- Keys have shimmering animations

## Game Structure

### Main Menu
The main menu provides access to:
- Level selection
- Settings
- Credits

### Settings
Adjust game settings like:
- Volume control

### Level Selection
Choose from three difficulty levels:
- Level 1
- Level 2
- Level 3

## Technical Implementation

### Procedural Maze Generation
The maze is procedurally generated using a depth-first search algorithm (`MazeLogic.java`).

### Input Handling
Player movement is managed through a dedicated key listener (`PlayerKeyListener.java`).

### Game Objects
All game objects are implemented as separate classes:
- `Hero` (player character)
- `Coin`
- `Key`
- `Spike`
- `HealthBar`

### UI Components
The game uses Swing for its user interface:
- `GameMenu`
- `MainMenuPanel`
- `SettingsPanel`
- `CreditsPanel`
- `SetLevel` (level selection)
- `GameOver` screen

## Installation and Running

### Requirements
- Java Development Kit (JDK) 8 or higher
- Java Runtime Environment (JRE)

### Running the Game
1. Compile all Java files
2. Run the `Main` class

```bash
javac *.java
java Main
```

## Future Improvements
- Save game functionality
- More levels and difficulty options
- Enemy characters
- Power-ups
- Boss fights

## Credits
Game developed by [Rupayan das]
