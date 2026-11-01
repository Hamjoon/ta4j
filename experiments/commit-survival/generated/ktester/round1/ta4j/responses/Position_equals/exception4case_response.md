Invalid control character at: line 10 column 70 (char 308)
{
    "testCases": [
        {
            "group": "testEquals_entryEqualsThrowsException",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "obj",
                            "value": "Position(entry=null, exit=null) // other Position with null entry and exit"
                        }
                    ],
                    "expected": "java.lang.NullPointerException",
                    "description": "The primary Position has a non‑null entry Trade whose equals method throws NullPointerException when the argument is null. The other Position's entry is null, causing entry.equals(null) to be invoked and the exception to propagate."
                }
            ]
        },
        {
            "group": "testEquals_exitEqualsThrowsException",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "obj",
                            "value": "Position(entry=null, exit=null) // other Position with null entry and exit"
                        }
                    ],
                    "expected": "java.lang.NullPointerException",
                    "description": "The primary Position has a non‑null exit Trade whose equals method throws NullPointerException when the argument is null. The other Position's exit is null, causing exit.equals(null) to be invoked and the exception to propagate."
                }
            ]
        }
    ]
}