package org.tron.jsonrpcserver;

import com.thetransactioncompany.jsonrpc2.server.*;

/**
 * @Description
 * @Author michael2008s
 * @Date 6/15/18 15:12
 */
public class JsonrpcDispatcher {

    public Dispatcher DispatcherRegister()  {
        // Create a new JSON-RPC 2.0 request dispatcher
        Dispatcher dispatcher = new Dispatcher();

        dispatcher.register(new Handlers.RegisterWalletHandler());
        dispatcher.register(new Handlers.SendCoinHandler());
        dispatcher.register(new Handlers.GetAccountHandler());
        dispatcher.register(new Handlers.GetTransactionByIdHandler());
        dispatcher.register(new Handlers.GetBlockHandler());

        return dispatcher;
    }

}
