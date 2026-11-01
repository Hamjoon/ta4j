{
    "test_cases": [
        {
            "group": "closedPositionWithNullExitThrowsNPE",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "A Position that is closed (isOpened() == false) but whose internal 'exit' trade reference is null. Calling getGrossProfit() should attempt to access exit.getPricePerAsset() and throw a NullPointerException."
                }
            ]
        },
        {
            "group": "closedPositionWithExitPriceNullThrowsNPE",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "A Position that is closed with a non‑null exit trade, however the exit trade's price per asset is null. When getGrossProfit() invokes getGrossProfit(exit.getPricePerAsset()), the null argument propagates and results in a NullPointerException inside the overloaded method."
                }
            ]
        }
    ]
}