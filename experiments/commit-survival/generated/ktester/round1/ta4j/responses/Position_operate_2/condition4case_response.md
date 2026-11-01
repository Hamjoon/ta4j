{
  "testCases": [
    {
      "group": "operate - position is new (entry trade created)",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 5
            },
            {
              "parameter": "price",
              "value": "Num.valueOf(100.0)"
            },
            {
              "parameter": "amount",
              "value": "Num.valueOf(10.0)"
            }
          ],
          "expected": "Trade object with index=5, type=startingType, price=100.0, amount=10.0; Position.entry set to this trade; Position.exit remains null",
          "description": "When the Position is new (no entry trade yet), calling operate should create an entry Trade using the startingType and store it in the entry field."
        }
      ]
    },
    {
      "group": "operate - position is opened (exit trade created with valid index)",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 10
            },
            {
              "parameter": "price",
              "value": "Num.valueOf(105.0)"
            },
            {
              "parameter": "amount",
              "value": "Num.valueOf(5.0)"
            }
          ],
          "expected": "Trade object with index=10, type=startingType.complementType(), price=105.0, amount=5.0; Position.exit set to this trade; Position.entry unchanged",
          "description": "When the Position already has an entry trade (isOpened) and the supplied index is >= entry index, operate should create an exit Trade using the complement of the starting type."
        }
      ]
    },
    {
      "group": "operate - position is opened but index is less than entry index (exception)",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 3
            },
            {
              "parameter": "price",
              "value": "Num.valueOf(95.0)"
            },
            {
              "parameter": "amount",
              "value": "Num.valueOf(8.0)"
            }
          ],
          "expected": "IllegalStateException with message \"The index i is less than the entryTrade index\"",
          "description": "If the Position is opened and the supplied index is smaller than the entry trade's index, operate must throw an IllegalStateException."
        }
      ]
    },
    {
      "group": "operate - position is neither new nor opened (returns null)",
      "cases": [
        {
          "input": [
            {
              "parameter": "index",
              "value": 7
            },
            {
              "parameter": "price",
              "value": "Num.valueOf(110.0)"
            },
            {
              "parameter": "amount",
              "value": "Num.valueOf(12.0)"
            }
          ],
          "expected": "null (no trade created, Position.entry and Position.exit unchanged)",
          "description": "When the Position is closed (both entry and exit already set) or otherwise not new/opened, operate should return null and not modify the Position."
        }
      ]
    }
  ]
}