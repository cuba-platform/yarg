import com.haulmont.yarg.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server()
                .reportsPath("H:\\WORK\\platform\\yarg\\core\\modules\\server\\src");
        server.init();
    }
}
