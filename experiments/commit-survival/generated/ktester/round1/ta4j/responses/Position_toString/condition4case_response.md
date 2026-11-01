{
  "group": "Position.toString()",
  "cases": [
    {
      "input": [],
      "expected": "Entry: null exit: null",
      "description": "Both entry and exit trades are null. The default string representation should show the literal \"null\" for each part."
    },
    {
      "input": [],
      "expected": "Entry: Trade[id=E1] exit: null",
      "description": "Only the entry trade is set (non‑null). The toString() should concatenate the entry's toString() value and the literal \"null\" for the exit."
    },
    {
      "input": [],
      "expected": "Entry: null exit: Trade[id=X1]",
      "description": "Only the exit trade is set (non‑null). The method should display \"null\" for the entry and the exit's toString() value."
    },
    {
      "input": [],
      "expected": "Entry: Trade[id=E2] exit: Trade[id=X2]",
      "description": "Both entry and exit trades are non‑null. The resulting string must contain the entry's toString() followed by a space, the word \"exit:\" and the exit's toString()."
    }
  ]
}