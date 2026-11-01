{
  "group": "CashFlow.numOf(Number) - delegation tests",
  "cases": [
    {
      "input": [
        {
          "parameter": "number",
          "value": "42"
        }
      ],
      "expected": "Num instance equal to barSeries.numOf(42) (e.g., a Num with value 42)",
      "description": "Verify that an Integer is correctly delegated to BarSeries.numOf and the returned Num reflects the same numeric value."
    },
    {
      "input": [
        {
          "parameter": "number",
          "value": "3.1415"
        }
      ],
      "expected": "Num instance equal to barSeries.numOf(3.1415) (e.g., a Num with value 3.1415)",
      "description": "Verify that a Double is correctly delegated to BarSeries.numOf and the returned Num reflects the same numeric value."
    },
    {
      "input": [
        {
          "parameter": "number",
          "value": "new java.math.BigDecimal(\"123456.789\")"
        }
      ],
      "expected": "Num instance equal to barSeries.numOf(BigDecimal(\"123456.789\")) (e.g., a Num with value 123456.789)",
      "description": "Verify that a BigDecimal is correctly delegated to BarSeries.numOf and the returned Num reflects the same numeric value."
    },
    {
      "input": [
        {
          "parameter": "number",
          "value": "null"
        }
      ],
      "expected": "NullPointerException",
      "description": "When a null Number is passed, the method should propagate a NullPointerException (thrown by the underlying BarSeries.numOf implementation)."
    },
    {
      "input": [
        {
          "parameter": "number",
          "value": "Long.valueOf(9999999999L)"
        }
      ],
      "expected": "Num instance equal to barSeries.numOf(9999999999L) (e.g., a Num with value 9999999999)",
      "description": "Verify that a Long value is correctly delegated and the returned Num matches the long value."
    }
  ]
}