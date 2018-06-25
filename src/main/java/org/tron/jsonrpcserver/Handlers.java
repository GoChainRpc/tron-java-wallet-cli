package org.tron.jsonrpcserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.gson.Gson;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.keystore.StringUtils;
import org.tron.keystore.WalletFile;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @Description
 * @Author michael2008s
 * @Date 6/15/18 15:18
 */
public class Handlers {


    // params [password]
    public static class RegisterWalletHandler implements RequestHandler {

        private WalletClient wallet;

        @Override
        public String[] handledRequests() {
            return new String[]{"registerWallet"};
        }

        @Override
        public JSONRPC2Response process(JSONRPC2Request request, MessageContext requestCtx) {

            if (request.getMethod().equals("registerWallet")) {
                List params = (List) request.getParams();
                Object password = params.get(0);

                if (params.size() < 1) {
                    return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, request.getID());
                }

                char[] passwordCharArray = String.valueOf(password).toCharArray();

                if ((passwordCharArray).length < 6) {
                    return new JSONRPC2Response(new JSONRPC2Error(-32602, "password not Valid"), request.getID());
                }

                try {
                    wallet = new WalletClient(StringUtils.char2Byte(passwordCharArray));
                } catch (CipherException e) {
                    e.printStackTrace();
                }

                String keystoreName = null;
                String walletAddress = null;
                if (wallet != null) {
                    try {
                        keystoreName = wallet.store2Keystore();
                        walletAddress = WalletClient.encode58Check(wallet.getAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        wallet.logout();
                    }

                    System.out.println("keystoreName:" + keystoreName);

                    return new JSONRPC2Response(walletAddress, request.getID());
                } else {
                    return new JSONRPC2Response(new JSONRPC2Error(-32603, "can not create wallet"), request.getID());
                }

            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, request.getID());
            }
        }
    }

    // params [from,pwd,to,amount]
    public static class SendCoinHandler implements RequestHandler {
        private WalletClient wallet;

        @Override
        public String[] handledRequests() {
            return new String[]{"sendCoin"};
        }

        @Override
        public JSONRPC2Response process(JSONRPC2Request request, MessageContext requestCtx) {
            if (request.getMethod().equals("sendCoin")) {

                if (wallet != null) {
                    wallet.logout();
                    wallet = null;
                }

                List params = (List) request.getParams();

                if (params.size() < 4) {
                    return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, request.getID());
                }

                // fetch params
                String from = String.valueOf(params.get(0));
                byte[] passwd = StringUtils.char2Byte(String.valueOf(params.get(1)).toCharArray());
                byte[] to = WalletClient.decodeFromBase58Check(String.valueOf(params.get(2)));
                long amount = Long.valueOf(String.valueOf(params.get(3)));

                System.out.println(from + passwd + to + amount);

                WalletFile walletFile = null;
                try {
                    walletFile = WalletUtils.loadWalletFile(from);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                wallet = new WalletClient(walletFile);

//                boolean isvalided;
//                try {
//                    isvalided = wallet.checkPassword(passwd);
//                } catch (CipherException e) {
//                    e.printStackTrace();
//                }


                Protocol.Transaction transaction = wallet.sendCoinReturnTx(to, amount);
                String txid = ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
                boolean isSendOk = false;
                try {
                    isSendOk = wallet.sendCoinSignAndBroadcast(transaction, passwd);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    wallet.logout();
                }
                return new JSONRPC2Response(txid, request.getID());

            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, request.getID());
            }
        }
    }

    private static class IgnoreInheritedIntrospector extends JacksonAnnotationIntrospector {
        @Override
        public boolean hasIgnoreMarker(final AnnotatedMember m) {
            return super.hasIgnoreMarker(m);
        }
    }

    //params [address]
    public static class GetAccountHandler implements RequestHandler {
        @Override
        public String[] handledRequests() {
            return new String[]{"getAccount"};
        }

        @Override
        public JSONRPC2Response process(JSONRPC2Request request, MessageContext requestCtx) {
            if (request.getMethod().equals("getAccount")) {
                List params = (List) request.getParams();
                Object addressObj = params.get(0);

                if (params.size() < 1) {
                    return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, request.getID());
                }

                String address = String.valueOf(addressObj);
                byte[] addressBytes = WalletClient.decodeFromBase58Check(address);
                if (addressBytes == null) {
                    return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, request.getID());
                }

                Protocol.Account account = WalletClient.queryAccount(addressBytes);
                if (account == null) {
                    return new JSONRPC2Response(new JSONRPC2Error(-32603, "Get Account failed !!!!"), request.getID());
                } else {
//                    ObjectMapper m = new ObjectMapper();
//                    m.setAnnotationIntrospector(new IgnoreInheritedIntrospector());
//                    FilterProvider filterProvider = new SimpleFilterProvider().addFilter("antPathFilter", new AntPathPropertyFilter(includedFieldNames));
//                    m.setFilterProvider(filterProvider);
//                    Map<String,Object> props = m.convertValue(account, Map.class);
                    Map<String, Object> accountMap = new HashMap<>();
                    accountMap.put("type", account.getType());
                    accountMap.put("balance", account.getBalance());

                    return new JSONRPC2Response(accountMap, request.getID());
                }

            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, request.getID());
            }
        }
    }


    public static class GetTransactionByIdHandler implements RequestHandler {

        @Override
        public String[] handledRequests() {
            return new String[]{"getTransactionById"};
        }

        @Override
        public JSONRPC2Response process(JSONRPC2Request request, MessageContext requestCtx) {
            if (request.getMethod().equals("getTransactionById")) {
                List params = (List) request.getParams();
                Object txidObj = params.get(0);

                if (params.size() < 1) {
                    return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, request.getID());
                }

                String txid = String.valueOf(txidObj);
                Optional<Protocol.Transaction> result = WalletClient.getTransactionById(txid);

                if (result.isPresent()) {
                    Protocol.Transaction transaction = result.get();
//                    Gson gson = new Gson();
//                    Map<String, Object> txMap = new HashMap<>();
//                    txMap.put("hash", ByteArray.toHexString(Sha256Hash.hash(transaction.toByteArray())));
//                    txMap.put("txid", ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray())));
                    return new JSONRPC2Response(WalletUtils.buildTransactionMap(transaction), request.getID());
                } else {
                    return new JSONRPC2Response(new JSONRPC2Error(-32603, "getTransactionById failed !!!!"), request.getID());
                }
            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, request.getID());
            }
        }
    }


    // params [index]  // -1 is latest block
    public static class GetBlockHandler implements RequestHandler {

        @Override
        public String[] handledRequests() {
            return new String[]{"getBlock"};
        }

        @Override
        public JSONRPC2Response process(JSONRPC2Request request, MessageContext requestCtx) {
            if (request.getMethod().equals("getBlock")) {

                List params = (List) request.getParams();
                if (params.size() < 1) {
                    return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, request.getID());
                }

                long blockNum = -1;
                try {
                    blockNum = Long.parseLong(String.valueOf(params.get(0)));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                Protocol.Block block = WalletClient.getBlock(blockNum);
//                Gson gson = new Gson();
//              printTransactionList
//                Map<String, Object> blockMap = new HashMap<>();
//                blockMap.put("type", block.getBlockHeader());
//                blockMap.put("txlist", block.getTransactionsList());
                return new JSONRPC2Response(WalletUtils.buildBlock(block), request.getID());

            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, request.getID());
            }
        }
    }
}
