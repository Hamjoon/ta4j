```java
package org.ta4j.core.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
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
 * The test class follows the grouping defined in the JSON test specification.
 * Each group is exercised by a dedicated (parameterised) test method.
 */
@ExtendWith(MockitoExtension.class)
class CashFlow_determineEndIndex_Test {

    @Mock
    private Position mockPosition; // reused mock, behaviour set per test

    @Mock
    private Trade mockExitTrade;   // mock for the exit trade when needed

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

    /* --------------------------------------------------------------------- *
     *  Group: determineEndIndex - No Exit
     * --------------------------------------------------------------------- */
    @ParameterizedTest(name = "[No Exit] finalIndex={1}, maxIndex={2} ⇒ expected={3}")
    @MethodSource("noExitProvider")
    void whenNoExit_thenReturnFinalIndexClampedToMaxIndex(
            String description,
            int finalIndex,
            int maxIndex,
            int expected) {

        // Position without an exit trade
        when(mockPosition.getExit()).thenReturn(null);

        int result = CashFlow.determineEndIndex(mockPosition, finalIndex, maxIndex);
        assertEquals(expected, result, description);
    }

    static Stream<Arguments> noExitProvider() {
        return Stream.of(
                // finalIndex <= maxIndex
                Arguments.of("finalIndex (5) ≤ maxIndex (10) → return finalIndex",
                        5, 10, 5),
                // finalIndex > maxIndex
                Arguments.of("finalIndex (15) > maxIndex (10) → clamp to maxIndex",
                        15, 10, 10),
                // negative finalIndex, still no exit
                Arguments.of("negative finalIndex (-2) with maxIndex 5 → return finalIndex",
                        -2, 5, -2),
                // negative maxIndex, no exit
                Arguments.of("maxIndex (-3) smaller than finalIndex (4) → clamp to maxIndex",
                        4, -3, -3)
        );
    }

    /* --------------------------------------------------------------------- *
     *  Group: determineEndIndex - Exit before finalIndex
     * --------------------------------------------------------------------- */
    @ParameterizedTest(name = "[Exit before final] exit={0}, final={1}, max={2} ⇒ expected={3}")
    @MethodSource("exitBeforeFinalProvider")
    void whenExitBeforeFinalIndex_thenUseExitIndexClampedToMaxIndex(
            int exitIdx,
            int finalIndex,
            int maxIndex,
            int expected) {

        // Configure mocks
        when(mockExitTrade.getIndex()).thenReturn(exitIdx);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        int result = CashFlow.determineEndIndex(mockPosition, finalIndex, maxIndex);
        assertEquals(expected, result);
    }

    static Stream<Arguments> exitBeforeFinalProvider() {
        return Stream.of(
                // exit < final && exit < max
                Arguments.of(3, 7, 10, 3),
                // exit < final && max < exit (should still return exit because it is min)
                Arguments.of(4, 15, 2, 2) // min(exit,final)=4 -> clamp to maxIndex=2 => 2
        );
    }

    /* --------------------------------------------------------------------- *
     *  Group: determineEndIndex - Exit after finalIndex
     * --------------------------------------------------------------------- */
    @ParameterizedTest(name = "[Exit after final] exit={0}, final={1}, max={2} ⇒ expected={3}")
    @MethodSource("exitAfterFinalProvider")
    void whenExitAfterFinalIndex_thenIgnoreExitUseFinalIndexClampedToMaxIndex(
            int exitIdx,
            int finalIndex,
            int maxIndex,
            int expected) {

        when(mockExitTrade.getIndex()).thenReturn(exitIdx);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        int result = CashFlow.determineEndIndex(mockPosition, finalIndex, maxIndex);
        assertEquals(expected, result);
    }

    static Stream<Arguments> exitAfterFinalProvider() {
        return Stream.of(
                // exit > final, final <= max
                Arguments.of(9, 5, 12, 5),
                // exit > final, final > max -> clamp to max
                Arguments.of(12, 9, 5, 5)
        );
    }

    /* --------------------------------------------------------------------- *
     *  Group: determineEndIndex - Exit before maxIndex, finalIndex exceeds maxIndex
     * --------------------------------------------------------------------- */
    @Test
    void whenExitBeforeMaxIndexAndFinalExceedsMax_thenReturnExitIndex() {
        // exit=4, final=15, max=10 → min(exit,final)=4, 4≤max → 4
        when(mockExitTrade.getIndex()).thenReturn(4);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        int result = CashFlow.determineEndIndex(mockPosition, 15, 10);
        assertEquals(4, result);
    }

    /* --------------------------------------------------------------------- *
     *  Group: determineEndIndex - Exit beyond maxIndex, finalIndex beyond maxIndex
     * --------------------------------------------------------------------- */
    @Test
    void whenExitAndFinalBeyondMax_thenClampToMaxIndex() {
        // exit=20, final=18, max=12 → min(exit,final)=18 → clamp to 12
        when(mockExitTrade.getIndex()).thenReturn(20);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        int result = CashFlow.determineEndIndex(mockPosition, 18, 12);
        assertEquals(12, result);
    }

    /* --------------------------------------------------------------------- *
     *  Group: determineEndIndex - Exit beyond maxIndex, finalIndex within maxIndex
     * --------------------------------------------------------------------- */
    @Test
    void whenExitBeyondMaxButFinalWithinMax_thenReturnFinalIndex() {
        // exit=14, final=9, max=10 → min(exit,final)=9 → 9≤max → 9
        when(mockExitTrade.getIndex()).thenReturn(14);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        int result = CashFlow.determineEndIndex(mockPosition, 9, 10);
        assertEquals(9, result);
    }

    /* --------------------------------------------------------------------- *
     *  Group: determineEndIndex - Exit equals finalIndex
     * --------------------------------------------------------------------- */
    @Test
    void whenExitEqualsFinalIndex_thenReturnThatValueClampedToMaxIndex() {
        when(mockExitTrade.getIndex()).thenReturn(6);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        // within maxIndex
        assertEquals(6, CashFlow.determineEndIndex(mockPosition, 6, 8));

        // exceeds maxIndex → clamp
        assertEquals(4, CashFlow.determineEndIndex(mockPosition, 6, 4));
    }

    /* --------------------------------------------------------------------- *
     *  Group: determineEndIndex - Negative exit index, finalIndex positive
     * --------------------------------------------------------------------- */
    @Test
    void whenNegativeExitIndex_thenReturnNegativeExit() {
        // exit=-1, final=3, max=5 → min(exit,final) = -1 → -1 ≤ max → -1
        when(mockExitTrade.getIndex()).thenReturn(-1);
        when(mockPosition.getExit()).thenReturn(mockExitTrade);

        int result = CashFlow.determineEndIndex(mockPosition, 3, 5);
        assertEquals(-1, result);
    }
}
```