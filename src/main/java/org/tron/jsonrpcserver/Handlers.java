package org.tron.jsonrpcserver;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.keystore.StringUtils;
import org.tron.keystore.WalletFile;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletClient;

import java.io.IOException;
import java.util.List;

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
                Object password = params.get(0);

                if (params.size() < 4) {
                    return new JSONRPC2Response(JSONRPC2Error.INVALID_PARAMS, request.getID());
                }

                // fetch params
                String from = String.valueOf(params.get(0));
                byte[] passwd = StringUtils.char2Byte(String.valueOf(params.get(1)).toCharArray());
                byte[] to = WalletClient.decodeFromBase58Check(String.valueOf(params.get(2)));
                long amount = Long.valueOf(String.valueOf(params.get(3)));

                WalletFile walletFile = null;
                try {
                    walletFile = WalletUtils.loadWalletFile(from);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                wallet = new WalletClient(walletFile);

                boolean isvalided;
                try {
                    isvalided = wallet.checkPassword(passwd);
                } catch (CipherException e) {
                    e.printStackTrace();
                }

                boolean isSendOk = false;
                try {
                    isSendOk = wallet.sendCoin(to, amount);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                wallet.logout();
                return new JSONRPC2Response(isSendOk, request.getID());

            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, request.getID());
            }
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
                    return new JSONRPC2Response(account, request.getID());
                }

            } else {
                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, request.getID());
            }
        }
    }
}
