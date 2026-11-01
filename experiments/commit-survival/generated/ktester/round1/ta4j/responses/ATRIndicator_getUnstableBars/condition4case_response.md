{
    "group": "ATRIndicator.getUnstableBars()",
    "cases": [
        {
            "input": [
                {
                    "parameter": "initialBarCount",
                    "value": 0
                }
            ],
            "expected": 0,
            "description": "When the ATRIndicator is constructed with an empty BarSeries (0 bars), getUnstableBars() should return 0, indicating no unstable bars."
        },
        {
            "input": [
                {
                    "parameter": "initialBarCount",
                    "value": 5
                }
            ],
            "expected": 5,
            "description": "When the ATRIndicator is constructed with a BarSeries containing 5 bars, getUnstableBars() should return 5, matching the bar count."
        },
        {
            "input": [
                {
                    "parameter": "initialBarCount",
                    "value": 42
                }
            ],
            "expected": 42,
            "description": "When the ATRIndicator is constructed with a BarSeries containing 42 bars, getUnstableBars() should return 42, verifying correct propagation of the bar count."
        },
        {
            "input": [
                {
                    "parameter": "initialBarCount",
                    "value": 1000
                }
            ],
            "expected": 1000,
            "description": "When the ATRIndicator is constructed with a large BarSeries (1000 bars), getUnstableBars() should return 1000, ensuring the method works for high counts."
        },
        {
            "input": [
                {
                    "parameter": "initialBarCount",
                    "value": -1
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "If the underlying BarSeries reports a negative bar count (which is invalid), getUnstableBars() should propagate the error, typically by throwing an IllegalArgumentException or similar. This case verifies defensive handling of corrupt state."
        }
    ]
}