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


        List getTxParam = new LinkedList();
        getTxParam.add("57de34ed53d8d19ddffae621bfcf1c93676ca18751275255f72c4f4484b54b23");
        JSONRPC2Request reqGetTx =  new JSONRPC2Request("getTransactionById",getTxParam,new Long(2));
        JSONRPC2Response respGetTx = dispatcher.process(reqGetTx, null);
        System.out.println("Response: \n" + respGetTx);

        List getblockParam = new LinkedList();
        getblockParam.add("-1");
        JSONRPC2Request reqGetBlock =  new JSONRPC2Request("getBlock",getblockParam,new Long(3));
        JSONRPC2Response respGetBlock = dispatcher.process(reqGetBlock, null);
        System.out.println("Response: \n" + respGetBlock);

    }

}
