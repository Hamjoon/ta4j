```java
package org.ta4j.core;

// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.DoubleNum;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

class Position_toString_Test {

    private Position emptyPosition;

    private Position openedPosition;

    private Position closedPosition;

    private Trade entryTrade;

    private Trade exitTrade;

    private CostModel transactionModel;

    private CostModel holdingModel;

    /**
     * the entry trade (may be {@code null})
     */
    private Trade entry;

    /**
     * the exit trade (may be {@code null})
     */
    private Trade exit;

    /**
     * cost model used when a trade is executed
     */
    private final CostModel transactionCostModel = new ZeroCostModel();

    /**
     * cost model used while a position is held
     */
    private final CostModel holdingCostModel = new ZeroCostModel();

    // @BeforeEach
    void setupBeforeEach() {
        // Cost models – the simplest (zero) models are sufficient for toString().
        transactionModel = new ZeroCostModel();
        holdingModel = new ZeroCostModel();

        // Trades used for opened / closed positions.
        entryTrade = Trade.buyAt(0, DoubleNum.valueOf(100), DoubleNum.valueOf(1), transactionModel);
        exitTrade = Trade.sellAt(5, DoubleNum.valueOf(110), DoubleNum.valueOf(1), transactionModel);

        // 1️⃣ Position with no trades.
        emptyPosition = new Position();

        // 2️⃣ Position with only an entry trade (opened).
        openedPosition = new Position();
        // creates the entry trade internally
        openedPosition.operate(entryTrade.getIndex());

        // 3️⃣ Position with both entry and exit trades (closed).
        closedPosition = new Position(entryTrade, exitTrade, transactionModel, holdingModel);
    }

    /**
     * Returns the entry trade (may be {@code null}).
     */
    public Trade getEntry() {
        return entry;
    }

    /**
     * Very small helper used by the test suite to simulate an opening trade.
     * It creates a BUY trade at the given index using the stored transaction
     * cost model. The exact price/amount are irrelevant for the {@code toString()}
     * tests – they only need a non‑null entry trade.
     *
     * @param index the index of the bar where the entry occurs
     */
    public void operate(int index) {
        // The library already provides static factories for BUY/SELL trades.
        // We simply create a BUY trade; the exact type does not matter for the
        // string representation tests.
        this.entry = Trade.buyAt(
                index,                     // index
                DoubleNum.valueOf(0),     // price (irrelevant)
                DoubleNum.valueOf(1),     // amount (irrelevant)
                transactionCostModel);
    }

    /**
     * Returns a textual representation of the position.
     *
     * <p>Format required by the tests:
     * <pre>
     * Entry: &lt;entry‑toString‑or‑null&gt; exit: &lt;exit‑toString‑or‑null&gt;
     * </pre>
     *
     * <p>The method deliberately does **not** catch any exception thrown by
     * {@code entry.toString()} or {@code exit.toString()}; they are allowed to
     * propagate, which is verified by several unit‑tests.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Entry: ");
        sb.append(entry != null ? entry.toString() : "null");
        sb.append(" exit: ");
        sb.append(exit != null ? exit.toString() : "null");
        return sb.toString();
    }

    /**
     * --------------------------------------------------------------------- *
     *  Equality – not required for the current tests but kept for completeness
     *  ---------------------------------------------------------------------
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Position))
            return false;
        Position other = (Position) obj;

        // Use the public getters of Position to avoid accessing its private fields.
        Trade otherEntry = other.getEntry();
        Trade otherExit = other.getExit();

        return (entry == null ? otherEntry == null : entry.equals(otherEntry))
                && (exit == null ? otherExit == null : exit.equals(otherExit));
    }

    @Override
    public int hashCode() {
        int result = (entry != null ? entry.hashCode() : 0);
        result = 31 * result + (exit != null ? exit.hashCode() : 0);
        return result;
    }
}
```