{
    "testCases": [
        {
            "group": "NumericIndicator.squared() - empty delegate",
            "cases": [
                {
                    "input": [],
                    "expected": "Calling getValue(0) on the squared indicator throws IndexOutOfBoundsException because the underlying delegate contains no bars.",
                    "description": "Validate behaviour when the original indicator has an empty BarSeries (no data points). The squared() call should not fail during creation but any attempt to read a value must respect the empty series."
                }
            ]
        },
        {
            "group": "NumericIndicator.squared() - mixed values",
            "cases": [
                {
                    "input": [],
                    "expected": "NumericIndicator where getValue(i) = original.getValue(i) * original.getValue(i) for each index i. For a delegate returning [0, -3, 5, -1.2] the squared values are [0, 9, 25, 1.44] (within Num precision).",
                    "description": "Ensure that squared() correctly handles a mixture of zero, negative, positive and fractional numbers in a single series."
                }
            ]
        },
        {
            "group": "NumericIndicator.squared() - large numbers",
            "cases": [
                {
                    "input": [],
                    "expected": "NumericIndicator where getValue(i) = original.getValue(i) * original.getValue(i). For a delegate returning [10000000000] the squared value is [100000000000000000000] (1e20) without overflow.",
                    "description": "Verify that the implementation works with very large numeric values supported by the Num implementation and does not overflow."
                }
            ]
        },
        {
            "group": "NumericIndicator.squared() - bar series propagation",
            "cases": [
                {
                    "input": [],
                    "expected": "The BarSeries returned by squared().getBarSeries() is the same instance as the original NumericIndicator's BarSeries.",
                    "description": "Check that squared() forwards the BarSeries reference unchanged, preserving series context for downstream rules."
                },
                {
                    "input": [],
                    "expected": "The unstable bars count returned by squared().getUnstableBars() equals the original indicator's unstable bars count.",
                    "description": "Confirm that the unstable bars metadata is propagated unchanged by the squared() operation."
                }
            ]
        },
        {
            "group": "NumericIndicator.squared() - type and instance checks",
            "cases": [
                {
                    "input": [],
                    "expected": "The object returned by squared() is an instance of NumericIndicator and not the same reference as the original indicator.",
                    "description": "Assert that squared() creates a new NumericIndicator object (type safety) while keeping the original untouched."
                }
            ]
        },
        {
            "group": "NumericIndicator.squared() - composition with other operations",
            "cases": [
                {
                    "input": [],
                    "expected": "NumericIndicator where getValue(i) = (2 * original.getValue(i))^2 = 4 * original.getValue(i)^2. For a delegate returning [3, -4] the composed result values are [36, 64].",
                    "description": "Validate that squared() works correctly when applied to an indicator that is itself the result of another arithmetic operation (multipliedBy)."
                }
            ]
        }
    ]
}