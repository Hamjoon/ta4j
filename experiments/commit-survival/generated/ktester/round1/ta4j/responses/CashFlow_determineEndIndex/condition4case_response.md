{
    "group": "determineEndIndex - No Exit",
    "cases": [
        {
            "input": [
                {
                    "parameter": "position",
                    "value": "mockPosition(exit=null)"
                },
                {
                    "parameter": "finalIndex",
                    "value": "5"
                },
                {
                    "parameter": "maxIndex",
                    "value": "10"
                }
            ],
            "expected": "5",
            "description": "Position has no exit; finalIndex (5) is less than maxIndex (10). The method should return finalIndex unchanged."
        },
        {
            "input": [
                {
                    "parameter": "position",
                    "value": "mockPosition(exit=null)"
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
            "expected": "10",
            "description": "Position has no exit; finalIndex (15) exceeds maxIndex (10). The method should cap the result at maxIndex."
        }
    ]
}