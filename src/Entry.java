import com.frisky.fsesl.EslConnector;

import java.util.Properties;

public class Entry {

    public static void main(String... args) throws Exception {
        Properties properties = new Properties();
        properties.setProperty(EslConnector.FS_ESL_IP_ADDRESS,"127.0.0.1");
        properties.setProperty(EslConnector.FS_ESL_PASSWORD,"ClueCon");
        properties.setProperty(EslConnector.FS_ESL_PORT,"8021");
        EslConnector eslConnector = new EslConnector(properties);
        eslConnector.connect();
    }

}
