{
    "testCases": [
        {
            "group": "Opened Position - Long with NaN finalPrice",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.NaN"
                        }
                    ],
                    "expected": "DecimalNum.NaN",
                    "description": "Opened long position (entry BUY) with finalPrice = NaN. amount * NaN yields NaN, NaN - entryValue is NaN, no negation applied."
                }
            ]
        },
        {
            "group": "Opened Position - Short with NaN finalPrice",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.NaN"
                        }
                    ],
                    "expected": "DecimalNum.NaN",
                    "description": "Opened short position (entry SELL) with finalPrice = NaN. amount * NaN = NaN, NaN - entryValue = NaN, then negated still NaN."
                }
            ]
        },
        {
            "group": "Closed Position - Long with non‑NaN finalPrice",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(10)"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(30)",
                    "description": "Closed long position where exit trade exists. finalPrice is ignored; gross profit = exitValue - entryValue = 80‑50 = 30."
                }
            ]
        },
        {
            "group": "Closed Position - Short with non‑NaN finalPrice",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(10)"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(-30)",
                    "description": "Closed short position where exit trade exists. finalPrice is ignored; gross profit = -(exitValue - entryValue) = -(80‑50) = -30."
                }
            ]
        },
        {
            "group": "Opened Position - Long Zero Amount",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(7)"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(0)",
                    "description": "Long position with entry amount = 0. Profit = 0 * finalPrice - entryValue (0) = 0."
                }
            ]
        },
        {
            "group": "Opened Position - Short Zero Amount",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(7)"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(0)",
                    "description": "Short position with entry amount = 0. amount * finalPrice - entryValue = 0, negated still 0."
                }
            ]
        },
        {
            "group": "Opened Position - Long Zero Entry Value",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(5)"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(50)",
                    "description": "Long position where entry value = 0 (price 0). Profit = amount * finalPrice - 0 = 10 * 5 = 50."
                }
            ]
        },
        {
            "group": "Opened Position - Short Zero Entry Value",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(5)"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(-50)",
                    "description": "Short position with entry value = 0. Gross profit = -(amount * finalPrice - 0) = -(10*5) = -50."
                }
            ]
        },
        {
            "group": "Opened Position - Long Negative Final Price",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(-5)"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(-100)",
                    "description": "Long position with negative finalPrice. Profit = 10 * (-5) - 50 = -100."
                }
            ]
        },
        {
            "group": "Opened Position - Short Negative Final Price",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.valueOf(-5)"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(100)",
                    "description": "Short position with negative finalPrice. Gross profit = -(10 * (-5) - 50) = 100."
                }
            ]
        },
        {
            "group": "Closed Position - Long Loss Scenario",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.NaN"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(-30)",
                    "description": "Closed long position where exitValue (50) < entryValue (80). Gross profit = 50‑80 = -30."
                }
            ]
        },
        {
            "group": "Closed Position - Short Gain Scenario",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "finalPrice",
                            "value": "DecimalNum.NaN"
                        }
                    ],
                    "expected": "DecimalNum.valueOf(30)",
                    "description": "Closed short position where exitValue (50) < entryValue (80). Gross profit = -(50‑80) = 30."
                }
            ]
        }
    ]
}