{
    "group": "NumericIndicator.volume(BarSeries) – basic functionality",
    "cases": [
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "BarSeries with 3 bars, volumes: [100, 200, 300]"
                }
            ],
            "expected": "NumericIndicator whose delegate is a VolumeIndicator wrapping the given BarSeries; getValue(0) == 100, getValue(1) == 200, getValue(2) == 300",
            "description": "Verify that volume(bs) creates a NumericIndicator that correctly forwards volume values from a non‑empty BarSeries."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "BarSeries with 0 bars (empty series)"
                }
            ],
            "expected": "NumericIndicator whose delegate is a VolumeIndicator wrapping the empty BarSeries; calling getValue(any index) throws IndexOutOfBoundsException",
            "description": "Ensure that the method works with an empty series – the wrapper is created, but accessing values behaves as the underlying VolumeIndicator (which should throw when the index is out of range)."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "null"
                }
            ],
            "expected": "NullPointerException",
            "description": "Confirm that passing a null BarSeries results in a NullPointerException, matching the typical contract of factory methods that require a non‑null argument."
        }
    ]
}