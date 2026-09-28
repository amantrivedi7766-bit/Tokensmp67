# TokenSMP V2

A complete, production-grade **Token SMP** plugin for Paper / Spigot / Purpur **1.21+** (Java 21, no NMS).

> **Core rule: ONE PLAYER = ONE CLAIMED TOKEN.** A player can own many tokens but only have a single active one - unclaim first, claim next, enforced server-side everywhere (GUI, commands, death claims, admin actions, reconnect, reload).

## Highlights

- **20 unique player tokens** x 3 tiers each - Zombie, Skeleton, Spider, Creeper, Enderman, Blaze, Warden, Wither, Slime, Phantom, Guardian, Evoker, Piglin, Ghast, Magma Cube, Witch, Vindicator, Ravager, Breeze, Elder Guardian - every one with unique passives, unlock grinds, upgrade materials and a **physically real Tier 3 combat ability** (no potion-only abilities, no duplicates).
- **Holdable token items** - the claimed token exists as a real Minecraft item with secure PDC data (`token_id`, `token_tier`, `token_instance_id`, `token_owner_uuid`, `claimed_state`, `item_version`) and duplicate-instance protection.
- **Held-token abilities** - SHIFT + RIGHT CLICK only works while you are actually holding your own PDC-verified token item.
- **60-second first-join system** - new players wait exactly one minute with preparation titles, countdown and a GET READY warning before the cinematic 9-slot spin (~5s, 2→5→10→15 tick phases). Server-side, survives reconnects, never triggers twice.
- **Claiming is never a spin** - a claim plays a short confirmation burst only; spins are reserved for the initial unlock and admin-triggered spins.
- **PvP token stealing** - kill the holder of an active token to receive a timed claim opportunity (direct claim, no spin, one-active-token rule still applies).
- **Premium GUIs** - paginated player menu (all 20 tokens with LOCKED / UNLOCKED / MAX lore states), per-token upgrade view with material requirements, confirmation menus, and a full admin control center (player management, give/remove/force-upgrade/claim/unclaim, progress control, cooldown resets, statistics, reload).
- **Physical damage engine** - real event damage, true damage, projectile damage and area damage; a real targeting system (area / ray / cone / nearest with PvP, spectator and dead-state validation).
- **Cooldown HUD** every 2 ticks with a progress bar, ground-drop vortex animation for dropped token items, exploit-proof GUIs (shift-click, drag, number keys, hotbar swaps, collection all blocked).

## Commands

| Command | Permission | Description |
|---|---|---|
| `/tokens`, `/token menu\|balance\|stats` | `tokensmp.player` (default) | Player token hub |
| `/tokensadmin` | `tokensmp.admin` (op) | Admin GUI + full registry |
| `/tokensadmin give <player> <token_id>` | `tokensmp.admin` | Spawn a token item (only way to get the Admin Token) |
| `/tokensadmin spin <player>` | `tokensmp.admin` | Launch the cinematic spin |
| `/tokensadmin setprogress <player> <token_id> <value>` | `tokensmp.admin` | Force task progress |
| `/tokensadmin forceupgrade <player> <token_id>` | `tokensmp.admin` | Skip to the next tier |
| `/tokensadmin resetcooldown <player>` | `tokensmp.admin` | Clear all cooldowns |
| `/tokensadmin claim / unclaim / remove / inspect` | `tokensmp.admin` | Claim-state management |
| `/tokensadmin reload` | `tokensmp.admin` | Atomic config reload |

The **Admin Token** is absolutely isolated: it never appears in GUIs, spins, `/token balance`, `/token stats` or tab completion for unauthorized players - only `/tokensadmin give <player> admin` can generate it.

## Building

```bash
mvn clean package
```

Produces `target/TokenSMP.jar`. The included GitHub Actions workflow (`.github/workflows/build.yml`) builds on every push/PR, uploads the `TokenSMP` artifact and publishes a GitHub Release with the JAR on `v*` tags.

## Configuration

Everything - the 60-second first-join timing, claim-system rules, stealing, spin pacing, ability values for all 20 tokens, every message, GUI title and sound - is configurable in `config.yml` and reloads atomically with `/tokensadmin reload`.
