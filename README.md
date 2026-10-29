# Cobblemon Spectacle

Mod client Fabric (Minecraft 1.21.1, Cobblemon 1.8.1) qui met en scène les combats Pokémon : caméra de cinéma
pendant le combat, intros dessinées, Méga-Évolution, Retour primal, capacités Z et Téracristallisation en grand
spectacle.

Il marche seul, sur n'importe quel serveur Cobblemon (combats joueur contre joueur et contre dresseurs PNJ), et
avec [Phantasmon](../Phantasmon-Client), qui s'appuie sur lui pour ses propres combats.

## État

En cours d'extraction depuis Phantasmon : voir [`Documentation/plan-decoupage.md`](Documentation/plan-decoupage.md).

## Règles

- Aucune dépendance autre que Cobblemon (et Fabric API). Aucun backend.
- Toute évolution doit garder les combats Phantasmon intacts : Phantasmon n'utilise que l'API publique
  (paquet `api`), et chaque changement se valide aussi sur un combat Phantasmon.
