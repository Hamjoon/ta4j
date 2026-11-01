{
    "group": "NumericIndicator.dividedBy(Number) - additional cases",
    "cases": [
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 2
                }
            ],
            "expected": "0",
            "description": "Divide a zero constant indicator (value 0) by a positive integer (2). Expected result is 0 / 2 = 0."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": -2
                }
            ],
            "expected": "0",
            "description": "Divide a zero constant indicator (value 0) by a negative integer (-2). Expected result is 0 / -2 = 0."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 2
                }
            ],
            "expected": "-5",
            "description": "Divide a negative constant indicator (value -10) by a positive integer (2). Expected result is -10 / 2 = -5."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": -2
                }
            ],
            "expected": "5",
            "description": "Divide a negative constant indicator (value -10) by a negative integer (-2). Expected result is -10 / -2 = 5."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": -2.5
                }
            ],
            "expected": "-4",
            "description": "Divide a positive constant indicator (value 10) by a negative fractional number (-2.5). Expected result is 10 / -2.5 = -4."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 0.0001
                }
            ],
            "expected": "100000",
            "description": "Divide a positive constant indicator (value 10) by a very small positive number (0.0001). Expected result is 10 / 0.0001 = 100000."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 1000
                }
            ],
            "expected": "0.01",
            "description": "Divide a positive constant indicator (value 10) by a large integer (1000). Expected result is 10 / 1000 = 0.01."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": 2.0
                }
            ],
            "expected": "5",
            "description": "Divide a positive constant indicator (value 10) by a Float instance representing 2.0. Expected result is 10 / 2.0 = 5."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": "2"
                }
            ],
            "expected": "5",
            "description": "Divide a positive constant indicator (value 10) by a BigDecimal created from the string \"2\". Expected result is 10 / 2 = 5."
        },
        {
            "input": [
                {
                    "parameter": "n",
                    "value": -0.5
                }
            ],
            "expected": "-20",
            "description": "Divide a positive constant indicator (value 10) by a negative fractional number (-0.5). Expected result is 10 / -0.5 = -20."
        }
    ]
}