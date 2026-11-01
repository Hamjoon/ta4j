{
    "group": "NumericIndicator.min(Indicator<Num>) exception handling",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": null
                }
            ],
            "expected": "NullPointerException",
            "description": "Passing a null indicator should cause the method to throw NullPointerException because the implementation attempts to use the argument without null‑check."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(Num.valueOf(5)) built on a different BarSeries than the caller"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "When the supplied indicator belongs to a BarSeries that is not the same as the caller's series, the BinaryOperation.min factory validates series compatibility and throws IllegalArgumentException."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "Indicator based on a BarSeries with fewer bars than the caller's series"
                }
            ],
            "expected": "IndexOutOfBoundsException when getValue(index) is invoked beyond the shorter series length",
            "description": "The min indicator delegates to both underlying indicators for each index. If the 'other' indicator's series is shorter, accessing an index that exists only in the caller's series triggers IndexOutOfBoundsException."
        }
    ]
}