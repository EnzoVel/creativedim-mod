# Creative Dimension Mod

Mod NeoForge pour Minecraft **1.21.1** (NeoForge **21.1.221**)

---

## Fonctionnalités

| Fonctionnalité | Détail |
|---|---|
| 🌍 Nouvelle dimension | Monde superflat dédié (`creativedim:creative_world`) |
| 🎨 Mode créatif automatique | Le joueur passe en Créatif en entrant, revient en Survie en sortant |
| 🎒 Inventaires séparés | L'inventaire de la dimension créative est totalement indépendant de celui de l'Overworld/Nether/End |
| 💾 Persistance | Les inventaires sont sauvegardés via NeoForge AttachmentType (survit aux redémarrages serveur) |
| 👥 Multijoueur | Chaque joueur a ses propres données sauvegardées indépendamment |

---

## Utilisation

### 1. Obtenir l'Activateur de Portail
En mode créatif ou via commande :
```
/give @p creativedim:portal_activator
```

### 2. Voyager vers la Dimension Créative
- **Clic droit** avec l'Activateur de Portail en main → téléportation vers la dimension créative
- Le jeu passe automatiquement en **mode Créatif**
- L'inventaire de survie est sauvegardé

### 3. Revenir en Survie
- **Clic droit** à nouveau avec l'Activateur de Portail → retour en Overworld
- Le jeu repasse automatiquement en **mode Survie**
- L'inventaire de survie est restauré

---

## Architecture du projet

```
src/main/
├── java/com/creativedim/mod/
│   ├── CreativeDimMod.java            ← Point d'entrée principal
│   ├── dimension/
│   │   └── ModDimensions.java         ← Clé ResourceKey de la dimension
│   ├── event/
│   │   └── DimensionEventHandler.java ← Gestion du clic droit + logs
│   ├── inventory/
│   │   ├── InventoryManager.java      ← Sauvegarde/restauration inventaires
│   │   ├── PlayerInventoryData.java   ← AttachmentType NeoForge (données joueur)
│   │   └── SavedInventory.java        ← Snapshot inventaire + stats
│   ├── item/
│   │   ├── ModItems.java              ← Enregistrement des items
│   │   └── PortalActivatorItem.java   ← Item Activateur de Portail
│   └── network/
│       └── ModNetwork.java            ← Placeholder réseau
│
└── resources/
    ├── META-INF/
    │   └── neoforge.mods.toml         ← Métadonnées du mod
    ├── assets/creativedim/
    │   ├── lang/
    │   │   ├── en_us.json
    │   │   └── fr_fr.json
    │   ├── models/item/
    │   │   └── portal_activator.json
    │   └── textures/item/
    │       └── portal_activator.png
    └── data/creativedim/
        ├── dimension/
        │   └── creative_world.json    ← Définition de la dimension
        └── dimension_type/
            └── creative_world.json    ← Type de dimension (lumière, ciel, etc.)
```

---

## Ce qui est sauvegardé par inventaire

- **Tous les slots** : inventaire principal, armure, offhand
- **Santé** (health points)
- **Faim** (food level + saturation)
- **Expérience** (niveaux + barre d'XP)

> ⚠️ Les effets de potions ne sont **pas** sauvegardés/restaurés (comportement intentionnel).

---

## Compilation

### Prérequis
- Java 21 (JDK)
- Connexion Internet (pour télécharger NeoForge via Gradle)

### Commandes

```bash
# Générer le JAR (dans build/libs/)
./gradlew build

# Lancer en mode client (développement)
./gradlew runClient

# Lancer en mode serveur (développement)
./gradlew runServer
```

Le JAR compilé se trouve dans `build/libs/creativedim-1.0.0.jar`.

---

## Installation

1. Installer **Minecraft 1.21.1** avec **NeoForge 21.1.221**
2. Copier `creativedim-1.0.0.jar` dans le dossier `mods/` du profil
3. Lancer le jeu / serveur

---

## Notes techniques

### AttachmentType NeoForge
Les données sont stockées avec `copyOnDeath()`, ce qui signifie que les données survivent à la mort du joueur. Le mode de jeu est toujours correctement réinitialisé à la téléportation et au respawn.

### Dimension superflat
La génération est définie dans `data/creativedim/dimension/creative_world.json` :
- 1 couche de Bedrock
- 2 couches de Dirt
- 1 couche de Grass Block
- Biome : Plains
- Pas de structures, pas de features de génération

### Temps fixe
Le soleil est fixé à la mi-journée (`fixed_time: 6000`) pour un éclairage permanent optimal en mode créatif.

---

## Licence

MIT — Libre d'utilisation et de modification.
