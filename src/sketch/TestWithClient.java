package sketch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voltdb.client.Client;
import org.voltdb.client.ClientFactory;


class TestWithClient {

    static Client s_client;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        s_client = ClientFactory.createClient();
        s_client.createConnection("localhost");
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        s_client.close();
    }

    @BeforeEach
    void setUp() throws Exception {

    }

    @AfterEach
    void tearDown() throws Exception {

    }

    @Test
    void testFullTwelveScale() throws Exception {

    }
}
