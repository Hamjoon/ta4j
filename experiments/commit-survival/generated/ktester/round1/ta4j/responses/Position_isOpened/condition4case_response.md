{
  "group": "Position.isOpened()",
  "cases": [
    {
      "input": [
        {
          "parameter": "entry",
          "value": null
        },
        {
          "parameter": "exit",
          "value": null
        }
      ],
      "expected": false,
      "description": "Both entry and exit are null → position is not opened."
    },
    {
      "input": [
        {
          "parameter": "entry",
          "value": "TradeMock(entry)"
        },
        {
          "parameter": "exit",
          "value": null
        }
      ],
      "expected": true,
      "description": "Entry trade is set, exit is still null → position is opened."
    },
    {
      "input": [
        {
          "parameter": "entry",
          "value": null
        },
        {
          "parameter": "exit",
          "value": "TradeMock(exit)"
        }
      ],
      "expected": false,
      "description": "Entry is null but exit is non‑null (illegal state) → position is not opened."
    },
    {
      "input": [
        {
          "parameter": "entry",
          "value": "TradeMock(entry)"
        },
        {
          "parameter": "exit",
          "value": "TradeMock(exit)"
        }
      ],
      "expected": false,
      "description": "Both entry and exit are non‑null (position already closed) → position is not opened."
    }
  ]
}