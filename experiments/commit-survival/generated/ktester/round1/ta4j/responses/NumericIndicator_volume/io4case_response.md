{
    "group": "NumericIndicator.volume(BarSeries) – single‑bar series",
    "cases": [
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "BarSeries with 1 bar, volume: [42]"
                }
            ],
            "expected": "NumericIndicator whose delegate is a VolumeIndicator wrapping the given BarSeries; getValue(0) == 42 and getBarSeries() returns the original BarSeries",
            "description": "Validate that volume(bs) works for the minimal non‑empty series and that the returned indicator correctly forwards the sole volume value and preserves the original BarSeries reference."
        }
    ]
}