# JavaByte

An isometric, low-poly 3D renderer and mini game-engine, built entirely in Java using **LWJGL 3** (OpenGL, GLFW, OpenAL) and **JOML** for math.

The goal is a lightweight, reusable engine capable of running a small mini-game — not just a one-off demo — covering rendering, input, audio, and a core game loop.

## Features (planned)

- [ ] Window creation and OpenGL context setup (GLFW)
- [ ] Isometric camera (orthographic projection, fixed-angle view)
- [ ] Low-poly mesh rendering with flat shading
- [ ] Input handling (keyboard/mouse via GLFW)
- [ ] Audio playback (OpenAL)
- [ ] Core game loop (fixed timestep update/render)
- [ ] Small mini-game to demonstrate the engine
- [ ] Optional: procedurally generated terrain (Perlin/Simplex noise)

## Tech Stack

| Purpose | Library |
|---|---|
| Windowing / Input | [LWJGL 3](https://www.lwjgl.org/) (GLFW) |
| Rendering | LWJGL 3 (OpenGL) |
| Audio | LWJGL 3 (OpenAL) |
| Math (vectors/matrices) | [JOML](https://github.com/JOML-CI/JOML) |
| Build | Gradle |
| Language | Java 21 |

## Project Structure

```
app/
└── src/main/java/game/engine/
    ├── App.java              # Entry point (main method, game loop thread)
    ├── renderEngine/         # Window/display management, rendering pipeline
    ├── input/                # Input handling (planned)
    └── audio/                # Audio handling (planned)
```

## Getting Started

### Prerequisites
- JDK 21+
- Windows (current dev environment for the team)

### Build & Run

```bash
git clone https://github.com/cablecorn/JavaByte.git
cd JavaByte
.\gradlew run
```

Gradle will automatically pull down LWJGL, JOML, and the required native libraries — no manual dependency setup needed.

## Team

Three-person semester project team.

## Timeline

Roughly a 2.5-month build:
- **Weeks 1–3:** Renderer fundamentals (window, shaders, mesh rendering, camera) + game loop concept pitching
- **Week 4+:** Visuals and gameplay mechanics
- **Remaining weeks:** Input/output, audio, reactivity, and (if time allows) platform portability

## License

TBD
