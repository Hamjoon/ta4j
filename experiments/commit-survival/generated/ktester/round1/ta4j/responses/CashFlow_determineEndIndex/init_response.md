```java
package org.ta4j.core.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;

/**
 * Unit tests for {@link CashFlow#determineEndIndex(Position, int, int)}.
 *
 * The method decides the last index up to which cash‑flow accrual should be
 * performed for a given {@link Position}. The logic is:
 *
 * <ul>
 *   <li>If the position has an exit trade, the candidate index is the exit
 *       trade index; otherwise it is the supplied {@code finalIndex}.</li>
 *   <li>The candidate index is limited to {@code finalIndex} (the caller’s
 *       limit).</li>
 *   <li>Finally the candidate index cannot exceed the maximal index of the
 *       series ({@code maxIndex}).</li>
 * </ul>
 *
 * The tests below cover all branches of this logic using Mockito mocks for
 * {@link Position} and {@link Trade}.
 */
@ExtendWith(MockitoExtension.class)
class CashFlow_determineEndIndex_Test {

    @Mock
    private Position mockPosition;   // reused mock, behaviour set per test

    @Mock
    private Trade mockExitTrade;     // mock for the exit trade when needed

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources required for these pure‑logic tests
    }

    @BeforeEach
    void setupBeforeEach() {
        // Reset mock interactions before each test
        reset(mockPosition, mockExitTrade);
    }

    @AfterEach
    void teardownAfterEach() {
        // Ensure no unexpected interactions remain
        verifyNoMoreInteractions(mockPosition, mockExitTrade);
    }

    @AfterAll
    static void teardownAfterAll() {
        // Nothing to clean up
    }

    /**
     * Scenario: Position has no exit trade. The method should return {@code finalIndex}
     * unless it exceeds {@code maxIndex}.
     */
    @Test
    void whenNoExit_thenReturnFinalIndexClampedToMaxIndex() {
        // exit == null
        when(mockPosition.getExit()).thenReturn(null);

        // finalIndex <= maxIndex
        int result1 = CashFlow.determineEndIndex(mockPosition, 5, 10);
        assertEquals(5, result1, "Should return finalIndex when it is within maxIndex");

        // finalIndex > maxIndex -> clamp
        int result2 = CashFlow.determineEndIndex(mockPosition, 15, 10);
        assertEquals(10, result2, "Should clamp to maxIndex when finalIndex exceeds it");
    }

    /**
     * Scenario: Position has an exit trade whose index is lower than {@code finalIndex}.
     * The method should use the exit index, then clamp to {@code maxIndex} if necessary.
     */
    @Test
    void whenExitBeforeFinalIndex_thenUseExitIndexClampedToMaxIndex() {
        // exit trade exists with index 3
        when(mockExitTrade.getIndex()).thenReturn(3);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        // exit index < finalIndex and < maxIndex
        int result1 = CashFlow.determineEndIndex(mockPosition, 8, 10);
        assertEquals(3, result1, "Should use exit index when it is the smallest bound");

        // exit index < finalIndex but > maxIndex -> clamp to maxIndex
        int result2 = CashFlow.determineEndIndex(mockPosition, 12, 5);
        // min(exit, final) = 3, then clamp to maxIndex=5 => 3 (already <=5)
        assertEquals(3, result2, "Exit index still governs; maxIndex larger than exit does not affect result");
    }

    /**
     * Scenario: Position has an exit trade whose index is greater than {@code finalIndex}.
     * The method should ignore the exit index and use {@code finalIndex}, then clamp to {@code maxIndex}.
     */
    @Test
    void whenExitAfterFinalIndex_thenIgnoreExitUseFinalIndexClampedToMaxIndex() {
        // exit trade index 12
        when(mockExitTrade.getIndex()).thenReturn(12);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        // finalIndex smaller than exit index
        int result1 = CashFlow.determineEndIndex(mockPosition, 7, 10);
        assertEquals(7, result1, "Should ignore exit index because it exceeds finalIndex");

        // finalIndex also exceeds maxIndex -> clamp to maxIndex
        int result2 = CashFlow.determineEndIndex(mockPosition, 9, 5);
        assertEquals(5, result2, "Should clamp to maxIndex after selecting finalIndex");
    }

    /**
     * Scenario: Exit index equals {@code finalIndex}. The method should return that value,
     * still respecting the {@code maxIndex} bound.
     */
    @Test
    void whenExitEqualsFinalIndex_thenReturnThatValueClampedToMaxIndex() {
        when(mockExitTrade.getIndex()).thenReturn(6);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        // finalIndex == exit index, within maxIndex
        int result1 = CashFlow.determineEndIndex(mockPosition, 6, 10);
        assertEquals(6, result1);

        // finalIndex == exit index, but exceeds maxIndex -> clamp
        int result2 = CashFlow.determineEndIndex(mockPosition, 6, 4);
        assertEquals(4, result2);
    }
}
```