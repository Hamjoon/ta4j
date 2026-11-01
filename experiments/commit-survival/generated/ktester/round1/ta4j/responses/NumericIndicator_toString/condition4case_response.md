{
    "group": "toString",
    "cases": [
        {
            "input": [],
            "expected": "same as delegate.toString()",
            "description": "NumericIndicator created with a ConstantIndicator delegate; toString() should return the ConstantIndicator's string representation."
        },
        {
            "input": [],
            "expected": "same as delegate.toString()",
            "description": "NumericIndicator created with a custom Indicator implementation whose toString() returns a fixed custom string; toString() must return that custom string."
        },
        {
            "input": [],
            "expected": "same as delegate.toString()",
            "description": "NumericIndicator created with a Mockito‑mocked Indicator where toString() is stubbed to return \"mocked-indicator\"; toString() should return \"mocked-indicator\"."
        }
    ]
}