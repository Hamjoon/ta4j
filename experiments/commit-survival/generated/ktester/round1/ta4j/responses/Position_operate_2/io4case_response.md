{
    "group": "operate - entry trade with negative index",
    "cases": [
        {
            "input": [
                { "parameter": "index", "value": -1 },
                { "parameter": "price", "value": "Num.valueOf(100.0)" },
                { "parameter": "amount", "value": "Num.valueOf(10.0)" }
            ],
            "expected": "Trade object with index=-1, type=startingType, price=100.0, amount=10.0; Position.entry set to this trade; Position.exit remains null",
            "description": "When a Position is new, operate should accept a negative index and still create the entry Trade (the library does not validate index sign)."
        }
    ]
}