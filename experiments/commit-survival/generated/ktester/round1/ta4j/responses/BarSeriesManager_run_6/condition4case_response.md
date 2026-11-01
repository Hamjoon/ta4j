{
    "group": "BarSeriesManager.run(Strategy, TradeType, Num, int, int) - basic flow and index bounds",
    "cases": [
        {
            "input": [
                { "parameter": "strategy", "value": "mockStrategyNeverOperates" },
                { "parameter": "tradeType", "value": "TradeType.BUY" },
                { "parameter": "amount", "value": "Num.valueOf(1)" },
                { "parameter": "startIndex", "value": "-5" },
                { "parameter": "finishIndex", "value": "200" }
            ],
            "expected": "tradeExecutionModel.execute is never called; returned TradingRecord is closed (no open position) and contains zero trades",
            "description": "startIndex is lower than series begin and finishIndex higher than series end, so runBeginIndex = seriesBegin and runEndIndex = seriesEnd. Strategy never signals an operation, therefore the inner loops are never entered and the record stays closed."
        },
        {
            "input": [
                { "parameter": "strategy", "value": "mockStrategyOpenCloseWithinRange" },
                { "parameter": "tradeType", "value": "TradeType.SELL" },
                { "parameter": "amount", "value": "Num.valueOf(10)" },
                { "parameter": "startIndex", "value": "2" },
                { "parameter": "finishIndex", "value": "8" }
            ],
            "expected": "tradeExecutionModel.execute is called exactly twice (at i=3 to open, i=6 to close); returned TradingRecord is closed and contains one completed trade",
            "description": "Both startIndex and finishIndex are inside the series. Strategy returns true on index 3 (open) and later on index 6 (close). After the loop the record is closed, so the post‑run extra‑bars block is not executed."
        },
        {
            "input": [
                { "parameter": "strategy", "value": "mockStrategyOpenAtEndCloseAfter" },
                { "parameter": "tradeType", "value": "TradeType.BUY" },
                { "parameter": "amount", "value": "Num.valueOf(5)" },
                { "parameter": "startIndex", "value": "0" },
                { "parameter": "finishIndex", "value": "barSeries.getEndIndex()" }
            ],
            "expected": "tradeExecutionModel.execute is called twice: first at the last series index to open a position, second at the first extra bar (runEndIndex+1) to close it; returned TradingRecord is closed with one completed trade",
            "description": "runEndIndex equals the series end. Strategy opens a position on the last bar. Because the position is still open and we are at the series end, the extra‑bars loop runs, finds a closing signal on the first extra bar, executes it and breaks. The final record is closed."
        },
        {
            "input": [
                { "parameter": "strategy", "value": "mockStrategyOpenAtEndNoCloseAfter" },
                { "parameter": "tradeType", "value": "TradeType.SELL" },
                { "parameter": "amount", "value": "Num.valueOf(2)" },
                { "parameter": "startIndex", "value": "0" },
                { "parameter": "finishIndex", "value": "barSeries.getEndIndex() - 2" }
            ],
            "expected": "tradeExecutionModel.execute is called once (open at runEndIndex); returned TradingRecord is NOT closed (open position remains) because runEndIndex != series end, so extra‑bars block is skipped",
            "description": "The run finishes before the series end, leaving an open position. The condition `runEndIndex == barSeries.getEndIndex()` is false, therefore the extra‑bars loop is not entered and the position stays open."
        },
        {
            "input": [
                { "parameter": "strategy", "value": "mockStrategyOperatesOnlyWithinBounds" },
                { "parameter": "tradeType", "value": "TradeType.BUY" },
                { "parameter": "amount", "value": "Num.valueOf(3)" },
                { "parameter": "startIndex", "value": "5" },
                { "parameter": "finishIndex", "value": "10" }
            ],
            "expected": "tradeExecutionModel.execute is called exactly once at i=7 (the only index where strategy.shouldOperate returns true); returned TradingRecord is open (single entry trade, no exit) because no closing signal is given within the run range and runEndIndex != series end",
            "description": "Both startIndex and finishIndex are inside the series, testing that `runBeginIndex = max(startIndex, seriesBegin)` and `runEndIndex = min(finishIndex, seriesEnd)` are applied correctly. Strategy signals an operation only on index 7, opening a trade that never closes within the provided range."
        }
    ]
}