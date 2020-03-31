import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.InterruptedException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.BlockingQueue;

public class BasicHttpServer implements Runnable{
    static HttpServer server;
    private static BlockingQueue blockingQueue;
    private static Object lock;

    public BasicHttpServer(BlockingQueue blockingQueue, Object lock) {
        this.blockingQueue = blockingQueue; this.lock = lock;
    }

    @Override
    public void run() {
        try {
            server = HttpServer.create(new InetSocketAddress(8888), 0);
            HttpContext context = server.createContext("/callback");
            context.setHandler(exchange -> {
                try {
                    handleRequest(exchange);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(HttpExchange exchange) throws IOException, InterruptedException {
        URI requestURI = exchange.getRequestURI();
        String response = "You may close this window.";
        blockingQueue.put(requestURI);
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        server.stop(1);
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}