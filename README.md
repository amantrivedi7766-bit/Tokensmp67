# TokenSMP

Enterprise-grade **Token SMP** plugin for Minecraft Java 1.21+ (Paper primary, Spigot/Purpur compatible). Complete rewrite - zero legacy code.

## Features

- **8 player tokens × 3 progressive tiers**: Zombie, Skeleton, Spider, Creeper, Enderman, Blaze, Warden + the isolated Admin Token
- **Full token lifecycle**: `LOCKED → SPIN → UNLOCKED → CLAIMED → ACTIVE` with claim / unclaim (confirmation GUI) and re-claim
- **PvP token stealing**: kill a player to receive a timed, cinematic claim opportunity on their active token (fully configurable)
- **First-join cinematic spin**: 9-slot crate animation (2→5→10→15 tick phases, ~5s), Admin Token never appears
- **Real physical abilities** (Shift + Right Click): true-damage engine, event-based AoE damage, Chrono Freeze, Sonic Boom, Server Judgment and more - each with unique particles, sounds, timing and knockback
- **Live cooldown HUD**: `████████░░░░░░░░` action bar every 2 ticks, server-side timestamps that survive reconnects
- **Ground drop vortex**: cosmetic lightning + rotating TOTEM_OF_UNDYING/FLAME vortex on dropped token items
- **Polished GUIs**: selection menu with exact lore states (LOCKED / UNLOCKED / MAX TIER), per-token upgrade view, My Tokens overview and a full Admin control panel with confirmation menus
- **Absolute Admin Token isolation**: only `/tokensadmin give <player> admin` can ever generate it
- **Exploit-hardened GUIs**: shift-click, drag, double-click, number-key and drop-key attacks all cancelled
- **PDC-only item identity** + server-authoritative player data (lore is never trusted)

## Commands

| Command | Description |
|---|---|
| `/tokens` (or `/token menu`) | Open the token collection GUI |
| `/token balance` | List your unlocked tokens in chat |
| `/token stats` | Detailed live task grinds |
| `/tokensadmin` | Open the admin GUI |
| `/tokensadmin give <player> <token_id>` | Give a token item (incl. `admin`) |
| `/tokensadmin spin <player>` | Launch the token spin |
| `/tokensadmin setprogress <player> <task_id> <value>` | Force task progress |
| `/tokensadmin forceupgrade <player> <token_id>` | Skip to the next tier |
| `/tokensadmin resetcooldown <player>` | Clear all cooldowns |
| `/tokensadmin reload` | Atomic config reload |

Permissions: `tokensmp.player` (default: everyone), `tokensmp.admin` (default: op).

## Build

```
./gradlew clean build
```

The Gradle build compiles the sources, runs the JUnit 5 test suite (`src/test/java`) and produces **`build/libs/TokenSMP.jar`** containing `plugin.yml` (with the version expanded), `config.yml` and all compiled classes - zero runtime dependencies.

The GitHub Actions workflow (`.github/workflows/build.yml`):
- triggers on every **push**, **pull request** and **manual dispatch** (`workflow_dispatch`),
- sets up JDK 21 + Gradle, runs `./gradlew clean build` (compile + tests),
- uploads the JAR as the **`TokenSMP`** artifact,
- on `v*` **tags** publishes a GitHub Release with `TokenSMP.jar` attached.

## Requirements

- Java 21+ (Gradle 8.10+ toolchain is auto-provisioned by the wrapper)
- Minecraft 1.21+ (Paper recommended; works on Spigot/Purpur)
- No NMS, no CraftBukkit internals - version-sensitive lookups are centralized in `core/VersionCompatibility.java`
