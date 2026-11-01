{
    "new_cases": [
        {
            "group": "Gross Return – Empty BarSeries",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barSeries",
                            "value": "mockEmptyBarSeries"
                        }
                    ],
                    "expected": "IndexOutOfBoundsException",
                    "description": "BarSeries contains no bars; getEntry().getPricePerAsset(barSeries) attempts to access a non‑existent index and should throw IndexOutOfBoundsException"
                }
            ]
        },
        {
            "group": "Gross Return – getPricePerAsset IllegalArgumentException",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "barSeries",
                            "value": "mockBarSeriesThrowsIllegalArgument"
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "The BarSeries implementation validates the requested index and throws IllegalArgumentException; getGrossReturn should propagate this exception"
                }
            ]
        }
    ]
}