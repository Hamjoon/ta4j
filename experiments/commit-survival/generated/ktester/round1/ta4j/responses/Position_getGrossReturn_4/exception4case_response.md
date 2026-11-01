{
    "test_cases": [
        {
            "group": "Null entry price handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": null
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"100\")"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "entryPrice is null; the method should throw a NullPointerException before any arithmetic is performed (both BUY and SELL branches)."
                }
            ]
        },
        {
            "group": "Null exit price handling",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"100\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": null
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "exitPrice is null; the method should throw a NullPointerException when attempting to call dividedBy on a null reference (both BUY and SELL branches)."
                }
            ]
        },
        {
            "group": "Buy position zero‑zero division",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"0\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"0\")"
                        }
                    ],
                    "expected": "ArithmeticException",
                    "description": "Both entryPrice and exitPrice are zero for a BUY position; exitPrice.dividedBy(entryPrice) results in a division‑by‑zero and must raise ArithmeticException."
                }
            ]
        },
        {
            "group": "Sell position zero‑zero division",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "entryPrice",
                            "value": "DecimalNum.valueOf(\"0\")"
                        },
                        {
                            "parameter": "exitPrice",
                            "value": "DecimalNum.valueOf(\"0\")"
                        }
                    ],
                    "expected": "ArithmeticException",
                    "description": "Both entryPrice and exitPrice are zero for a SELL position; the internal division (exitPrice.dividedBy(entryPrice)) triggers a division‑by‑zero and must raise ArithmeticException."
                }
            ]
        }
    ]
}