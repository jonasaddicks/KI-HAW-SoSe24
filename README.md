# Connect4 AI

## Overview

This project is the practical part of a student assignment for the module Artificial
Intelligence. It implements a Connect Four AI based on a classic Minimax
search with alpha-beta pruning and a heuristic evaluation function whose parameters are
optimized by a genetic algorithm. A genome encodes weights for positional scoring,
major/minor threat weighting, win evaluation and a positional score table for the board.

The implementation is console-only and intended for experimentation and evaluation
(training, benchmarking, headless AI-vs-AI matches and interactive play). No external
libraries are required — the code targets Java 21 and uses the JDK standard library only.
Gradle is used to build and run the project.


## Quickstart

Prerequisites:
- Java 21 (or the JDK version you compile/run with)
- Gradle wrapper (project includes ``./gradlew``)

Build:
```
./gradlew build
```

Run:
- From an IDE: run ``Client.main(...)``.
- From the command line (if the application plugin is configured):

```
./gradlew run
```

Which mode runs is controlled by ``game.GameProperties.GAME_MODE`` (see Configuration below).
Typical development flow:
- Adjust ``GameProperties`` / ``TrainingProperties`` constants if needed.
- Build.
- Run ``Client`` (or run ``TrainingGround`` via ``Client`` in training mode).


## Configuration

Two central classes hold configurable constants:
`player.ai.genetic.TrainingProperties` (defaults in this project)
```java
public static final int POPULATION_SIZE = 500;
public static final int NR_BENCHMARK_GAMES = 300;
public static final int NR_FITNESS_GAMES = 300;
public static final int NR_GENERATIONS = 1000;

public static final double ELITIST_SHARE = 0.1;
public static final int TOURNAMENT_SIZE = 5;

public static final double GENOME_MUTATION_PROBABILITY = 0.8;
public static final double GEN_MUTATION_PROBABILITY = 0.2;
public static final int MUTATION_RANGE = 2;
```

`game.GameProperties`
```java
public static final int ROWS = 6;
public static final int COLS = 7;

// GAME_MODE:
// 0 = Multiplayer (Human vs Human)
// 1 = Single player (Human vs AI)
// 2 = AI-only (AI vs AI, use provided genomes)
// 3 = Train (run genetic algorithm training)
// 4 = Benchmark AI vs AI
// 5 = Benchmark AI vs Player
public static final int GAME_MODE = 1;

public static final boolean PLAYER1_STARTS = true;
public static final int NR_BENCHMARK_GAMES = 500;
```
Change these constants before launching the application to select training vs play vs benchmarking.


## Project / Runtime Artifacts

The project reads/writes several files under the ``resource`` paths resolved by ``ResourceLoadHelper``.
Important file locations used by the code (relative resource names):
- ``evolutionary/genome.selection/fittestSelection`` - fittest genomes log (one Base64 genome per line)
- ``evolutionary/genome.selection/benchmarkSelection`` - benchmark genomes (each genome per line)
- ``evolutionary/genome.selection/aiVsAiSelection`` - used for AI-vs-AI runs
- ``evolutionary/stats/genStats`` - training statistics & logs
- ``evolutionary/generation`` - stores the last generation number for training restarts
- ``aiBenchmark/stats/stats`` - benchmark summaries for AI benchmarking runs

The loader expects Base64-encoded genome strings (one genome per line) and uses
``GenomeLoader`` to read them.


## Genome Format

- The genome raw byte array length is 51 bytes. 
  - Bytes 0–7: weight parameters (positional/opponent weights, major/minor weights, win weights, etc.)
  - Bytes 8–49: positional score table (6×7 matrix, row-major)
  - Byte 50: additional win-evaluation parameter
- On disk the genome is stored as a Base64 string (encoded from the 51-byte array). Many helper methods exist for encoding/decoding (``Genome.encodeGenome``, ``Genome.decodeGenome``).


## Main Features

- Minimax search with alpha-beta pruning for move selection (AI uses a fixed preferred move order for pruning efficiency)
- Heuristic evaluation based on:
  - positional score matrix 
  - major/minor threats 
  - win evaluation weight 
  - separate weights for own and opponent values 
- Genetic algorithm to optimize evaluation parameters:
  - population initialization, elitist selection, tournament selection, crossover and mutation 
  - parallel fitness evaluation (one thread per genome fitness session)
  - periodic benchmarking of the current fittest genome against benchmark genomes 
- Console-based visualization for the board (basic, for demonstration only)


## Project Layout

- ``game`` - board, game rules, game properties
- ``player`` - ``Player``, ``HumanPlayer``, ``PlayerProperty``
- ``player.ai`` - ``AIPlayer`` implementation (Minimax + evaluation)
- ``player.ai.genetic`` - ``Genome``, ``GenomeLoader``, ``TrainingGround``, ``TrainingProperties``, ``GenomeTranslator`` (utilities)
- ``Client`` - main entry point and benchmark/training runner
- ``ResourceLoadHelper`` - helper to resolve resources on the classpath
- ``TrainingSupervisor`` - enables interaction with training in process


## Notes & Limitations

- Console visualization is intentionally minimal and intended for demonstration only. Column/row alignment and Unicode glyph rendering can vary across terminals.
- The project uses Unicode glyphs for player tokens (``0x0001F535``, ``0x0001F534``, ``0x000026AB``). Some terminals or fonts may not display these symbols correctly; expect potential layout/encoding issues depending on environment.
- No external libraries are required — everything uses JDK standard APIs (tested against modern JDK versions; this project is written for Java 21).
- The Connect Four game is a solved game in theory; this project focuses on demonstrating AI techniques (Minimax with alpha-beta pruning) and applying genetic optimization to a heuristic evaluator, not on discovering new theoretical results.


## Reproduction / Logging

- Training output, fittest genome snapshots and benchmark logs are appended to the files listed under Project / runtime artifacts.
- The ``Client`` class measures elapsed execution time and prints it on exit.