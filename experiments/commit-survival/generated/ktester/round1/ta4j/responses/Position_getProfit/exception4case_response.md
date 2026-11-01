{
    "test_cases": [
        {
            "group": "Position.getProfit() - NullPointerException when exit trade is null but position reported as closed",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "Create a Position, manually set its internal 'exit' field to null while mocking/spying isOpened() to return false. Calling getProfit() will attempt exit.getPricePerAsset() and throw NullPointerException."
                }
            ]
        },
        {
            "group": "Position.getProfit() - NullPointerException when exit price per asset is null",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "Create a closed Position where the exit Trade object exists but its pricePerAsset is null. getProfit() calls getGrossProfit(null) which internally dereferences the price and throws NullPointerException."
                }
            ]
        },
        {
            "group": "Position.getProfit() - IllegalArgumentException for negative exit price",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalArgumentException",
                    "description": "Create a closed Position with a valid exit Trade whose pricePerAsset is a negative Num. The underlying getGrossProfit(finalPrice) validates that price must be non‑negative and throws IllegalArgumentException. getProfit() propagates this exception."
                }
            ]
        },
        {
            "group": "Position.getProfit() - NullPointerException when cost model is null",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "Create a closed Position with a non‑null exit trade, but set its internal CostModel (used by getPositionCost()) to null via reflection or a test subclass. When getProfit() calls getPositionCost(), a NullPointerException is thrown."
                }
            ]
        },
        {
            "group": "Position.getProfit() - RuntimeException from zero() when position is opened",
            "cases": [
                {
                    "input": [],
                    "expected": "RuntimeException",
                    "description": "Spy on an opened Position and make its private zero() method throw a RuntimeException (e.g., new RuntimeException(\"zero failed\")). Calling getProfit() will take the opened branch and propagate the exception."
                }
            ]
        }
    ]
}