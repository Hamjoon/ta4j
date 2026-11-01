{
    "group": "ATRIndicator.getBarCount.exception",
    "cases": [
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicator",
                    "value": null
                }
            ],
            "expected": "NullPointerException",
            "description": "When the ATRIndicator is constructed with a null averageTrueRangeIndicator, calling getBarCount() should throw a NullPointerException because the method dereferences the field."
        },
        {
            "input": [
                {
                    "parameter": "averageTrueRangeIndicator",
                    "value": "throwsRuntimeException"
                }
            ],
            "expected": "RuntimeException",
            "description": "If the underlying MMAIndicator's getBarCount() implementation throws a RuntimeException (e.g., IllegalStateException), getBarCount() must propagate that exception unchanged."
        }
    ]
}