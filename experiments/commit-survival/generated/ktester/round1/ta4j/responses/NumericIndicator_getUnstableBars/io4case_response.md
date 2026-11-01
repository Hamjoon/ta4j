{
    "group": "getUnstableBars",
    "cases": [
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator created from an SMAIndicator (which itself has unstable bars = barCount-1) should still report 0."
        },
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator created from an EMAIndicator (unstable bars = barCount-1) should still report 0."
        },
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator created from a StandardDeviationIndicator (unstable bars = barCount-1) should still report 0."
        },
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator created from a HighestValueIndicator (unstable bars = barCount-1) should still report 0."
        },
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator created from a LowestValueIndicator (unstable bars = barCount-1) should still report 0."
        },
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator created from a PreviousValueIndicator (unstable bars = 1) should still report 0."
        },
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator created from a ConstantIndicator (unstable bars = 0) should still report 0."
        },
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator after a complex chain: of(delegate).ema(5).sma(3).sqrt().abs().minus(2).multipliedBy(3) should still return 0."
        },
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator after chaining a rule‑producing method (crossedOver) does not affect getUnstableBars; the underlying NumericIndicator remains 0."
        },
        {
            "input": [],
            "expected": 0,
            "description": "NumericIndicator built from a delegate that itself is another NumericIndicator (e.g., NumericIndicator.of(NumericIndicator.of(delegate))) should still return 0."
        }
    ]
}