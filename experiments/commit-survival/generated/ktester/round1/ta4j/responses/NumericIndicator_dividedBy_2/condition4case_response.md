{
    "group": "NumericIndicator.dividedBy(Number) - branch and edge cases",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 2
                }
            ],
            "expected": "5",
            "description": "Divide a positive constant indicator (value 10) by a positive integer (2). Expected result is 10 / 2 = 5."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": -2
                }
            ],
            "expected": "-5",
            "description": "Divide a positive constant indicator (value 10) by a negative integer (-2). Expected result is 10 / -2 = -5."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 2.5
                }
            ],
            "expected": "4",
            "description": "Divide a positive constant indicator (value 10) by a non‑integer Number (2.5). Expected result is 10 / 2.5 = 4."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 0
                }
            ],
            "expected": "ArithmeticException",
            "description": "Attempt to divide by zero. The underlying Num implementation throws an ArithmeticException; the test verifies that the exception propagates."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": null
                }
            ],
            "expected": "NullPointerException",
            "description": "Pass a null Number. The method should throw a NullPointerException before delegating to the overloaded method."
        }
    ]
}