{
    "groups": [
        {
            "group": "CostModel throws unchecked exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 5
                        }
                    ],
                    "expected": "IllegalStateException",
                    "description": "A custom CostModel implementation is programmed to throw IllegalStateException for any index. Position#getHoldingCost should propagate this unchecked exception unchanged."
                },
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": -2
                        }
                    ],
                    "expected": "ArithmeticException",
                    "description": "A custom CostModel throws ArithmeticException when finalIndex is negative. The method must not catch it and should let the exception bubble up."
                }
            ]
        },
        {
            "group": "CostModel throws NullPointerException internally",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 0
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "The CostModel's calculate method deliberately throws a NullPointerException (e.g., due to a bug). Position#getHoldingCost should forward this exception rather than masking it."
                }
            ]
        },
        {
            "group": "CostModel throws Error",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalIndex",
                            "value": 1
                        }
                    ],
                    "expected": "AssertionError",
                    "description": "A CostModel implementation throws an AssertionError (or any subclass of java.lang.Error). Position#getHoldingCost must not catch Errors; they should propagate to the caller."
                }
            ]
        }
    ]
}