{
    "tests": [
        {
            "group": "ATRIndicator.getUnstableBars() - dynamic series growth",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "initialBarCount",
                            "value": 3
                        },
                        {
                            "parameter": "addedBars",
                            "value": 2
                        }
                    ],
                    "expected": 5,
                    "description": "Create an ATRIndicator with a BarSeries of 3 bars, then add 2 more bars to the series. getUnstableBars() should reflect the updated total (3 + 2 = 5)."
                },
                {
                    "input": [
                        {
                            "parameter": "initialBarCount",
                            "value": 0
                        },
                        {
                            "parameter": "addedBars",
                            "value": 7
                        }
                    ],
                    "expected": 7,
                    "description": "Start with an empty BarSeries, add 7 bars after the indicator is constructed. The method must return the current bar count (7)."
                }
            ]
        },
        {
            "group": "ATRIndicator.getUnstableBars() - series replacement",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "initialBarCount",
                            "value": 8
                        },
                        {
                            "parameter": "newBarCount",
                            "value": 4
                        }
                    ],
                    "expected": 4,
                    "description": "Construct the indicator with a series of 8 bars, then replace its underlying BarSeries with a new series containing 4 bars. getUnstableBars() should now return 4, proving that the method always queries the current series."
                }
            ]
        },
        {
            "group": "ATRIndicator.getUnstableBars() - null series handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "initialBarCount",
                            "value": null
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "Pass a null BarSeries to the ATRIndicator constructor. Subsequent call to getUnstableBars() should throw NullPointerException (or the constructor itself should)."
                }
            ]
        },
        {
            "group": "ATRIndicator.getUnstableBars() - mocked negative count after construction",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "initialBarCount",
                            "value": 5
                        },
                        {
                            "parameter": "mockedBarCount",
                            "value": -3
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "Create the indicator with a valid series (5 bars) then, using a mock or test double, force getBarCount() to return -3. getUnstableBars() must propagate an IllegalArgumentException, confirming defensive checks for corrupted state."
                }
            ]
        },
        {
            "group": "ATRIndicator.getUnstableBars() - maximum integer count",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "initialBarCount",
                            "value": 2147483647
                        }
                    ],
                    "expected": 2147483647,
                    "description": "Construct the indicator with a BarSeries whose size equals Integer.MAX_VALUE. getUnstableBars() should correctly return this maximum value without overflow."
                }
            ]
        }
    ]
}