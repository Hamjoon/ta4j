{
    "group": "Buy position gross return calculations",
    "cases": [
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
            "expected": "DecimalNum.valueOf(\"1.5\")",
            "description": "Entry trade is a BUY. Exit price is higher than entry price, so gross return = exit/entry = 150/100 = 1.5."
        },
        {
            "input": [
                {
                    "parameter": "entryPrice",
                    "value": "DecimalNum.valueOf(\"200\")"
                },
                {
                    "parameter": "exitPrice",
                    "value": "DecimalNum.valueOf(\"200\")"
                }
            ],
            "expected": "DecimalNum.valueOf(\"1\")",
            "description": "Buy position with no price change. Gross return should be exactly 1."
        },
        {
            "input": [
                {
                    "parameter": "entryPrice",
                    "value": "DecimalNum.valueOf(\"120\")"
                },
                {
                    "parameter": "exitPrice",
                    "value": "DecimalNum.valueOf(\"90\")"
                }
            ],
            "expected": "DecimalNum.valueOf(\"0.75\")",
            "description": "Buy position with a loss. Gross return = 90/120 = 0.75 (< 1)."
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
            "description": "Entry price is zero; division by zero should raise an ArithmeticException (or return NaN depending on Num implementation). The test verifies that an exception is thrown."
        }
    ]
}