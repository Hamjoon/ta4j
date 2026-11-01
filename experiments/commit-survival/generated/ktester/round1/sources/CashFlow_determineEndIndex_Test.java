package org.ta4j.core.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.Position;
import org.ta4j.core.Trade;
import java.util.stream.Stream;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

@ExtendWith(MockitoExtension.class)
class CashFlow_determineEndIndex_Test {

    @Mock
    private Position // reused mock, behaviour set per test
    mockPosition;

    @Mock
    private Trade // mock for the exit trade when needed
    mockExitTrade;

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

    static Stream<Arguments> noExitProvider() {
        return Stream.of(// finalIndex <= maxIndex
        Arguments.of("finalIndex (5) ≤ maxIndex (10) → return finalIndex", 5, 10, 5), // finalIndex > maxIndex
        Arguments.of("finalIndex (15) > maxIndex (10) → clamp to maxIndex", 15, 10, 10), // negative finalIndex, still no exit
        Arguments.of("negative finalIndex (-2) with maxIndex 5 → return finalIndex", -2, 5, -2), // negative maxIndex, no exit
        Arguments.of("maxIndex (-3) smaller than finalIndex (4) → clamp to maxIndex", 4, -3, -3));
    }

    static Stream<Arguments> exitBeforeFinalProvider() {
        return Stream.of(// exit < final && exit < max
        Arguments.of(3, 7, 10, 3), // exit < final && max < exit (should still return exit because it is min)
        // min(exit,final)=4 -> clamp to maxIndex=2 => 2
        Arguments.of(4, 15, 2, 2));
    }

    static Stream<Arguments> exitAfterFinalProvider() {
        return Stream.of(// exit > final, final <= max
        Arguments.of(9, 5, 12, 5), // exit > final, final > max -> clamp to max
        Arguments.of(12, 9, 5, 5));
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
