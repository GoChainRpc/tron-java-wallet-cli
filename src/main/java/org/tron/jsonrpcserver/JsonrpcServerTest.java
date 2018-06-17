package org.tron.jsonrpcserver;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;

import java.util.LinkedList;
import java.util.List;

/**
 * @Description
 * @Author michael2008s
 * @Date 6/15/18 15:40
 */
public class JsonrpcServerTest {


    public static void main(String[] args) {


        Dispatcher dispatcher = new JsonrpcDispatcher().DispatcherRegister();

//        List registerWalletParam = new LinkedList();
//        registerWalletParam.add("123456789");
//        JSONRPC2Request req = new JSONRPC2Request("registerWallet", registerWalletParam,new Long(1));
//        JSONRPC2Response resp = dispatcher.process(req, null);
//        System.out.println("Response: \n" + resp);


        List getAccountParam = new LinkedList();
        getAccountParam.add("TMXJTYXkSmo7a388so1Ntc2T5vspQA8BBw");
        JSONRPC2Request reqGetAccount =  new JSONRPC2Request("getAccount",getAccountParam,new Long(2));
        JSONRPC2Response respGetAccount = dispatcher.process(reqGetAccount, null);
        System.out.println("Response: \n" + respGetAccount);


    }

}
