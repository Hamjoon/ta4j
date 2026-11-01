{
    "testCases": [
        {
            "group": "determineEndIndex - Exit before finalIndex",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "position",
                            "value": "mockPosition(exit=mockTrade(index=3))"
                        },
                        {
                            "parameter": "finalIndex",
                            "value": "7"
                        },
                        {
                            "parameter": "maxIndex",
                            "value": "10"
                        }
                    ],
                    "expected": "3",
                    "description": "Position has an exit at index 3, which is earlier than the supplied finalIndex (7). Since the exit occurs before finalIndex and is also below maxIndex, the method should return the exit index (3)."
                }
            ]
        },
        {
            "group": "determineEndIndex - Exit after finalIndex",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "position",
                            "value": "mockPosition(exit=mockTrade(index=9))"
                        },
                        {
                            "parameter": "finalIndex",
                            "value": "5"
                        },
                        {
                            "parameter": "maxIndex",
                            "value": "12"
                        }
                    ],
                    "expected": "5",
                    "description": "Position exit is at index 9, which is later than finalIndex (5). The method should use the earlier of exit index and finalIndex, i.e., finalIndex, and because it is below maxIndex, return 5."
                }
            ]
        },
        {
            "group": "determineEndIndex - Exit before maxIndex, finalIndex exceeds maxIndex",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "position",
                            "value": "mockPosition(exit=mockTrade(index=4))"
                        },
                        {
                            "parameter": "finalIndex",
                            "value": "15"
                        },
                        {
                            "parameter": "maxIndex",
                            "value": "10"
                        }
                    ],
                    "expected": "4",
                    "description": "Exit index (4) is earlier than both finalIndex (15) and maxIndex (10). After applying Math.min with exit and finalIndex, idx becomes 4, which is not greater than maxIndex, so the method returns 4."
                }
            ]
        },
        {
            "group": "determineEndIndex - Exit beyond maxIndex, finalIndex beyond maxIndex",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "position",
                            "value": "mockPosition(exit=mockTrade(index=20))"
                        },
                        {
                            "parameter": "finalIndex",
                            "value": "18"
                        },
                        {
                            "parameter": "maxIndex",
                            "value": "12"
                        }
                    ],
                    "expected": "12",
                    "description": "Both exit index (20) and finalIndex (18) exceed maxIndex (12). After the first min, idx = 18, then the cap to maxIndex forces the result to 12."
                }
            ]
        },
        {
            "group": "determineEndIndex - Exit beyond maxIndex, finalIndex within maxIndex",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "position",
                            "value": "mockPosition(exit=mockTrade(index=14))"
                        },
                        {
                            "parameter": "finalIndex",
                            "value": "9"
                        },
                        {
                            "parameter": "maxIndex",
                            "value": "10"
                        }
                    ],
                    "expected": "9",
                    "description": "Exit index (14) is after maxIndex, but finalIndex (9) is before maxIndex. The method picks the earlier of exit and finalIndex (9) and, because 9 ≤ maxIndex, returns 9."
                }
            ]
        },
        {
            "group": "determineEndIndex - Exit equals finalIndex",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "position",
                            "value": "mockPosition(exit=mockTrade(index=6))"
                        },
                        {
                            "parameter": "finalIndex",
                            "value": "6"
                        },
                        {
                            "parameter": "maxIndex",
                            "value": "8"
                        }
                    ],
                    "expected": "6",
                    "description": "Exit index and finalIndex are identical (6) and below maxIndex, so the method returns 6."
                }
            ]
        },
        {
            "group": "determineEndIndex - Negative finalIndex, no exit",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "position",
                            "value": "mockPosition(exit=null)"
                        },
                        {
                            "parameter": "finalIndex",
                            "value": "-2"
                        },
                        {
                            "parameter": "maxIndex",
                            "value": "5"
                        }
                    ],
                    "expected": "-2",
                    "description": "When finalIndex is negative and there is no exit, the method simply returns the finalIndex unchanged because it is not greater than maxIndex."
                }
            ]
        },
        {
            "group": "determineEndIndex - Negative maxIndex, no exit",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "position",
                            "value": "mockPosition(exit=null)"
                        },
                        {
                            "parameter": "finalIndex",
                            "value": "4"
                        },
                        {
                            "parameter": "maxIndex",
                            "value": "-3"
                        }
                    ],
                    "expected": "-3",
                    "description": "maxIndex is negative. finalIndex (4) exceeds maxIndex, so after the capping step the method returns maxIndex (-3)."
                }
            ]
        },
        {
            "group": "determineEndIndex - Negative exit index, finalIndex positive",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "position",
                            "value": "mockPosition(exit=mockTrade(index=-1))"
                        },
                        {
                            "parameter": "finalIndex",
                            "value": "3"
                        },
                        {
                            "parameter": "maxIndex",
                            "value": "5"
                        }
                    ],
                    "expected": "-1",
                    "description": "Exit index is negative and earlier than finalIndex. The method selects the earlier index (-1) and, because it is not greater than maxIndex, returns -1."
                }
            ]
        }
    ]
}