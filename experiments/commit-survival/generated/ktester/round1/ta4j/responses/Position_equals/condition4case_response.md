Invalid control character at: line 8 column 45 (char 219)
{
    "group": "Position.equals(Object) - branch coverage",
    "cases": [
        {
            "input": [
                {
                    "parameter": "thisPosition",
                    "value": "new Position() // entry=null, exit=null"
                },
                {
                    "parameter": "otherObject",
                    "value": "\"some string\" // not a Position"
                }
            ],
            "expected": false,
            "description": "Object is not an instance of Position, equals should return false."
        },
        {
            "input": [
                {
                    "parameter": "thisPosition",
                    "value": "new Position() // entry=null, exit=null"
                },
                {
                    "parameter": "otherObject",
                    "value": "new Position() // entry=null, exit=null"
                }
            ],
            "expected": true,
            "description": "Both positions have null entry and null exit; equality should be true."
        },
        {
            "input": [
                {
                    "parameter": "thisPosition",
                    "value": "new Position() // entry=null, exit=null"
                },
                {
                    "parameter": "otherObject",
                    "value": "Position with entry=Trade(id=1), exit=null"
                }
            ],
            "expected": false,
            "description": "This entry is null while other entry is non‑null; equals must return false."
        },
        {
            "input": [
                {
                    "parameter": "thisPosition",
                    "value": "Position with entry=Trade(id=1), exit=null"
                },
                {
                    "parameter": "otherObject",
                    "value": "new Position() // entry=null, exit=null"
                }
            ],
            "expected": false,
            "description": "This entry is non‑null while other entry is null; equals must return false."
        },
        {
            "input": [
                {
                    "parameter": "thisPosition",
                    "value": "Position with entry=Trade(id=1), exit=null"
                },
                {
                    "parameter": "otherObject",
                    "value": "Position with entry=Trade(id=2), exit=null"
                }
            ],
            "expected": false,
            "description": "Both entries are non‑null but not equal; equals must return false."
        },
        {
            "input": [
                {
                    "parameter": "thisPosition",
                    "value": "Position with entry=Trade(id=1), exit=null"
                },
                {
                    "parameter": "otherObject",
                    "value": "Position with entry=Trade(id=1), exit=null"
                }
            ],
            "expected": true,
            "description": "Entries are equal and both exits are null; equals should be true."
        },
        {
            "input": [
                {
                    "parameter": "thisPosition",
                    "value": "Position with entry=Trade(id=1), exit=null"
                },
                {
                    "parameter": "otherObject",
                    "value": "Position with entry=Trade(id=1), exit=Trade(id=3)"
                }
            ],
            "expected": false,
            "description": "Entries are equal but this exit is null while other exit is non‑null; equals must return false."
        },
        {
            "input": [
                {
                    "parameter": "thisPosition",
                    "value": "Position with entry=Trade(id=1), exit=Trade(id=3)"
                },
                {
                    "parameter": "otherObject",
                    "value": "Position with entry=Trade(id=1), exit=Trade(id=3)"
                }
            ],
            "expected": true,
            "description": "Both entries and exits are equal; equals should return true."
        },
        {
            "input": [
                {
                    "parameter": "thisPosition",
                    "value": "Position with entry=Trade(id=1), exit=Trade(id=3)"
                },
                {
                    "parameter": "otherObject",
                    "value": "Position with entry=Trade(id=1), exit=Trade(id=4)"
                }
            ],
            "expected": false,
            "description": "Entries are equal but exits are different; equals must return false."
        }
    ]
}