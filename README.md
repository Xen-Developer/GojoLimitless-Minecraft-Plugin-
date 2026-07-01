# Gojo Limitless

A Spigot/Paper plugin that adds combat mechanics and custom abilities inspired by Satoru Gojo. Built with clean performance patterns to handle heavy particles and vector math without lagging the server's main thread.

---

## 🚀 Key Features

* **Infinite Void (Domain Expansion):** Spawns a custom arena that catches nearby targets, freezes them in place, and handles unique visual effects using client-side packet manipulation.
* **Blue & Red Techniques:** Uses directional vector math to dynamically manipulate entities. Blue pulls targets into a focal point, while Red acts as a powerful knockback effect.
* **Hollow Purple:** Fires a high-velocity particle projectile along an accurate raycast track, checking bounding box collisions in real-time to damage targets and scale terrain destruction.
* **Mobility & Awakening:** Includes custom short-range teleportation using line-of-sight safety checks to block wall-glitches, alongside an awakening state manager that activates advanced versions of standard skills.

---

## 🛠️ Codebase Architecture

* **Thread Management:** Heavy particle loops and duration timers run inside an async thread system (`TickManager`) to keep the main server thread light and stable.
* **Target Detection:** Uses vector-based raycasting to handle hit registration instantly, avoiding slow loops that scan every entity in the world.
* **Extensible Skills:** Built on top of an abstract base class (`Skill.java`), making it easy to change cooldowns, registration rules, or deploy new custom abilities without repeating code.

---

## 📁 Folder Structure

```text
.
├── pom.xml                               # Dependencies (Paper API, PacketEvents, etc.)
└── src
    └── main
        ├── java
        │   └── com.xen.gojolimitless
        │       ├── GojoLimitlessPlugin.java  # Plugin startup and registry loading
        │       ├── commands/                 # Command routing and logic
        │       ├── listeners/                # High-frequency event handling
        │       ├── managers/                 # Core logic (Data loading, domains, timers)
        │       ├── skills/                   # Abstract base implementation and sub-skills
        │       └── util/                     # Raycasters, math tools, and visual effects
        └── resources
            ├── config.yml                # Configurable values (damage, cooldowns, scales)
            └── plugin.yml                # Spigot plugin definition file

```

---

## ⚙️ Compilation

### Requirements

* Java 17+
* Maven

### Build

Run the following command in the root folder:

```bash
mvn clean package

```

The compiled jar will be located inside the `target/` directory.
