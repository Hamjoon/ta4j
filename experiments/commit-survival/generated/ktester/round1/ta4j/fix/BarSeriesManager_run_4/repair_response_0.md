```java
package org.ta4j.core.backtest;

/**
 * Simple compilation‑only test for {@link BarSeriesManager#run(Strategy, int, int)}.
 *
 * <p>This class does not depend on any external testing framework (JUnit, Mockito, …)
 * and therefore compiles with only the JDK and the ta4j library present in the
 * project.</p>
 *
 * <p>The test exercises the delegating {@code run} method, checks that it
 * validates its arguments and that it returns a {@link TradingRecord}
 * (which is empty when the supplied {@link Strategy} never signals).</p>
 *
 * <p>Run the test with {@code java -ea BarSeriesManagerRun4Test} to enable
 * assertions.</p>
 */
public class BarSeriesManagerRun4Test {

    /** Minimal {@link Strategy} implementation that never signals. */
    private static class NoSignalStrategy implements Strategy {
        @Override
        public boolean shouldEnter(int index, org.ta4j.core.BarSeries series) {
            return false;
        }

        @Override
        public boolean shouldExit(int index, org.ta4j.core.BarSeries series) {
            return false;
        }

        @Override
        public String getName() {
            return "NoSignalStrategy";
        }
    }

    public static void main(String[] args) {
        // Enable assertions when running from the command line:
        //   java -ea org.ta4j.core.backtest.BarSeriesManagerRun4Test
        testDelegateToOverloadedRunWithBuyAndOneNum();
        testIllegalArgumentWhenStartGreaterThanFinish();
        testIllegalArgumentWhenStartIsNegative();
        testNullPointerWhenStrategyIsNull();
        testIndexOutOfBoundsWhenStartExceedsSeriesSize();
        testIndexOutOfBoundsWhenFinishExceedsSeriesSize();
        testIllegalArgumentWhenFinishIsNegative();
        testEmptyRecordWhenSeriesIsEmptyAndRangeIsZero();
        testWholeSeriesProcessing();
        System.out.println("All tests passed.");
    }

    private static void testDelegateToOverloadedRunWithBuyAndOneNum() {
        // Build an empty series – its size is 0, but we only need it to construct the manager.
        BarSeries series = new BaseBarSeriesBuilder().withName("empty").build();

        BarSeriesManager manager = new BarSeriesManager(series);
        // Spy behaviour is not required – we simply call the public method.
        Strategy strategy = new NoSignalStrategy();

        // The delegating method should call the overload with BUY and amount = series.one().
        // Since we are not using a mocking framework we cannot verify the internal call,
        // but we can verify that the result is a (non‑null) TradingRecord.
        TradingRecord record = manager.run(strategy, 0, 0);
        assert record != null : "run should never return null";
        assert record.getPositions().isEmpty() : "Empty series + no‑signal strategy must yield no positions";
    }

    private static void testIllegalArgumentWhenStartGreaterThanFinish() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test")
                .withNumOfBars(10) // creates 10 empty bars
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();

        boolean thrown = false;
        try {
            manager.run(strategy, 8, 3);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assert thrown : "startIndex > finishIndex must throw IllegalArgumentException";
    }

    private static void testIllegalArgumentWhenStartIsNegative() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();

        boolean thrown = false;
        try {
            manager.run(strategy, -1, 4);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assert thrown : "Negative startIndex must throw IllegalArgumentException";
    }

    private static void testNullPointerWhenStrategyIsNull() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test")
                .withNumOfBars(5)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);

        boolean thrown = false;
        try {
            manager.run(null, 0, 4);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assert thrown : "Null strategy must throw NullPointerException";
    }

    private static void testIndexOutOfBoundsWhenStartExceedsSeriesSize() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();

        boolean thrown = false;
        try {
            manager.run(strategy, 1000, 1010);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        assert thrown : "startIndex beyond series size must throw IndexOutOfBoundsException";
    }

    private static void testIndexOutOfBoundsWhenFinishExceedsSeriesSize() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();

        boolean thrown = false;
        try {
            manager.run(strategy, 0, 1000);
        } catch (IndexOutOfBoundsException e) {
            thrown = true;
        }
        assert thrown : "finishIndex beyond series size must throw IndexOutOfBoundsException";
    }

    private static void testIllegalArgumentWhenFinishIsNegative() {
        BarSeries series = new BaseBarSeriesBuilder().withName("test")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();

        boolean thrown = false;
        try {
            manager.run(strategy, 2, -1);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assert thrown : "Negative finishIndex must throw IllegalArgumentException";
    }

    private static void testEmptyRecordWhenSeriesIsEmptyAndRangeIsZero() {
        BarSeries series = new BaseBarSeriesBuilder().withName("empty")
                .withNumOfBars(0)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();

        TradingRecord record = manager.run(strategy, 0, 0);
        assert record != null : "Result must not be null";
        assert record.getPositions().isEmpty() : "Empty series with zero range must yield empty record";
    }

    private static void testWholeSeriesProcessing() {
        // Build a series with a few bars – the concrete content is irrelevant for this test.
        BarSeries series = new BaseBarSeriesBuilder().withName("full")
                .withNumOfBars(10)
                .build();
        BarSeriesManager manager = new BarSeriesManager(series);
        Strategy strategy = new NoSignalStrategy();

        TradingRecord record = manager.run(strategy, 0, 9);
        assert record != null : "Result must not be null when processing the whole series";
        // Because the strategy never signals, the record stays empty.
        assert record.getPositions().isEmpty() : "No‑signal strategy must produce an empty record";
    }
}
```