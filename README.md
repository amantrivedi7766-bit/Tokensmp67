# Tokensmp67 — TokenSMP

Enterprise-grade **Token SMP** plugin for **Paper / Spigot / Purpur 1.21+** (Java 21), built on pure Bukkit/Paper API — no NMS.

## Tokens (3 progressive tiers each)

| Token | Rarity | Tier 3 Ability |
|---|---|---|
| Zombie | Common | **Undead Enrage** — 15s Absorption IV (45s CD) |
| Skeleton | Common | **Archer's Focus** — +50% bow damage, arrow particle trails |
| Spider | Common | **Web Walker** — wall climbing, fall immunity, cloud jumps |
| Creeper | Rare | **Charged Overload** — explosion nova (30s CD) |
| Blaze | Rare | **Blazing Wrath** — fire nova (40s CD) |
| Enderman | Epic | **Warp Strike** — 15-block teleport strike (30s CD) |
| Witch | Epic | **Healing Mist** — AoE heal + Regeneration II (60s CD) |
| Warden | Legendary | **Sonic Boom** — raycast true damage + knockback (45s CD) |
| Admin | Admin-only | **God Mode** — Nether Shockwave → Chrono Freeze → Server Judgment |

Every token unlocks via **kill-task grinds + material costs**, upgraded through a split GUI (materials left, live task stats right), with a **crate wheel spin animation** on first unlock, fake-lightning + totem vortex on dropped token items, and a **live action-bar cooldown HUD**.

## Commands

- `/tokens` — open the token selection GUI (`tokensmp.player`, default: all)
- `/token balance` — list unlocked tokens in chat
- `/token stats` — live task grind + kill counts
- `/tokensadmin give|setprogress|forceupgrade|resetcooldown|reload` (`tokensmp.admin`, default: op)

The **Admin Token can only be created by an admin** via `/tokensadmin give <player> admin` — it never drops, crafts or spawns naturally.

## Configuration

Everything — lore frames, announcements, colors, cooldowns, tasks, costs, ability numbers — lives in `config.yml` (hot-reload with `/tokensadmin reload`). See the heavily commented template shipped with the plugin.

## Building / Downloading

Every push to `main` (and every PR) triggers the [GitHub Actions workflow](.github/workflows/build.yml):

1. Compiles with Temurin JDK 21 + Maven against Paper API 1.21.4
2. Uploads the plugin JAR as a build artifact named **`Tokensmp67-plugin`**

**Download the JAR:** repo → *Actions* → latest successful run → scroll down to *Artifacts* → `Tokensmp67-plugin` (zip containing `Tokensmp67-<version>.jar`) → drop into your server's `plugins/` folder.

Pushing a tag like `v2.0.0` also publishes a **GitHub Release** with the JAR attached.

Local build: `mvn clean package` (needs JDK 21).
