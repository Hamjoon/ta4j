{
    "group": "NumericIndicator.min(Number) - mixed delegate values",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "13"
                }
            ],
            "expected": "Returned NumericIndicator yields values [10, 12, 13] at indexes 0, 1, 2 respectively (min of delegate values 10, 12, 15 and constant 13).",
            "description": "Constant lies between the delegate's minimum and maximum – the min operation should return the delegate value when it is lower and the constant when it is lower than the delegate at that index."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "-2"
                }
            ],
            "expected": "Returned NumericIndicator yields values [-5, -2, -2] at indexes 0, 1, 2 respectively (delegate values are -5, -2, 3).",
            "description": "Delegate contains negative and positive numbers; constant -2 is higher than the most negative value but lower than the positive value, verifying correct element‑wise min selection."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "0"
                }
            ],
            "expected": "Returned NumericIndicator yields values [-5, -2, 0] at indexes 0, 1, 2 respectively (delegate values are -5, -2, 3).",
            "description": "Zero constant with a delegate that has both negative and positive values – ensures zero is chosen only when it is the smaller of the two."
        }
    ]
}