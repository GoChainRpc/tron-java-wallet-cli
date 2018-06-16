package org.tron.jsonrpcserver;

import org.apache.commons.lang3.ArrayUtils;
import org.tron.keystore.WalletFile;

import java.io.File;
import java.io.IOException;

/**
 * @Description
 * @Author michael2008s
 * @Date 6/15/18 21:33
 */
public class WalletUtils {


    private static final String FilePath = "Wallet";


    public static WalletFile loadWalletFile(String address) throws IOException {
        File wallet = selectWalletFile(address);
        if (wallet == null) {
            throw new IOException(
                    "No keystore file found, please use registerwallet or importwallet first!");
        }
        return org.tron.keystore.WalletUtils.loadWalletFile(wallet);
    }

    public static File selectWalletFile(String address) {
        File file = new File(FilePath);
        if (!file.exists() || !file.isDirectory()) {
            return null;
        }

        File[] wallets = file.listFiles();
        if (ArrayUtils.isEmpty(wallets)) {
            return null;
        }

        File wallet;
        if (wallets.length > 1) {
            for (int i = 0; i < wallets.length; i++) {
                if (wallets[i].getName().contains(address)) {
                    return wallets[i];
                }
            }
        }

        return null;
    }

}
