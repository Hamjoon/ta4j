{
    "group": "crossedUnder_exceptions",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(null)"
                }
            ],
            "expected": "NullPointerException thrown",
            "description": "Passing a NumericIndicator created with a null delegate should cause the crossedUnder method (or the CrossedDownIndicatorRule constructor) to throw NPE because the underlying delegate is required."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new Indicator<Num>() {\n    @Override public Num getValue(int index) { throw new IllegalStateException(\"forced failure\"); }\n    @Override public BarSeries getBarSeries() { return series; }\n    @Override public int getUnstableBars() { return 0; }\n})"
                }
            ],
            "expected": "IllegalStateException thrown when Rule.isSatisfied(index) is called",
            "description": "The supplied indicator throws an exception from getValue; the rule creation succeeds, but evaluating the rule should propagate the IllegalStateException."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new ConstantIndicator<>(null))"
                }
            ],
            "expected": "NullPointerException thrown when Rule.isSatisfied(index) is called",
            "description": "ConstantIndicator constructed with a null Num leads to NPE during rule evaluation because the comparison with a null value is illegal."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new Indicator<Num>() {\n    private final Num[] values = { Num.valueOf(5), Num.valueOf(6) };\n    @Override public Num getValue(int index) { return values[index]; }\n    @Override public BarSeries getBarSeries() { return series; }\n    @Override public int getUnstableBars() { return 0; }\n})"
                }
            ],
            "expected": "IndexOutOfBoundsException thrown when Rule.isSatisfied(2) is called",
            "description": "The 'other' indicator has fewer data points than the primary indicator; accessing index 2 during rule evaluation exceeds its array bounds, resulting in IndexOutOfBoundsException."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "NumericIndicator.of(new Indicator<Num>() {\n    @Override public Num getValue(int index) { return Num.valueOf(index); }\n    @Override public BarSeries getBarSeries() { return null; }\n    @Override public int getUnstableBars() { return 0; }\n})"
                }
            ],
            "expected": "NullPointerException thrown when Rule.isSatisfied(index) is called",
            "description": "The supplied indicator returns null for getBarSeries(); CrossedDownIndicatorRule later accesses the series and throws NPE."
        }
    ]
}