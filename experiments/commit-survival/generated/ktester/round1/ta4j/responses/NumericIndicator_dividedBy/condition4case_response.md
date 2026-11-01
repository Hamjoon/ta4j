{
    "group": "Normal division",
    "cases": [
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(2)"
                }
            ],
            "expected": "NumericIndicator whose getValue(i) == 5 for any index i (10 / 2)",
            "description": "Divides a NumericIndicator wrapping a constant 10 by another constant indicator 2. The result should be a NumericIndicator that returns 5 for all indices."
        },
        {
            "input": [
                {
                    "parameter": "other",
                    "value": "ConstantIndicator(0.5)"
                }
            ],
            "expected": "NumericIndicator whose getValue(i) == 20 for any index i (10 / 0.5)",
            "description": "Divides a constant 10 by a constant 0.5. Verifies that the quotient is correctly calculated as 20."
        }
    ]
}