Invalid control character at: line 8 column 54 (char 235)
{
    "group": "NumericIndicator.min with varying other indicator (SMA)",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.sma(3) // SMA of the original indicator with barCount=3"
                }
            ],
            "expected": "NumericIndicator whose getValue(i) equals min(originalIndicator.getValue(i), SMA_of_original.getValue(i)) for all valid i",
            "description": "The 'other' indicator is a moving average of the original values, producing both higher and lower values depending on the window. The min operation should correctly select the smaller value at each index."
        }
    ]
}