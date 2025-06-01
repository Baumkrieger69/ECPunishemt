# EcPunishment - Advanced Minecraft Punishment System

Ein umfassendes Bestrafungssystem für Minecraft-Server mit erweiterten Funktionen für Moderatoren und Administratoren.

## Features

### 🛡️ Bestrafungssystem
- **Warn** - Warnung an Spieler senden
- **Mute** - Spieler stumm schalten (temporär/permanent)
- **Ban** - Spieler bannen (temporär/permanent)
- **Kick** - Spieler vom Server kicken
- **Jail** - Spieler ins Gefängnis teleportieren
- **Freeze** - Spieler einfrieren (Bewegung blockieren)
- **Softban** - Spieler kann nicht interagieren
- **Shadowmute** - Spieler denkt er kann schreiben, aber niemand sieht es

### 🎮 GUI-System
- Benutzerfreundliches Inventar-basiertes GUI
- Bestrafungsauswahl mit vorgefertigten Gründen
- Zeitauswahl für temporäre Bestrafungen
- Bestrafungshistorie direkt im GUI

### 📊 Verwaltung & Statistiken
- **History** - Bestrafungshistorie von Spielern
- **Modlog** - Detaillierte Staff-Logs anzeigen
- **Modstats** - Moderator-Statistiken und Server-Übersicht
- Automatische Eskalation basierend auf Bestrafungsanzahl

### 👮 Staff-Modus
- Vollständiger Staff-Modus mit speziellen Items
- Unsichtbarkeit (Vanish) für Staff-Mitglieder
- Spieler-Beobachtung (Watch-System)
- Zufällige Teleportation zu Spielern
- Schnelle Bestrafungs-Tools

### 🗄️ Datenbank & Konfiguration
- SQLite-Datenbank für alle Bestrafungen
- Konfigurierbare Nachrichten und Gründe
- Eskalationssystem mit anpassbaren Stufen
- Automatische Offline-Bestrafungsausführung

## Installation

1. Lade die `EcPunishment.jar` in den `plugins/` Ordner deines Servers
2. Starte den Server neu
3. Konfiguriere das Plugin nach deinen Wünschen
4. Setze den Jail-Standort mit `/setjail`

## Commands

### Bestrafungsbefehle
```
/punish <Spieler>              - Öffnet Bestrafungs-GUI
/warn <Spieler> [Grund]        - Warnt einen Spieler
/mute <Spieler> [Zeit] [Grund] - Macht einen Spieler stumm
/ban <Spieler> [Zeit] [Grund]  - Bannt einen Spieler
/kick <Spieler> [Grund]        - Kickt einen Spieler
/jail <Spieler> [Zeit]         - Sperrt einen Spieler ins Gefängnis
/freeze <Spieler>              - Friert einen Spieler ein
/softban <Spieler>             - Softbant einen Spieler
```

### Aufhebungsbefehle
```
/unjail <Spieler>    - Entlässt einen Spieler aus dem Gefängnis
/unfreeze <Spieler>  - Taut einen Spieler auf
```

### Informationsbefehle
```
/history <Spieler>     - Zeigt Bestrafungshistorie
/modlog <Spieler>      - Zeigt Staff-Logs
/modstats [Spieler]    - Zeigt Moderator-Statistiken
```

### Admin-Befehle
```
/staffmode           - Aktiviert/Deaktiviert Staff-Modus
/setjail             - Setzt Gefängnis-Standort
/ecpunishment reload - Lädt Plugin-Konfiguration neu
```

## Zeitformate

Das Plugin unterstützt verschiedene Zeitformate:
- `5m` = 5 Minuten
- `1h` = 1 Stunde
- `2d` = 2 Tage
- `1w` = 1 Woche
- `perm` oder `permanent` = Permanent

Kombinationen sind möglich: `1d12h30m` = 1 Tag, 12 Stunden, 30 Minuten

## Permissions

### Basis-Permissions
```yaml
ecpunishment.*          # Vollzugriff auf alle Funktionen
ecpunishment.punish     # Zugriff auf Bestrafungs-GUI
ecpunishment.warn       # Spieler warnen
ecpunishment.mute       # Spieler stumm schalten
ecpunishment.ban        # Spieler bannen
ecpunishment.kick       # Spieler kicken
ecpunishment.jail       # Spieler ins Gefängnis sperren
ecpunishment.freeze     # Spieler einfrieren
ecpunishment.softban    # Spieler softbannen
```

### Verwaltungs-Permissions
```yaml
ecpunishment.history    # Bestrafungshistorie anzeigen
ecpunishment.modlog     # Staff-Logs anzeigen
ecpunishment.modstats   # Moderator-Statistiken anzeigen
ecpunishment.staffmode  # Staff-Modus verwenden
ecpunishment.setjail    # Gefängnis-Standort setzen
ecpunishment.unjail     # Spieler aus Gefängnis entlassen
ecpunishment.unfreeze   # Spieler auftauen
ecpunishment.reload     # Plugin neu laden
ecpunishment.bypass     # Alle Bestrafungen umgehen
```

## Staff-Modus Items

Im Staff-Modus erhältst du folgende Items:

1. **Kompass** - Zufälliger Teleport zu Online-Spielern
2. **Stock** - Freeze/Unfreeze Tool (Rechtsklick auf Spieler)
3. **Buch** - Spieler-Informationen anzeigen
4. **Papier** - Bestrafungs-GUI öffnen
5. **Enderauge** - Vanish-Modus umschalten
6. **Uhr** - Spieler beobachten (unsichtbar folgen)
7. **Barriere** - Staff-Modus verlassen

## Konfiguration

Das Plugin erstellt mehrere Konfigurationsdateien:

- `config.yml` - Hauptkonfiguration
- `messages.yml` - Alle Nachrichten und Texte
- `reasons.yml` - Vorgefertigte Bestrafungsgründe
- `gui.yml` - GUI-Layout und Items
- `escalation.yml` - Eskalationssystem-Einstellungen
- `jail.yml` - Gefängnis-Standort (automatisch erstellt)

## Datenbank

Das Plugin verwendet SQLite und erstellt automatisch folgende Tabellen:

- `punishments` - Alle Bestrafungen
- `staff_actions` - Staff-Aktionen für Logs
- `player_data` - Zusätzliche Spielerdaten

## Eskalationssystem

Das automatische Eskalationssystem kann so konfiguriert werden:

```yaml
escalation:
  enabled: true
  levels:
    1:
      punishments: 3
      action: "mute"
      duration: "1h"
    2:
      punishments: 5
      action: "ban"
      duration: "1d"
```

## Support

Bei Problemen oder Fragen:
1. Überprüfe die Server-Logs auf Fehlermeldungen
2. Stelle sicher, dass alle Permissions korrekt gesetzt sind
3. Verwende `/ecpunishment reload` nach Konfigurationsänderungen

## Entwicklung

### Projektstruktur
```
src/main/java/de/ecpunishment/
├── EcPunishment.java          # Hauptklasse
├── commands/                  # Alle Befehle
├── configs/                   # Konfigurationsmanager
├── data/                      # Datenbank und Bestrafungslogik
├── gui/                       # GUI-System
├── listeners/                 # Event-Listener
└── utils/                     # Hilfsfunktionen
```

### Abhängigkeiten
- Spigot/Paper API 1.16+
- Java 8+
- SQLite (eingebaut)

## Lizenz

Dieses Plugin ist für private und kommerzielle Server-Nutzung freigegeben.

---

**Version:** 1.0.0  
**Entwickelt für:** Spigot/Paper 1.16+  
**Autor:** Developer
