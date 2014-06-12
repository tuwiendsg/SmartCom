package at.ac.tuwien.dsg.smartcom.scm.manager.am.adapter;

import at.ac.tuwien.dsg.smartcom.adapter.PeerAdapter;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.scm.manager.am.AddressResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerAdapterExecution implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(PeerAdapterExecution.class);

    private final PeerAdapter adapter;
    private final AddressResolver address;
    private final String id;
    private final boolean stateful;
    private final MessageBroker broker;

    public PeerAdapterExecution(PeerAdapter adapter, AddressResolver address, String id, boolean stateful, MessageBroker broker) {
        this.adapter = adapter;
        this.address = address;
        this.id = id;
        this.stateful = stateful;
        this.broker = broker;
    }

    public PeerAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            log.info("Waiting for new task ...");
            Message message = broker.receiveTasks(id);
            if (message == null) {
                log.info("Received interrupted!");
                break;
            }
            log.info("Received task {}", message);
            PeerAddress peerAddress = address.getPeerAddress(message.getReceiverId(), (stateful ? id.substring(0, id.lastIndexOf(".")) : id));

            log.info("Sending message {} to peer {}", message, peerAddress);
            adapter.push(message, peerAddress);
        }
    }
}
