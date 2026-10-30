"""Writes the trainers' default animations (plan-spectacle.md, phase E1) as playerAnimator / Emotecraft JSON.

Each animation is a list of poses (tick, easing, {part: {axis: value}}); a part left out of a pose holds its previous
value. Radians; an arm or leg with a negative pitch swings forward, a positive head pitch looks down, z negative is in
front. "torso" moves the WHOLE player (Adrien's test 2026-10-30): a positive pitch tilts it backwards, a positive y
lifts it off the ground — so it only leans a little forward (negative pitch) and never moves up. The head's rotation
replaces the player's own look: yaw 0 keeps the eyes on the opponent the body is turned to.

    python tools/trainer_animations.py

writes src/main/resources/assets/cobblemon_spectacle/player_animations/*.json. Finer animations made in Blockbench
(Emotecraft plugin) can replace these files: same names.
"""
import json
import os

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "cobblemon_spectacle",
                   "player_animations")

DEFAULTS = {
    "head": {"x": 0, "y": 0, "z": 0, "pitch": 0, "yaw": 0, "roll": 0},
    "torso": {"x": 0, "y": 0, "z": 0, "pitch": 0, "yaw": 0, "roll": 0},
    "rightArm": {"x": -5, "y": 2, "z": 0, "pitch": 0, "yaw": 0, "roll": 0, "bend": 0},
    "leftArm": {"x": 5, "y": 2, "z": 0, "pitch": 0, "yaw": 0, "roll": 0, "bend": 0},
    "rightLeg": {"x": -1.9, "y": 12, "z": 0.1, "pitch": 0, "yaw": 0, "roll": 0, "bend": 0},
    "leftLeg": {"x": 1.9, "y": 12, "z": -0.1, "pitch": 0, "yaw": 0, "roll": 0, "bend": 0},
}

# Elbows and knees bend (bendy-lib, shipped with Emotecraft). Signs checked in game by Adrien 2026-10-30.
ELBOW = -1
KNEE = 1

# The battle guard (Battle Revolution, Adrien's references and feedback 2026-10-30): right foot a little forward, left a
# little back — legs stay under the hips —, knees softly bent, elbows bent with loose fists at chest height apart from
# each other (not a sword grip). Every axis the moments change is set here, so coming back to it resets them.
GUARD = {
    "torso": {"pitch": -0.04, "yaw": 0, "roll": 0},
    "head": {"pitch": 0.04, "yaw": 0, "roll": 0},
    "rightLeg": {"x": -2.0, "z": 0.1, "pitch": -0.2, "bend": KNEE * 0.3},
    "leftLeg": {"x": 2.0, "z": -0.1, "pitch": 0.2, "bend": KNEE * 0.18},
    "rightArm": {"pitch": -0.5, "yaw": 0, "roll": 0.12, "bend": ELBOW * 0.95},
    "leftArm": {"pitch": -0.38, "yaw": 0, "roll": -0.12, "bend": ELBOW * 1.1},
}

# Its breathing and weight shifts (3 s loop): knees sink a little, the weight goes from one foot to the other, the
# head glances.
GUARD_SINK = {
    "torso": {"pitch": -0.07, "roll": 0.03},
    "head": {"pitch": 0.07, "yaw": 0.06, "roll": -0.03},
    "rightLeg": {"bend": KNEE * 0.42},
    "leftLeg": {"bend": KNEE * 0.3},
    "rightArm": {"pitch": -0.56, "bend": ELBOW * 1.05},
    "leftArm": {"pitch": -0.44, "bend": ELBOW * 1.2},
}
GUARD_SHIFT = {
    "torso": {"pitch": -0.05, "roll": -0.03},
    "head": {"pitch": 0.05, "yaw": -0.05, "roll": 0.03},
    "rightLeg": {"bend": KNEE * 0.26},
    "leftLeg": {"bend": KNEE * 0.24},
    "rightArm": {"pitch": -0.48, "bend": ELBOW * 0.9},
    "leftArm": {"pitch": -0.4, "bend": ELBOW * 1.05},
}


def merge(base, change):
    pose = {part: dict(axes) for part, axes in base.items()}
    for part, axes in change.items():
        pose.setdefault(part, {}).update(axes)
    return pose


def animation(name, description, poses, loop=False):
    """poses: [(tick, easing, pose)], each pose complete or partial (merged over the previous one)."""
    moves = []
    current = {}
    for tick, easing, pose in poses:
        current = merge(current, pose)
        for part, axes in current.items():
            values = {axis: axes.get(axis, DEFAULTS[part][axis]) for axis in DEFAULTS[part]}
            moves.append({"tick": tick, "easing": easing, "turn": 0, part: values})
    end = poses[-1][0]
    return {
        "version": 1,
        "name": name,
        "author": "Cobblemon Spectacle",
        "description": description,
        "emote": {
            "isLoop": "true" if loop else "false",
            "returnTick": 1 if loop else 0,
            "beginTick": 0,
            "endTick": end,
            "stopTick": end + 1,
            "degrees": False,
            "moves": moves,
        },
    }


def neutral():
    return {part: dict(axes) for part, axes in DEFAULTS.items()}


