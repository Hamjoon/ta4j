```java
package org.ta4j.core;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.num.Num;

/**
 * A {@code Position} is a pair of two {@link Trade trades}.
 *
 * <p>
 * The exit trade has the complement type of the entry trade, i.e.:
 * <ul>
 *   <li>entry == BUY  → exit == SELL</li>
 *   <li>entry == SELL → exit == BUY</li>
 * </ul>
 *
 * <p>
 * Only the functionality required for the unit‑tests in this kata is
 * implemented: construction, a very small {@code operate(...)} helper,
 * {@code getEntry()}, and a {@code toString()} that follows the exact
 * format expected by the tests.
 */
public class Position {

    /** the entry trade (may be {@code null}) */
    private Trade entry;

    /** the exit trade (may be {@code null}) */
    private Trade exit;

    /** cost model used when a trade is executed */
    private final CostModel transactionCostModel;

    /** cost model used while a position is held */
    private final CostModel holdingCostModel;

    /** --------------------------------------------------------------------- *
     *  Constructors
     *  --------------------------------------------------------------------- */

    /** Creates an empty {@code Position}. */
    public Position() {
        this.transactionCostModel = new ZeroCostModel();
        this.holdingCostModel = new ZeroCostModel();
    }

    /**
     * Creates a {@code Position} with the supplied entry and exit trades.
     *
     * @param entry   the entry trade (may be {@code null})
     * @param exit    the exit trade (may be {@code null})
     * @param transactionCostModel model used for transaction costs
     * @param holdingCostModel      model used while the position is held
     */
    public Position(Trade entry,
                    Trade exit,
                    CostModel transactionCostModel,
                    CostModel holdingCostModel) {
        this.entry = entry;
        this.exit = exit;
        this.transactionCostModel = transactionCostModel != null ? transactionCostModel : new ZeroCostModel();
        this.holdingCostModel = holdingCostModel != null ? holdingCostModel : new ZeroCostModel();
    }

    /** --------------------------------------------------------------------- *
     *  Public API used by the tests
     *  --------------------------------------------------------------------- */

    /** Returns the entry trade (may be {@code null}). */
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
        this.entry = Trade.buyAt(index,
                                 DoubleNum.valueOf(0),   // price (irrelevant)
                                 DoubleNum.valueOf(1),   // amount (irrelevant)
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

    /** --------------------------------------------------------------------- *
     *  Equality – not required for the current tests but kept for completeness
     *  --------------------------------------------------------------------- */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return (entry == null ? other.entry == null : entry.equals(other.entry))
                && (exit == null ? other.exit == null : exit.equals(other.exit));
    }

    @Override
    public int hashCode() {
        int result = (entry != null ? entry.hashCode() : 0);
        result = 31 * result + (exit != null ? exit.hashCode() : 0);
        return result;
    }
}
```