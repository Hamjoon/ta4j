{
    "group": "NumericIndicator.highest(int) null delegate exception",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 5
                }
            ],
            "expected": "NullPointerException",
            "description": "Calling highest(int) on a NumericIndicator whose internal delegate is null should throw a NullPointerException because HighestValueIndicator cannot be constructed with a null source indicator."
        }
    ]
}