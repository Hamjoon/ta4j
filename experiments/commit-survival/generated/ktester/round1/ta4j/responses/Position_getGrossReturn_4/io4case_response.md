{
    "testCases": [
        {
            "group": "Sell position gross return calculations",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"200\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"150\")"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(\"1.25\")",
                    "description": "Entry trade is a SELL. Exit price is lower than entry price, producing a profit. Gross return = 2 - (exit/entry) = 2 - (150/200) = 2 - 0.75 = 1.25."
                },
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"100\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"150\")"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(\"0.5\")",
                    "description": "Entry trade is a SELL. Exit price is higher than entry price, producing a loss. Gross return = 2 - (150/100) = 2 - 1.5 = 0.5."
                },
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"120\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"120\")"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(\"1\")",
                    "description": "SELL position with no price change. Gross return should be exactly 1 (2 - 1)."
                },
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"0\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"100\")"
                        }
                    ],
                    "expected": "ArithmeticException",
                    "description": "Entry price is zero for a SELL position; division by zero should raise an ArithmeticException."
                }
            ]
        },
        {
            "group": "Zero price edge cases",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"100\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"0\")"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(\"0\")",
                    "description": "BUY position where exit price is zero. Gross return = 0/100 = 0."
                },
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"100\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"0\")"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(\"2\")",
                    "description": "SELL position where exit price is zero. Gross return = 2 - (0/100) = 2."
                }
            ]
        },
        {
            "group": "NaN handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "NaN"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"100\")"
                        }
                    ],
                    "expected": "NaN",
                    "description": "Entry price is NaN; any arithmetic with NaN should result in NaN."
                },
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"100\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "NaN"
                        }
                    ],
                    "expected": "NaN",
                    "description": "Exit price is NaN; the gross return should propagate NaN."
                }
            ]
        }
    ]
}