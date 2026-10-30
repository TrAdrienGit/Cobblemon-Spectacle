# Plan — Spectacle, deuxième vague (emotes, ambiances, attaques)

> Demandé par Adrien le 2026-10-30. Spectacle reste autonome : rien ne passe par le backend, tout est joué localement
> par chaque client à partir des événements du combat qu'il reçoit déjà (combats Cobblemon classiques comme combats
> Ghost pilotés par Phantasmon).

## Choix d'Adrien (2026-10-30)

- **Ambiance** : chaque joueur choisit la sienne (réglage local de Spectacle). Surcouches :
  - Phantasmon : dans le lobby, une ambiance commune choisie pour les deux joueurs (et ses spectateurs) ;
  - Phantasmon Events, plus tard : ambiances imposées pour des matchs scénarisés (lore).
  Spectacle expose donc une API « ambiance imposée pour ce combat » qui l'emporte sur le réglage local.
- **Emotes** : Emotecraft (déjà dans le modpack, avec sa bibliothèque d'animation de joueur) en dépendance
  **facultative** : présent, les dresseurs s'animent ; absent, Spectacle marche sans emotes.
- **Attaques** : **tout refaire** pour le top Smogon, même les attaques que Cobblemon anime déjà, dans un style
  unique et fidèle aux jeux (recréation en particules, caméra et mouvements ; aucun asset extrait des jeux).

## Ordre

| Phase | Contenu | Statut |
|---|---|---|
| E1 | Emotes : moteur (playerAnimator, facultatif), garde d'attente et moments joués automatiquement (lancer, rappel — un échange = rappel puis lancer —, attaque, victoire, défaite), une animation par moment, `/spectacle preview emote <moment>` ; dresseurs tournés vers l'adversaire | validé 2026-10-30 |
| E2 | Profil d'animation : variantes `<moment>_<style>` (3 gardes, 3 lancers, 2 rappels, 3 attaques, 3 victoires, 2 défaites ; un pack de ressources peut en ajouter), écran « Profil d'animation du dresseur » (réglages de Spectacle) avec aperçu en boucle sur son propre modèle, choix enregistré dans `config/cobblemon_spectacle.json`, `SpectacleHooks.setTrainerProfiles` pour les autres dresseurs | validé 2026-10-30 |
| E3 | Phantasmon : profil envoyé au backend à la connexion et à chaque changement (`POST /trainer-profile`), ceux des autres dresseurs récupérés au début du combat et à la demande, gardés une minute (`GET /trainer-profiles`, table `trainer_profiles`, V20) ; menu de Spectacle sur sa seule touche (N) | validé 2026-10-30 |
| A1 | Ambiances : système (lumière, teinte du ciel, particules, effets aux moments forts, musique facultative), réglage local, API d'ambiance imposée | à faire |
| A2 | Ambiances de départ : disco, nature, nuit (puis d'autres, façon stades de Battle Revolution) | à faire |
| A3 | Phantasmon : choix d'une ambiance commune dans le lobby | à faire |
| M1 | Attaques : top d'usage Smogon (National Dex et OU, Gen 9), inventaire de l'existant Cobblemon (136 animations), vérifier si les effets serveur se remplacent côté client ou seulement se complètent | à faire |
| M2+ | Attaques refaites une par une, par lots, dans l'ordre d'usage | à faire |

## E1 — comment c'est fait

- `emote/TrainerEmotes` : la file des moments de chaque dresseur, la garde en boucle entre eux ; `PlayerAnimatorEmotes`
  (chargé seulement si playerAnimator est là) joue les animations sur une couche au-dessus de celle d'Emotecraft.
- `classic/ClassicEmotes` : nourri par les gestionnaires de paquets de Cobblemon, combats Spectacle **et** combats Ghost
  de Phantasmon (mêmes gestionnaires) ; combats sauvages exclus ; seuls les dresseurs joueurs (et avatars) s'animent.
- Animations : `assets/cobblemon_spectacle/player_animations/*.json`, écrites par `tools/trainer_animations.py`
  (poses en radians). Références : vidéos de Pokémon Battle Revolution et Colosseum envoyées par Adrien (garde des
  dresseurs, ordre d'attaque bras tendu, lancers par-dessus l'épaule). `torso` déplace tout le joueur (pitch positif =
  en arrière, y positif = en l'air) ; coudes et genoux par `bend` (signes vérifiés en jeu).
- Lus **en tête** des gestionnaires de début de combat et de messages, priorité **500** : sur le serveur Cobblemon
  Academy, DeltaClient annule ces gestionnaires pour son propre écran de combat, et les injections en tête passent par
  ordre de priorité croissant (à 1500, après DeltaClient, l'attaque et la victoire n'étaient jamais vues).
- Le message « X utilise Y » d'un combat entre joueurs nomme « Pokémon de Dresseur » (`owned_pokemon`) : le dresseur
  est retrouvé par le nom du propriétaire.
- Intro : les deux dresseurs sont dessinés quelle que soit la distance (`EntityRendererCinematicMixin`), la caméra
  reculant au-delà de la distance d'affichage des entités.
- `LivingEntityRendererFacingMixin` : pendant le combat, chaque dresseur est dessiné tourné vers son adversaire.

## Points à vérifier

- Emotecraft / bibliothèque d'animation : jouer une animation sur un **autre** joueur (et sur un avatar de Hub)
  côté client, sans passer par le serveur.
- Effets d'attaque Cobblemon : les « action effects » sont des données serveur ; sur un serveur multijoueur, les
  nôtres ne peuvent que s'y ajouter si le client ne peut pas les couper.
- Shaders (Iris) : les ambiances doivent rester correctes avec et sans shaders.
