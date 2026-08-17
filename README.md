# 🛡️ StaffStatistic

[![Release](https://img.shields.io/badge/Release-v1.0-00D2FF.svg)](https://github.com/Fedossil/StaffStatistic/releases)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot%20%7C%20Purpur-blue.svg)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-17%20%2F%2021-orange.svg)](https://www.oracle.com/java/)
[![Modrinth](https://img.shields.io/badge/Modrinth-Available-00AF5C.svg?logo=modrinth)](https://modrinth.com/plugin/staffstatistic)

A modular, production-ready staff administration plugin for Minecraft servers (**1.16.5 – 1.21+**). 

**StaffStatistic** automates staff activity monitoring, tracks clean on-duty playtime, dynamically manages moderation permissions via LuckPerms, and synchronizes records directly to Discord and Google Sheets.

---

## ✨ Features

* 🕒 **Duty & Shift System (`/staffwork` / `/sw`):**
  * Accurately tracks active duty time (ignores regular playtime/AFK).
  * Automatically grants staff permissions upon entering duty and strips them upon leaving or disconnecting.
* 🖥️ **Interactive Management GUI (`/staff`):**
  * Displays player skin heads sorted by rank hierarchy/priority.
  * Real-time online/offline status indicators (`[● ONLINE]` / `[● OFFLINE]`).
  * In-menu one-click actions: issue warns, rebukes, clear infractions, or remove staff.
* ⚖️ **Punishment System:**
  * Commands: `/staff warn <player> [reason]` & `/staff rebuke <player> [reason]`.
  * Configurable warn/rebuke limits per role with automatic kick triggers.
* 📊 **External Hooks & Logging:**
  * **LiteBans / AdvancedBan:** Fetches total bans and mutes issued by staff.
  * **Discord Webhooks (`discord.yml`):** Formatted embed alerts on duty changes and penalties.
  * **Google Sheets (`sheets.yml`):** Automated shift session exports via Google Apps Script.
* 🌐 **Multi-Language (i18n):**
  * Full UTF-8 support with separate configs in `locale/` (`ru_RU`, `en_US`) and HEX color gradient formatting (`&#RRGGBB`).

---

## 📋 Commands & Permissions

| Command | Description | Permission |
| :--- | :--- | :--- |
| `/staff` | Opens the interactive staff management GUI | `staffstatistic.admin` |
| `/staffwork` (or `/sw`) | Toggle moderation duty mode | `staffstatistic.work` |
| `/staff add <player> <role>` | Add a player to a specific staff role | `staffstatistic.admin` |
| `/staff kick <player>` | Remove a staff member | `staffstatistic.admin` |
| `/staff up <player> <role>` | Promote staff member to a new role | `staffstatistic.admin` |
| `/staff down <player> <role>` | Demote staff member | `staffstatistic.admin` |
| `/staff warn <player> [reason]` | Issue a warning to a staff member | `staffstatistic.admin` |
| `/staff rebuke <player> [reason]` | Issue a formal rebuke | `staffstatistic.admin` |
| `/staff stats <player>` | Display detailed staff statistics in chat | `staffstatistic.admin` |
| `/staff createnew <role> <priority>` | Register a new role directly into config | `staffstatistic.admin` |


