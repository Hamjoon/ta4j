{
    "group": "Position.toString() - entry empty string",
    "cases": [
        {
            "input": [],
            "expected": "Entry:  exit: Trade[id=X3]",
            "description": "Entry trade's toString() returns an empty string while exit trade is a normal non‑null Trade. The method should concatenate the empty entry string with the literal \"exit:\" and the exit's toString() value."
        }
    ]
}