{
    "group": "NumericIndicator.previous(int) basic shift",
    "cases": [
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 3
                }
            ],
            "expected": "For any index i >= 3, indicator.getValue(i) equals delegate.getValue(i-3). For i < 3 the result is Num.NaN.",
            "description": "Validate that previous(3) correctly shifts the underlying values by three bars. Use a stub delegate that returns the index as a Num; assert getValue(5) == 2 and getValue(2) == Num.NaN."
        },
        {
            "input": [
                {
                    "parameter": "barCount",
                    "value": 0
                }
            ],
            "expected": "The returned Indicator behaves identically to the original delegate (no shift).",
            "description": "Edge case where barCount is zero. According to the implementation contract, previous(0) should return an indicator that forwards calls directly to the delegate. Verify by comparing getValue(i) of both indicators for several i."
        }
    ]
}