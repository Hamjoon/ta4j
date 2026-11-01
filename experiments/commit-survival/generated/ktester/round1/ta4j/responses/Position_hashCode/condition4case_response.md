{
    "group": "hashCode - both entry and exit are null",
    "cases": [
        {
            "input": [],
            "expected": "Objects.hash(null, null) == 961",
            "description": "A Position with no entry and no exit should return the hash code computed by Objects.hash(null, null). This verifies that the method correctly handles null fields."
        }
    ]
}