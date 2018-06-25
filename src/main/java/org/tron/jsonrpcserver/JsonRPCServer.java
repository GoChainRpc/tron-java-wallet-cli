package org.tron.jsonrpcserver;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;
import com.typesafe.config.Config;
import org.tron.core.config.Configuration;

import javax.sound.sampled.Port;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author michael2008s
 * @Date 6/18/18 12:48
 */
public class JsonRPCServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 28080;


    private static class Handler extends Thread {


        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private Dispatcher dispatcher;
        static List<Socket> clients = new ArrayList<>();


        public Handler(Socket socket) {
            this.socket = socket;
            clients.add(socket);
            // Create a new JSON-RPC 2.0 request dispatcher
            this.dispatcher = new Dispatcher();

            // Register the handlers with it
            dispatcher.register(new Handlers.RegisterWalletHandler());
            dispatcher.register(new Handlers.SendCoinHandler());
            dispatcher.register(new Handlers.GetAccountHandler());
            dispatcher.register(new Handlers.GetTransactionByIdHandler());
            dispatcher.register(new Handlers.GetBlockHandler());
        }


        public void streamHandler() {
            try {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // read request
                String line;
                line = in.readLine();
                // System.out.println(line);
                StringBuilder raw = new StringBuilder();
                raw.append("" + line);
                boolean isPost = line.startsWith("POST");
                int contentLength = 0;
                while (!(line = in.readLine()).equals("")) {
                    // System.out.println(line);
                    raw.append('\n' + line);
                    if (isPost) {
                        final String contentHeader = "Content-Length: ";
                        if (line.startsWith(contentHeader)) {
                            contentLength = Integer.parseInt(line
                                    .substring(contentHeader.length()));
                        }
                    }
                }
                StringBuilder body = new StringBuilder();
                if (isPost) {
                    int c = 0;
                    for (int i = 0; i < contentLength; i++) {
                        c = in.read();
                        body.append((char) c);
                    }
                }

                System.out.println(body.toString());
                JSONRPC2Request request = JSONRPC2Request
                        .parse(body.toString());
                JSONRPC2Response resp = dispatcher.process(request, null);
//                System.out.println(resp.toJSONString());
                // send response
                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Content-Type: application/json\r\n");
                out.write("\r\n");
                out.write(resp.toJSONString());
                // do not in.close();
                out.flush();
                out.close();
                socket.close();
            } catch (IOException e) {
                System.out.println(e);
            } catch (JSONRPC2ParseException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        public void run() {
            streamHandler();
        }

    }

    public static void main(String[] args) throws Exception {

        System.out.println("Tron wallet rpc server is running.");

        Config config = Configuration.getByPath("config.conf");

        int port = PORT;
        if (config.hasPath("jsonrpc.port")) {
            port = config.getInt("jsonrpc.port");
            if (!(port > 1024 && port < 65536)) {
                port = PORT;
            }
        }
        System.out.println("Listener port:" + port);
        ServerSocket listener = new ServerSocket(port);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
}
