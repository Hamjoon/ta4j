{
    "group": "NumericIndicator.max(Indicator<Num>) - different BarSeries",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator of Num values [10, 20, 30] built on a BarSeries different from the one used by the 'this' NumericIndicator"
                }
            ],
            "expected": "IllegalArgumentException",
            "description": "The method should reject indicators that are based on a different BarSeries than the current instance; a BinaryOperation on mismatched series must throw IllegalArgumentException."
        }
    ]
}