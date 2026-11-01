Invalid control character at: line 26 column 68 (char 992)
{
    "group": "ATRIndicator#getTRIndicator - normal operation",
    "cases": [
        {
            "input": [
                {
                    "parameter": "trIndicator",
                    "value": "new TRIndicator(mockBarSeries)"
                },
                {
                    "parameter": "atrIndicator",
                    "value": "new ATRIndicator(mockBarSeries, new TRIndicator(mockBarSeries))"
                }
            ],
            "expected": "Same TRIndicator instance that was passed to the ATRIndicator constructor",
            "description": "Verify that getTRIndicator() returns the exact TRIndicator object supplied to the ATRIndicator constructor."
        },
        {
            "input": [
                {
                    "parameter": "trIndicator",
                    "value": "null"
                },
                {
                    "parameter": "atrIndicator",
                    "value": "new ATRIndicator(mockBarSeries, null) // using reflection to bypass final check if needed"
                }
            ],
            "expected": "null",
            "description": "When the ATRIndicator is constructed with a null TRIndicator (possible via reflection or a permissive constructor), getTRIndicator() should return null."
        }
    ]
}