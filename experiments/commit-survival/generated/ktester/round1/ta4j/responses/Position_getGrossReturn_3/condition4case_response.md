{
    "group": "Position.getGrossReturn(Num) – delegation and edge‑cases",
    "cases": [
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(110)"
                }
            ],
            "expected": "returns the same Num as Position.getGrossReturn(entryPrice=Num.valueOf(100), finalPrice=Num.valueOf(110))",
            "description": "Happy‑path: entry trade price is 100, final price is 110. The method should simply delegate to the two‑argument overload and return its result."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(95)"
                }
            ],
            "expected": "returns the same Num as Position.getGrossReturn(entryPrice=Num.valueOf(100), finalPrice=Num.valueOf(95))",
            "description": "Entry price higher than final price (loss scenario). The method must still delegate correctly and return whatever the overload computes."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "NaN"
                }
            ],
            "expected": "NaN",
            "description": "Final price is NaN. According to the library contract, any calculation involving NaN should propagate NaN, so the returned value must be NaN."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException",
            "description": "Passing a null Num should cause a NullPointerException because the method immediately forwards the argument to the overload."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(120)"
                }
            ],
            "expected": "NullPointerException",
            "description": "The entry trade is null (Position.getEntry() returns null). The method tries to call getPricePerAsset() on a null reference and must throw NullPointerException."
        },
        {
            "input": [
                {
                    "parameter": "finalPrice",
                    "value": "Num.valueOf(130)"
                }
            ],
            "expected": "NaN",
            "description": "Entry trade price is NaN. Even with a valid final price, the calculation should propagate NaN and the method must return NaN."
        }
    ]
}