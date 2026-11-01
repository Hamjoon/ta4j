{
    "group": "NumericIndicator.closePrice(BarSeries) exception edge cases",
    "cases": [
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "seriesWithNullBar"
                }
            ],
            "expected": "NullPointerException when calling getValue(0) on the returned NumericIndicator because the underlying BarSeries contains a null Bar.",
            "description": "Validate that closePrice propagates a NullPointerException when the BarSeries includes a null Bar element; the ClosePriceIndicator will attempt to access the bar's close price and fail."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "seriesWithBarHavingNullClose"
                }
            ],
            "expected": "NullPointerException when calling getValue(0) on the returned NumericIndicator because the Bar's close price is null.",
            "description": "Test behavior when a Bar in the series has a null close price; the ClosePriceIndicator should throw a NullPointerException when retrieving that value."
        },
        {
            "input": [
                {
                    "parameter": "bs",
                    "value": "faultySeriesThrowsOnAccess"
                }
            ],
            "expected": "RuntimeException (or the specific exception thrown by the faulty series) when calling getValue(0) on the returned NumericIndicator.",
            "description": "Ensure that any unchecked exception thrown by a custom BarSeries implementation during bar access propagates through the NumericIndicator created by closePrice."
        }
    ]
}