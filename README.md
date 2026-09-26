# Tokensmp67

Premium **Token SMP** plugin for Minecraft **1.21 - 1.21.4** (Paper / Spigot, Java 21).

## Features

- **6-row (54 slot) Token GUI** — `/token` or `/tokensmp` opens `§8Token SMP - Menu` with glass-pane borders, three token tiers and a live profile head (slot 49).
- **Zombie Token (Common)** — `§a§lZombie Token` — passive +2 extra hearts; **Rotten Rush** (Shift + Left Click): Strength II + Speed I for 8s. 45s cooldown.
- **Blaze Token (Rare)** — `§6§lBlaze Token` — permanent Fire Resistance; **Fireball Barrage** (Shift + Right Click): straight-line small fireball. 30s cooldown.
- **Warden Token (Legendary)** — `§3§lWarden Token` — Resistance I + Night Vision; **Sonic Boom** (Shift + Left Click): 15-block raycast with SONIC_BOOM particles, 12.0 true damage (armor-bypassing) and 1.8x knockback. 60s cooldown.
- **Real-time action bar cooldown engine** — hotbar alerts for both blocked and successful activations.
- **Heart items (lifesteal)** — `/token withdraw <n>` converts stacked hearts into tagged `§4§lExtra Heart` items; right-click re-deposits them (40.0 HP / 20 heart hard cap).
- **Async data persistence** — all player data is written to `data.yml` asynchronously on quit and on shutdown.

## Building

The project builds with Maven and Java 21:

```bash
mvn clean package
```

The compiled plugin JAR is produced in `target/Tokensmp67-<version>.jar`.

### GitHub Actions

Every push (and pull request) to `main` triggers the [Build workflow](.github/workflows/build.yml), which:

1. Checks out the repository
2. Sets up Temurin JDK 21 with Maven caching
3. Compiles and packages the plugin (`mvn -B clean package`)
4. Uploads the JAR as a downloadable build artifact (`Tokensmp67-plugin`)

Pushing a tag like `v1.0.0` additionally publishes a GitHub Release with the JAR attached.

## Installation

1. Grab the JAR from the latest successful [Actions run](../../actions) (artifact `Tokensmp67-plugin`) or build it yourself.
2. Drop it into your server's `plugins/` folder.
3. Restart the server — works on Paper/Spigot 1.21 through 1.21.4.
