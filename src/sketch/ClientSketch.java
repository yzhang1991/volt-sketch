package sketch;

import java.io.IOException;
import java.net.UnknownHostException;

import org.voltdb.client.Client;
import org.voltdb.client.ClientFactory;

public class ClientSketch {

    public static void main(String[] args) throws UnknownHostException, IOException {
        Client client = ClientFactory.createClient();
        client.createConnection("localhost");

    }
}
