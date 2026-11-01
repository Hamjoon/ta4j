{
    "test_cases": [
        {
            "group": "operate - null price when position is new (entry trade creation)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 5
                        },
                        {
                            "parameter": "price",
                            "value": null
                        },
                        {
                            "parameter": "amount",
                            "value": "Num.valueOf(10.0)"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "When the Position is new and the price argument is null, the Trade constructor receives a null price and should throw a NullPointerException."
                }
            ]
        },
        {
            "group": "operate - null amount when position is new (entry trade creation)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 5
                        },
                        {
                            "parameter": "price",
                            "value": "Num.valueOf(100.0)"
                        },
                        {
                            "parameter": "amount",
                            "value": null
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "When the Position is new and the amount argument is null, the Trade constructor receives a null amount and should throw a NullPointerException."
                }
            ]
        },
        {
            "group": "operate - null price when position is opened (exit trade creation)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 10
                        },
                        {
                            "parameter": "price",
                            "value": null
                        },
                        {
                            "parameter": "amount",
                            "value": "Num.valueOf(5.0)"
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "When the Position is already opened (entry trade exists) and the price argument is null, creating the exit Trade should result in a NullPointerException."
                }
            ]
        },
        {
            "group": "operate - null amount when position is opened (exit trade creation)",
            "cases": [
                {
                    "input": [
                        {
                            "parameter": "index",
                            "value": 10
                        },
                        {
                            "parameter": "price",
                            "value": "Num.valueOf(105.0)"
                        },
                        {
                            "parameter": "amount",
                            "value": null
                        }
                    ],
                    "expected": "NullPointerException",
                    "description": "When the Position is already opened and the amount argument is null, creating the exit Trade should result in a NullPointerException."
                }
            ]
        }
    ]
}