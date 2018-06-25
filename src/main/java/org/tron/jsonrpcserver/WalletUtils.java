package org.tron.jsonrpcserver;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.commons.lang3.ArrayUtils;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.ByteArray;
import org.tron.keystore.WalletFile;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.tron.walletserver.WalletClient;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import static org.tron.walletserver.WalletClient.createTransferContract;

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


    public static Map buildTransactionMap(Protocol.Transaction transaction) {

        Map<String, Object> map = new HashMap<>();
        map.put("hash", ByteArray.toHexString(Sha256Hash.hash(transaction.toByteArray())));
        map.put("txid", ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray())));


        if (transaction.getRawData() != null) {
            map.put("raw_data", buildTransactionRow(transaction.getRawData()));
        }

        return map;
    }


    public static Map buildTransactionRow(Protocol.Transaction.raw raw) {
        Map<String, Object> map = new HashMap<>();
        if (raw.getRefBlockBytes() != null) {
            map.put("ref_block_bytes", ByteArray.toHexString(raw.getRefBlockBytes().toByteArray()));
        }
        map.put("ref_block_num", raw.getRefBlockNum());

        if (raw.getRefBlockHash() != null) {
            map.put("ref_block_hash", ByteArray.toHexString(raw.getRefBlockHash().toByteArray()));
        }
        if (raw.getContractCount() > 0) {
            map.put("contract", buildContractList(raw.getContractList()));
        }
        map.put("timestamp", raw.getTimestamp());

        return map;
    }

    public static ArrayList<Map> buildContractList(List<Protocol.Transaction.Contract> contractList) {
        ArrayList<Map> maps = new ArrayList<>();

        for (Protocol.Transaction.Contract contract : contractList) {
            Map<String, Object> contractMap = new HashMap<>();
            contractMap.put("type", contract.getType());

            switch (contract.getType()) {
                case TransferContract:
                    Contract.TransferContract transferContract = null;
                    // TODO when error?
                    try {
                        transferContract = contract.getParameter().unpack(Contract.TransferContract.class);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                    contractMap.put("owner_address", WalletClient.encode58Check(transferContract.getOwnerAddress().toByteArray()));
                    contractMap.put("to_address", WalletClient.encode58Check(transferContract.getToAddress().toByteArray()));
                    contractMap.put("amount", transferContract.getAmount());
                    break;
                default:
            }
            maps.add(contractMap);
        }
        return maps;
    }

    public static Map<String, Object> buildBlock(Protocol.Block block) {

        Map<String, Object> map = new HashMap<>();
        if (block.getBlockHeader() != null) {
            map.put("block_header", buildBlockHeader(block.getBlockHeader()));
        }
        if (block.getTransactionsCount() > 0) {
            map.put("transactions", buildTransactions(block.getTransactionsList()));
        }
        return map;
    }

    public static ArrayList<Map> buildTransactions(List<Protocol.Transaction> transactionList) {
        ArrayList<Map> maps = new ArrayList<>();
        for (Protocol.Transaction transaction : transactionList) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("transaction", buildTransactionMap(transaction));
            maps.add(buildTransactionMap(transaction));
        }
        return maps;
    }

    public static Map<String, Object> buildBlockHeader(Protocol.BlockHeader blockHeader) {

        Map<String, Object> map = new HashMap<>();
        map.put("raw_data", buildBlockRow(blockHeader.getRawData()));
        map.put("witness_signature", ByteArray.toHexString(blockHeader.getWitnessSignature().toByteArray()));

        return map;
    }

    public static Map<String, Object> buildBlockRow(Protocol.BlockHeader.raw raw) {

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", raw.getTimestamp());
        map.put("txTrieRoot", ByteArray.toHexString(raw.getTxTrieRoot().toByteArray()));
        map.put("parentHash", ByteArray.toHexString(raw.getParentHash().toByteArray()));
        map.put("number", raw.getNumber());
        map.put("witness_id", raw.getWitnessId());
        map.put("witness_address", WalletClient.encode58Check(raw.getWitnessAddress().toByteArray()));

        return map;
    }

}
