{
    "testCases": [
        {
            "group": "Position.toString() - entry toString throws IllegalStateException",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalStateException",
                    "description": "The entry Trade object's toString() method is overridden to throw an IllegalStateException. When Position.toString() is invoked, the exception should be propagated unchanged."
                }
            ]
        },
        {
            "group": "Position.toString() - entry toString throws NullPointerException",
            "cases": [
                {
                    "input": [],
                    "expected": "NullPointerException",
                    "description": "The entry Trade object's toString() method is overridden to throw a NullPointerException. Position.toString() must not catch it; the same exception should be thrown to the caller."
                }
            ]
        },
        {
            "group": "Position.toString() - exit toString throws RuntimeException",
            "cases": [
                {
                    "input": [],
                    "expected": "RuntimeException",
                    "description": "The exit Trade object's toString() method is overridden to throw a generic RuntimeException. Position.toString() should propagate this exception."
                }
            ]
        },
        {
            "group": "Position.toString() - both entry and exit toString throw",
            "cases": [
                {
                    "input": [],
                    "expected": "IllegalStateException",
                    "description": "Both entry and exit Trade objects have toString() implementations that throw exceptions, but entry's exception (IllegalStateException) occurs first. Position.toString() should propagate the first encountered exception."
                }
            ]
        }
    ]
}