```java
package org.ta4j.core.backtest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.ta4j.core.analysis.cost.CostModel;
import org.ta4j.core.analysis.cost.ZeroCostModel;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.num.Num;

import java.util.stream.Stream;

/**
 * Comprehensive tests for {@link BarSeriesManager#getTransactionCostModel()}.
 *
 * The test suite covers:
 * <ul>
 *   <li>Basic behaviours (ZeroCostModel, mock, null)</li>
 *   <li>Advanced scenarios such as custom anonymous implementations, spies,
 *       equality overrides, instance isolation, and immutability after other
 *       manager operations.</li>
 * </ul>
 *
 * The @Before/@After methods from the provided framework are left untouched.
 */
@ExtendWith(MockitoExtension.class)
class BarSeriesManager_getTransactionCostModel_Test {

    // ------------------------------------------------------------------------
    // Shared mock objects
    // ------------------------------------------------------------------------
    @Mock
    private BarSeries mockSeries;

    @Mock
    private TradeExecutionModel mockExecutionModel;

    @BeforeAll
    static void setupBeforeAll() {
        // No global resources to initialise
    }

    @BeforeEach
    void setupBeforeEach() {
        // Initialise Mockito annotations for @Mock fields
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardownAfterEach() {
        // No per‑test cleanup required
    }

    @AfterAll
    static void teardownAfterAll() {
        // No global cleanup required
    }

    // ------------------------------------------------------------------------
    // Basic behaviour – parameterised tests
    // ------------------------------------------------------------------------
    /**
     * Verifies that the getter returns exactly the instance supplied to the
     * constructor for the three basic cases: a real {@link ZeroCostModel},
     * a Mockito mock, and {@code null}.
     */
    @ParameterizedTest(name = "{index} => transactionCost={0}")
    @MethodSource("basicBehaviourProvider")
    void whenProvidingTransactionCostModel_thenGetterReturnsSameInstance(CostModel supplied) {
        // given – use the 3‑argument constructor (series, transactionCost, holdingCost)
        CostModel holdingCost = new ZeroCostModel(); // any non‑null holding cost
        BarSeriesManager manager = new BarSeriesManager(mockSeries, supplied, holdingCost);

        // when
        CostModel result = manager.getTransactionCostModel();

        // then
        assertSame(supplied, result,
                "The getter must return the exact instance that was passed to the constructor");
    }

    /** Supplies the three basic test inputs. */
    private static Stream<Arguments> basicBehaviourProvider() {
        CostModel zero = new ZeroCostModel();
        CostModel mock = mock(CostModel.class);
        CostModel nullModel = null;
        return Stream.of(
                Arguments.of(zero),
                Arguments.of(mock),
                Arguments.of(nullModel)
        );
    }

    // ------------------------------------------------------------------------
    // Advanced scenario – custom anonymous CostModel
    // ------------------------------------------------------------------------
    @Test
    void customAnonymousCostModel_isReturnedAndReferenceStaysConstant() {
        // given – an anonymous implementation that simply returns the amount
        CostModel custom = new CostModel() {
            @Override
            public Num calculate(Num amount, Num price) {
                return amount;
            }
        };
        BarSeriesManager manager = new BarSeriesManager(mockSeries, custom, new ZeroCostModel());

        // when – call the getter multiple times
        CostModel first = manager.getTransactionCostModel();
        CostModel second = manager.getTransactionCostModel();

        // then – both references must be identical to the original instance
        assertSame(custom, first, "First call should return the original custom CostModel");
        assertSame(custom, second, "Second call should return the same reference (==)");
    }

    // ------------------------------------------------------------------------
    // Advanced scenario – spy on ZeroCostModel
    // ------------------------------------------------------------------------
    @Test
    void spyZeroCostModel_isReturnedWithoutAnyInteraction() {
        // given – a spy wrapping a real ZeroCostModel
        ZeroCostModel realZero = new ZeroCostModel();
        ZeroCostModel spyZero = spy(realZero);
        BarSeriesManager manager = new BarSeriesManager(mockSeries, spyZero, new ZeroCostModel());

        // when – retrieve the transaction cost model
        CostModel result = manager.getTransactionCostModel();

        // then – the spy is returned and no method on it has been invoked yet
        assertSame(spyZero, result, "Getter should return the spy instance");
        verifyNoInteractions(spyZero);
    }

    // ------------------------------------------------------------------------
    // Advanced scenario – distinct mocks per manager (no cross‑sharing)
    // ------------------------------------------------------------------------
    @Test
    void eachManagerKeepsItsOwnTransactionCostModelInstance() {
        // given – two distinct mock CostModel objects
        CostModel mock1 = mock(CostModel.class);
        CostModel mock2 = mock(CostModel.class);
        BarSeriesManager manager1 = new BarSeriesManager(mockSeries, mock1, new ZeroCostModel());
        BarSeriesManager manager2 = new BarSeriesManager(mockSeries, mock2, new ZeroCostModel());

        // when
        CostModel result1 = manager1.getTransactionCostModel();
        CostModel result2 = manager2.getTransactionCostModel();

        // then – each manager returns its own mock, not the other's
        assertSame(mock1, result1, "Manager1 should return its own CostModel mock");
        assertSame(mock2, result2, "Manager2 should return its own CostModel mock");
        assertNotSame(result1, result2, "The two managers must not share the same CostModel instance");
    }

    // ------------------------------------------------------------------------
    // Advanced scenario – CostModel with overridden equals()/hashCode()
    // ------------------------------------------------------------------------
    @Test
    void costModelWithCustomEqualsAndHashCode_isReturnedAsOriginalReference() {
        // given – an anonymous CostModel that overrides equals & hashCode
        CostModel customEq = new CostModel() {
            @Override
            public Num calculate(Num amount, Num price) {
                return amount;
            }

            @Override
            public boolean equals(Object o) {
                // deliberately return true for any object
                return true;
            }

            @Override
            public int hashCode() {
                return 42;
            }
        };
        BarSeriesManager manager = new BarSeriesManager(mockSeries, customEq, new ZeroCostModel());

        // when
        CostModel result = manager.getTransactionCostModel();

        // then – the exact same instance must be returned (no copy based on equality)
        assertSame(customEq, result,
                "Even though equals() returns true, the getter must return the original object reference");
    }

    // ------------------------------------------------------------------------
    // Advanced scenario – immutability after other manager operations
    // ------------------------------------------------------------------------
    @Test
    void transactionCostModelReferenceRemainsSameAfterRunInvocation() {
        // given – manager with a ZeroCostModel
        ZeroCostModel zeroCost = new ZeroCostModel();
        BarSeriesManager manager = new BarSeriesManager(mockSeries, zeroCost, new ZeroCostModel());

        // store reference before any other operation
        CostModel beforeRun = manager.getTransactionCostModel();

        // mock a trivial Strategy that never recommends a trade
        Strategy dummyStrategy = mock(Strategy.class);
        when(dummyStrategy.shouldOperate(anyInt(), any(TradingRecord.class))).thenReturn(false);

        // when – invoke a run (the actual implementation is not under test here)
        // we use the simplest overload; any required parameters are mocked
        manager.run(dummyStrategy);

        // then – the reference after run must be identical to the one before run
        CostModel afterRun = manager.getTransactionCostModel();
        assertSame(beforeRun, afterRun,
                "The transactionCostModel field must stay unchanged after calling other methods");
    }

    // ------------------------------------------------------------------------
    // Existing tests from the original framework (kept unchanged)
    // ------------------------------------------------------------------------
    @Test
    void whenUsingDefaultConstructor_thenTransactionCostModelIsZeroCostModel() {
        // given
        BarSeriesManager manager = new BarSeriesManager(mockSeries);
        // when
        CostModel result = manager.getTransactionCostModel();
        // then
        assertTrue(result instanceof ZeroCostModel,
                "Default constructor should initialise a ZeroCostModel");
    }

    @Test
    void whenProvidingCustomTransactionCostModel_thenGetterReturnsSameInstance() {
        // given
        CostModel customTransactionCost = mock(CostModel.class);
        CostModel holdingCost = new ZeroCostModel(); // any holding cost works
        BarSeriesManager manager = new BarSeriesManager(mockSeries,
                customTransactionCost,
                holdingCost);
        // when
        CostModel result = manager.getTransactionCostModel();
        // then
        assertSame(customTransactionCost, result,
                "Getter must return the transaction cost model supplied to the constructor");
    }

    @Test
    void whenUsingFullConstructor_thenGetterReturnsProvidedTransactionCostModel() {
        // given
        CostModel customTransactionCost = mock(CostModel.class);
        CostModel customHoldingCost = mock(CostModel.class);
        // mockExecutionModel is already injected via @Mock
        BarSeriesManager manager = new BarSeriesManager(mockSeries,
                customTransactionCost,
                customHoldingCost,
                mockExecutionModel);
        // when
        CostModel result = manager.getTransactionCostModel();
        // then
        assertSame(customTransactionCost, result,
                "Getter must return the transaction cost model supplied to the full‑args constructor");
    }
}
```