import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voltdb.VoltTable;
import org.voltdb.client.Client;
import org.voltdb.client.ClientFactory;

/**
 * If a select statement includes an arithmetic expression on DECIMAL columns and casts its result to DECIMAL,
 * does the resulting value delivered to the application/stored procedure have the same precision and scale constraints
 * as a value from a DECIMAL column in a table?
 *
 * We have a scheme that overcomes the 12 scale limitation of DECIMAL column types by shifting the decimal point left by
 * (12 - current-scale) and performs the reverse or retrieval.  However I’m concerned that when combined with a select
 * expression we have the potential to lose precision:  Here is an example of such an select expression:
 *
 * cast(SUM(Tot.Quantity * Tot.Price * power(10, 12 - Tot.currencyprecision) ) as decimal)
 *
 */
class TestDecimalPrecision {

    static Client s_client;
    // Using double will lose precision.
    static final BigDecimal s_scaleTwelveDecimal =
            new BigDecimal("280123.456789012345").setScale(12, RoundingMode.HALF_UP);

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        s_client = ClientFactory.createClient();
        s_client.createConnection("localhost");
        s_client.callProcedure("@AdHoc", "drop table t if exists;");
        s_client.callProcedure("@AdHoc", "create table t(a decimal);");
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        s_client.callProcedure("@AdHoc", "drop table t;");
        s_client.close();
    }

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
        s_client.callProcedure("@AdHoc", "truncate table t");
    }

    @Test
    void testFullTwelveScale() throws Exception {
        s_client.callProcedure("T.insert", s_scaleTwelveDecimal);
        VoltTable vt = s_client.callProcedure("@AdHoc", "select * from t").getResults()[0];
        assertTrue(vt.advanceRow());
        assertEquals(s_scaleTwelveDecimal, vt.getDecimalAsBigDecimal(0));

        // Add scale by 2.
        BigDecimal valConverted =
                new BigDecimal("2801.23456789012345").setScale(14, RoundingMode.HALF_UP);
        vt = s_client.callProcedure("@AdHoc", "select a/100 from t").getResults()[0];
        assertTrue(vt.advanceRow());
        assertFalse(valConverted.equals(vt.getDecimalAsBigDecimal(0)));
    }

    @Test
    void testInsertLiteral() throws Exception {
        s_client.callProcedure("@AdHoc", "insert into t values (280123.456789012345)");
        VoltTable vt = s_client.callProcedure("@AdHoc", "select * from t").getResults()[0];
        assertTrue(vt.advanceRow());
        assertEquals(s_scaleTwelveDecimal, vt.getDecimalAsBigDecimal(0));
        s_client.callProcedure("@AdHoc", "truncate table t");

        // precision = 38, scale = 12
        s_client.callProcedure("@AdHoc", "insert into t values (98765432109876543210987654.987654321098)");
        BigDecimal val = new BigDecimal("98765432109876543210987654.987654321098").setScale(12, RoundingMode.HALF_UP);
        vt = s_client.callProcedure("@AdHoc", "select * from t").getResults()[0];
        assertTrue(vt.advanceRow());
        assertEquals(val, vt.getDecimalAsBigDecimal(0));
    }

}
