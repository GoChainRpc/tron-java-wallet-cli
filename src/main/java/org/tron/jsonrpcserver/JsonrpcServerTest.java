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
//        System.out.println("Request: \n" + req);
//        JSONRPC2Response resp = dispatcher.process(req, null);
//        System.out.println("Response: \n" + resp);


        List getAccountParam = new LinkedList();
        getAccountParam.add("TMXJTYXkSmo7a388so1Ntc2T5vspQA8BBw");
        JSONRPC2Request reqGetAccount =  new JSONRPC2Request("getAccount",getAccountParam,new Long(2));
        System.out.println("Request: \n" + reqGetAccount);
        JSONRPC2Response respGetAccount = dispatcher.process(reqGetAccount, null);
        System.out.println("Response: \n" + respGetAccount);


        List getTxParam = new LinkedList();
        getTxParam.add("cf123efeac6aae9da71420f8d320fd0bda8111808ac018db38c2b4b086ef5fdb");
        JSONRPC2Request reqGetTx =  new JSONRPC2Request("getTransactionById",getTxParam,new Long(2));
        System.out.println("Request: \n" + reqGetTx);
        JSONRPC2Response respGetTx = dispatcher.process(reqGetTx, null);
        System.out.println("Response: \n" + respGetTx);

        List getTxInfoParam = new LinkedList();
        getTxInfoParam.add("cf123efeac6aae9da71420f8d320fd0bda8111808ac018db38c2b4b086ef5fdb");
        JSONRPC2Request reqGetTxInfo =  new JSONRPC2Request("getTransactionInfoById",getTxInfoParam,new Long(5));
        System.out.println("Request: \n" + reqGetTxInfo);
        JSONRPC2Response respGetTxInfo = dispatcher.process(reqGetTxInfo, null);
        System.out.println("Response: \n" + respGetTxInfo);

        List getblockParam = new LinkedList();
        getblockParam.add("-1");
        JSONRPC2Request reqGetBlock =  new JSONRPC2Request("getBlock",getblockParam,new Long(3));
        System.out.println("Request: \n" + reqGetBlock);
        JSONRPC2Response respGetBlock = dispatcher.process(reqGetBlock, null);
        System.out.println("Response: \n" + respGetBlock);

    }

}
