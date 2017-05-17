package org.adridadou.ethereum.ethj;

import org.adridadou.ethereum.propeller.event.BlockInfo;
import org.adridadou.ethereum.propeller.event.EthereumEventHandler;
import org.adridadou.ethereum.propeller.event.TransactionInfo;
import org.adridadou.ethereum.propeller.event.TransactionStatus;
import org.adridadou.ethereum.propeller.values.EthAddress;
import org.adridadou.ethereum.propeller.values.EthData;
import org.adridadou.ethereum.propeller.values.EthHash;
import org.adridadou.ethereum.propeller.values.EventInfo;
import org.ethereum.core.Block;
import org.ethereum.core.Transaction;
import org.ethereum.core.TransactionReceipt;
import org.ethereum.listener.EthereumListenerAdapter;
import org.ethereum.vm.LogInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 27.04.16.
 * This code is released under Apache 2 license
 */
public class EthJEventListener extends EthereumListenerAdapter {
    private final EthereumEventHandler eventHandler;

    EthJEventListener(EthereumEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void onBlock(Block block, List<TransactionReceipt> receipts) {
        eventHandler.onBlock(new BlockInfo(block.getNumber(), receipts.stream().map(this::toReceipt).collect(Collectors.toList())));
        receipts.forEach(tx -> eventHandler.onTransactionExecuted(new TransactionInfo(toReceipt(tx), TransactionStatus.Executed)));
    }

    private List<EventInfo> createEventInfoList(List<LogInfo> logs) {
        return logs.stream().map(log -> {
            List<EthData> topics = log.getTopics().stream().map(info -> EthData.of(info.getData())).collect(Collectors.toList());
            EthData eventSignature = topics.get(0);
            EthData eventArguments = EthData.of(log.getData());
            return new EventInfo(eventSignature, eventArguments, topics.subList(1, topics.size()));
        }).collect(Collectors.toList());
    }

    @Override
    public void onSyncDone(final SyncState syncState) {
        eventHandler.onReady();
    }

    private org.adridadou.ethereum.propeller.event.TransactionReceipt toReceipt(TransactionReceipt transactionReceipt) {
        Transaction tx = transactionReceipt.getTransaction();
        return new org.adridadou.ethereum.propeller.event.TransactionReceipt(EthHash.of(tx.getHash()), EthAddress.of(tx.getSender()), EthAddress.of(tx.getReceiveAddress()), EthAddress.of(tx.getContractAddress()), transactionReceipt.getError(), EthData.of(transactionReceipt.getExecutionResult()), transactionReceipt.isSuccessful() && transactionReceipt.isValid(), createEventInfoList(transactionReceipt.getLogInfoList()));
    }
}
