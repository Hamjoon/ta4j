{
    "group": "isNew",
    "cases": [
        {
            "input": [],
            "expected": "true",
            "description": "A Position that was previously opened and closed, then both its entry and exit trades are explicitly set back to null (e.g., via reflection). After the reset, isNew() should again return true."
        },
        {
            "input": [],
            "expected": "false",
            "description": "A Position where entry and exit reference the same non‑null Trade instance (both fields point to the identical object). Even though they are the same object, both are non‑null, so isNew() must return false."
        },
        {
            "input": [],
            "expected": "false",
            "description": "A Position with a non‑null entry Trade whose internal fields (price, amount, etc.) are null or NaN, while exit remains null. The method only checks reference nullness, therefore isNew() should return false."
        },
        {
            "input": [],
            "expected": "true",
            "description": "A freshly deserialized Position (using Java serialization) that had no entry or exit trades before serialization. After deserialization the fields are still null, so isNew() must return true."
        }
    ]
}