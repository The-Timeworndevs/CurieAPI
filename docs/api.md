# Configuring
API by default reads **all files** contained within `config/curie` directory, assuming they're json.
Therefore, a basic setup for the API would be creating said directory, with a file ex. `config.json`

```json
{}
```

## Possible configurations:

```json
{
  "cap": 10000,
  "div_constant": 4,
  "blocks": {
    "minecraft:glowstone": {
      "alpha": 4,
      "beta": 3,
      "gamma": 2
    }
  },
  "items": {
    "minecraft:glowstone_dust": {
      "alpha": 3,
      "gamma": 1
    }
  },
  "biomes": {
    "minecraft:crimson_forest": {
      "beta": 2
    }
  },
  "armor": [
    {
      "armor": {
        "minecraft:leather_cap": 0.17,
        "minecraft:leather_tunic": 0.42,
        "minecraft:leather_pants": 0.28,
        "minecraft:leather_boots": 0.13
      },
      "radiation": {
        "alpha": 0.10,
        "beta": 0.05
      }
    },
  ],
  "insulators": {
    "createnuclear:reactor_casing": {
      "alpha": 4,
      "beta": 3,
      "gamma": 2
    },
  }
}
```

## Logics:
### Cap:
Cap is the *maximum* intake of each radiation kind.
Mods that handle effects and mutations, such as [CurieAPI-SimpleAdditionales](https://github.com/The-TimewornDevs/CurieAPI-SimpleAdditionales), use percentage values ((radiation_value)/(cap)), for flexibility.

### Div Constant:
It is the divisor of radiation intake, it is modifiable so that the [user] can modify how impactful armors are (in isolating radiation, of course).

### Items:
HashMap of objects, containing values of radiation (incl. types) dealt by a single (1) item contained in player's inventory. Scales with the item's amount.

### Biomes:
HashMap of biomes, containing objects similar to these of [Items](###Items).

### Armor:
List of objects, containing specification of both radiation and the multiplier of each radiation part. Basically for each armor part the radiation values are multiplied by the part's efficiency.

### Insulators:
HashMap of blocks, similar to this of [Items](###Items), this time the values being amounts by how much the radiation intake (from blocks) is reduced, if the insulator is in path between player and the block.