ANIMATIONS = [
    animation("guard_ready", "Battle guard, loops between the battle's moments: breathing and weight shifts.", [
        (0, "INOUTSINE", merge(neutral(), GUARD)),
        (15, "INOUTSINE", GUARD_SINK),
        (30, "INOUTSINE", GUARD),
        (45, "INOUTSINE", GUARD_SHIFT),
        (60, "INOUTSINE", GUARD),
    ], loop=True),

    # The head turns against the torso: the eyes stay on the field.
    animation("throw_overhand", "Sends a Pokémon out: the arm swings back, a step in, overhand throw, follow-through.", [
        (0, "INOUTSINE", merge(neutral(), GUARD)),
        (7, "OUTQUAD", {"torso": {"pitch": 0.02, "yaw": 0.35}, "head": {"pitch": -0.02, "yaw": -0.35},
                        "rightArm": {"pitch": 0.8, "roll": 0.1, "bend": ELBOW * 0.6},
                        "leftArm": {"pitch": -1.1, "roll": -0.05, "bend": ELBOW * 0.4},
                        "rightLeg": {"pitch": 0.12, "bend": KNEE * 0.35}, "leftLeg": {"pitch": -0.25, "bend": KNEE * 0.2}}),
        (12, "INQUAD", {"torso": {"pitch": -0.1, "yaw": -0.25}, "head": {"pitch": 0.1, "yaw": 0.25},
                        "rightArm": {"pitch": -2.3, "roll": 0, "bend": ELBOW * 0.1},
                        "leftArm": {"pitch": -0.3, "bend": ELBOW * 0.7},
                        "rightLeg": {"pitch": -0.1, "bend": KNEE * 0.2}, "leftLeg": {"pitch": 0.15, "bend": KNEE * 0.3}}),
        (16, "OUTQUAD", {"torso": {"pitch": -0.14, "yaw": -0.35}, "head": {"pitch": 0.14, "yaw": 0.35},
                         "rightArm": {"pitch": -1.0, "yaw": -0.3, "bend": ELBOW * 0.25}}),
        (28, "INOUTSINE", GUARD),
    ]),

    animation("recall_reach", "Calls a Pokémon back: the ball held out at arm's length, then drawn back to the chest.", [
        (0, "INOUTSINE", merge(neutral(), GUARD)),
        (5, "OUTQUAD", {"torso": {"yaw": -0.1}, "head": {"yaw": 0.1},
                        "rightArm": {"pitch": -1.45, "yaw": 0.1, "roll": 0.05, "bend": ELBOW * 0.05}}),
        (14, "INOUTSINE", {"rightArm": {"pitch": -1.4}}),
        (19, "INQUAD", {"torso": {"yaw": 0}, "head": {"yaw": 0},
                        "rightArm": {"pitch": -0.7, "yaw": 0, "bend": ELBOW * 1.2}}),
        (26, "INOUTSINE", GUARD),
    ]),

    animation("command_point", "Orders a move: the arm cocks, then snaps out pointing at the opponent.", [
        (0, "INOUTSINE", merge(neutral(), GUARD)),
        (3, "OUTQUAD", {"torso": {"yaw": 0.15}, "head": {"yaw": -0.15},
                        "rightArm": {"pitch": -0.3, "bend": ELBOW * 1.3}}),
        (6, "OUTBACK", {"torso": {"pitch": -0.08, "yaw": -0.25}, "head": {"pitch": 0.08, "yaw": 0.25},
                        "rightArm": {"pitch": -1.6, "yaw": 0.12, "roll": 0.05, "bend": 0},
                        "leftArm": {"pitch": -0.2, "roll": -0.1, "bend": ELBOW * 1.0},
                        "rightLeg": {"pitch": -0.3, "bend": KNEE * 0.35}, "leftLeg": {"pitch": 0.25}}),
        (17, "INOUTSINE", {"rightArm": {"pitch": -1.55}}),
        (24, "INOUTSINE", GUARD),
    ]),

    animation("victory_fists", "Wins: both fists thrown up, pumped twice.", [
        (0, "INOUTSINE", merge(neutral(), GUARD)),
        (5, "OUTBACK", {"torso": {"pitch": 0.04}, "head": {"pitch": -0.3},
                        "rightArm": {"pitch": -2.95, "yaw": 0, "roll": -0.25},
                        "leftArm": {"pitch": -2.95, "yaw": 0, "roll": 0.25},
                        "rightLeg": {"y": 12, "z": 0.1, "pitch": 0}, "leftLeg": {"y": 12, "z": -0.1, "pitch": 0}}),
        (9, "INOUTSINE", {"rightArm": {"pitch": -2.55}, "leftArm": {"pitch": -2.55}}),
        (13, "OUTBACK", {"rightArm": {"pitch": -3.05}, "leftArm": {"pitch": -3.05}}),
        (17, "INOUTSINE", {"rightArm": {"pitch": -2.65}, "leftArm": {"pitch": -2.65}}),
        (32, "INOUTSINE", {"rightArm": {"pitch": -2.7}, "leftArm": {"pitch": -2.7}}),
        (40, "INOUTSINE", neutral()),
    ]),

    animation("defeat_slump", "Loses: head down, shoulders slumped.", [
        (0, "INOUTSINE", merge(neutral(), GUARD)),
        (10, "OUTQUAD", {"torso": {"pitch": -0.12}, "head": {"pitch": 0.6},
                         "rightArm": {"pitch": -0.2, "yaw": 0, "roll": 0.08},
                         "leftArm": {"pitch": -0.2, "yaw": 0, "roll": -0.08},
                         "rightLeg": {"y": 12, "z": -0.6, "pitch": -0.05}, "leftLeg": {"y": 12, "z": 0.6, "pitch": 0.05}}),
        (44, "INOUTSINE", {"head": {"pitch": 0.65}}),
        (54, "INOUTSINE", neutral()),
    ]),
]

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    for anim in ANIMATIONS:
        with open(os.path.join(OUT, anim["name"] + ".json"), "w", encoding="utf-8") as file:
            json.dump(anim, file, ensure_ascii=False, indent="\t")
            file.write("\n")
        print(anim["name"], anim["emote"]["endTick"], "ticks")
