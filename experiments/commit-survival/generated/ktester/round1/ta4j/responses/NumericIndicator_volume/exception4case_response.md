{
    "test_cases": [
        {
            "group": "NumericIndicator.volume(BarSeries) – null bar within series",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "bs",
                            "value": "BarSeries with 2 bars, first bar valid, second bar is null"
                        }
                    ],
                    "expected": "NullPointerException when calling getValue(1) on the returned NumericIndicator",
                    "description": "The VolumeIndicator iterates over the BarSeries to fetch volume values. If a bar object is null, accessing its volume should raise a NullPointerException. This test verifies that the exception propagates through the NumericIndicator wrapper."
                }
            ]
        },
        {
            "group": "NumericIndicator.volume(BarSeries) – bar with null volume",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "bs",
                            "value": "BarSeries with 1 bar whose getVolume() returns null"
                        }
                    ],
                    "expected": "NullPointerException when calling getValue(0) on the returned NumericIndicator",
                    "description": "A VolumeIndicator expects each bar to provide a non‑null Num for volume. Supplying a bar with a null volume should cause a NullPointerException when the indicator tries to wrap the value."
                }
            ]
        },
        {
            "group": "NumericIndicator.volume(BarSeries) – delegate null via of()",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "bs",
                            "value": "BarSeries mock that returns null from new VolumeIndicator(bs) (simulated by a subclass whose constructor returns null)"
                        }
                    ],
                    "expected": "IllegalArgumentException (or NullPointerException) thrown by NumericIndicator.of when delegate is null",
                    "description": "If, for any reason, the VolumeIndicator constructor yields a null delegate, the static factory method NumericIndicator.of should reject it and throw an exception. This test forces that situation to confirm defensive programming."
                }
            ]
        },
        {
            "group": "NumericIndicator.volume(BarSeries) – negative bar count (invalid series)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "bs",
                            "value": "BarSeries implementation whose getBarCount() returns -1"
                        }
                    ],
                    "expected": "IllegalArgumentException when constructing VolumeIndicator or when calling getValue",
                    "description": "A well‑formed BarSeries must have a non‑negative bar count. Supplying a series reporting a negative count should cause the VolumeIndicator (or the NumericIndicator wrapper) to reject the input with an IllegalArgumentException."
                }
            ]
        }
    ]
}