{
    "group": "NumericIndicator.min(Number) - basic functionality",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "5"
                }
            ],
            "expected": "Returned NumericIndicator yields value 5 at any index because the constant (5) is lower than the delegate's values (10, 12, 15).",
            "description": "Constant is smaller than all delegate values – the min operation should always return the constant."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "20"
                }
            ],
            "expected": "Returned NumericIndicator yields the delegate values (10, 12, 15) at indexes 0,1,2 because the constant (20) is higher than each delegate value.",
            "description": "Constant is larger than all delegate values – the min operation should return the delegate's original values."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "12"
                }
            ],
            "expected": "Returned NumericIndicator yields values [10, 12, 12] at indexes 0,1,2 respectively (min of delegate and constant 12).",
            "description": "Constant equals one of the delegate values – verify that the min operation correctly picks the smaller of the two at each index."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-3"
                }
            ],
            "expected": "Returned NumericIndicator yields -3 at every index because the constant (-3) is lower than any positive delegate values.",
            "description": "Negative constant with positive delegate values – ensures min works with negative numbers."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "0"
                }
            ],
            "expected": "Returned NumericIndicator yields values [0, 0, 0] at indexes 0,1,2 because 0 is lower than delegate values (10,12,15).",
            "description": "Zero constant, testing boundary where constant is neither positive nor negative."
        }
    ]
}