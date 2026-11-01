{
    "testCases": [
        {
            "group": "CashFlow.numOf(Number) - null BarSeries",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "number",
                            "value": "42"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "CashFlow is instantiated with a null BarSeries. Any call to numOf should throw NullPointerException because the delegate field is null."
                }
            ]
        },
        {
            "group": "CashFlow.numOf(Number) - Number subclass propagates exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "number",
                            "value": "new Number() { public int intValue() { return 0; } public long longValue() { return 0L; } public float floatValue() { throw new IllegalArgumentException(\"invalid float\"); } public double doubleValue() { throw new IllegalArgumentException(\"invalid double\"); } }"
                        }
                    ],
                    "expected": "IllegalArgumentException",
                    "description": "A custom Number implementation throws IllegalArgumentException when its floatValue/doubleValue methods are invoked. The exception should propagate out of CashFlow.numOf."
                }
            ]
        },
        {
            "group": "CashFlow.numOf(Number) - BarSeries throws runtime exception",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "number",
                            "value": "123"
                        }
                    ],
                    "expected": "IllegalStateException",
                    "description": "The underlying BarSeries is mocked to throw IllegalStateException for the specific input value. CashFlow.numOf must not catch it and should let the exception bubble up."
                }
            ]
        }
    ]
}