{
    "testCases": [
        {
            "group": "Position#getGrossReturn() - closed position with null exit trade",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "A Position is created with an entry trade but never assigned an exit trade. isOpened() returns false, so getGrossReturn() attempts to call exit.getPricePerAsset() on a null exit, resulting in a NullPointerException."
                }
            ]
        },
        {
            "group": "Position#getGrossReturn() - closed position with null exit price",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "A Position is closed with an exit Trade whose pricePerAsset field is null. The method calls exit.getPricePerAsset() which returns null, and the subsequent call to getGrossReturn(null) throws a NullPointerException (or IllegalArgumentException depending on implementation). The test expects a NullPointerException."
                }
            ]
        },
        {
            "group": "Position#getGrossReturn() - closed position where getGrossReturn(Num) receives null argument",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalArgumentException",
                    "description": "A Position is closed and the exit trade's getPricePerAsset() method is stubbed to return null. The private overload getGrossReturn(Num finalPrice) validates its argument and throws IllegalArgumentException when finalPrice is null. The test verifies that this exception is thrown."
                }
            ]
        }
    ]
}