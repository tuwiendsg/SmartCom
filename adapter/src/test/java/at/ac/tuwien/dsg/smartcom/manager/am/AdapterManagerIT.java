package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.SimpleMessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageBroker;
import at.ac.tuwien.dsg.smartcom.broker.MessageListener;
import at.ac.tuwien.dsg.smartcom.callback.PMCallback;
import at.ac.tuwien.dsg.smartcom.manager.AdapterManager;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatefulAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.StatelessAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.adapter.TestInputPullAdapter;
import at.ac.tuwien.dsg.smartcom.manager.am.dao.MongoDBResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.am.dao.ResolverDAO;
import at.ac.tuwien.dsg.smartcom.manager.am.utils.MongoDBInstance;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import at.ac.tuwien.dsg.smartcom.model.PeerAddress;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;
import com.mongodb.MongoClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AdapterManagerIT {
    public static final int AMOUNT_OF_PEERS = 1000;
    private MongoDBInstance mongoDB;

    private AdapterManager manager;
    private MessageBroker broker;

    @Before
    public void setUp() throws Exception {
        mongoDB = new MongoDBInstance();
        mongoDB.setUp();

        broker = new SimpleMessageBroker();

        MongoClient mongo = new MongoClient("localhost", 12345);
        ResolverDAO dao = new MongoDBResolverDAO(mongo, "test-resolver", "resolver");

        manager = new AdapterManagerImpl(dao, new PMCallbackImpl(), broker);
        manager.init();
    }

    @After
    public void tearDown() throws Exception {
        mongoDB.tearDown();
        manager.destroy();
    }

    @Test(timeout = 20000l)
    public void test() throws InterruptedException {
        Identifier statefulAdapterId = manager.registerOutputAdapter(StatefulAdapter.class);
        Identifier statelessAdapterId = manager.registerOutputAdapter(StatelessAdapter.class);

        List<Identifier> adapterIds = new ArrayList<>(AMOUNT_OF_PEERS);
        List<RoutingRule> rules = new ArrayList<>(AMOUNT_OF_PEERS);
        List<Identifier> peers = new ArrayList<>(AMOUNT_OF_PEERS);
        for (int i = 0; i < AMOUNT_OF_PEERS; i++) {
            peers.add(Identifier.peer("peer"+i));
        }

        for (Identifier peer : peers) {
            RoutingRule route = manager.createEndpointForPeer(peer);
            rules.add(route);
            adapterIds.add(manager.addPullAdapter(new TestInputPullAdapter(peer.getId()+"."+route.getRoute().getIdWithoutPostfix()), 0));
        }

        InputListener listener = new InputListener();

        broker.registerInputListener(listener);

        for (RoutingRule rule : rules) {
            Message msg = new Message();
            msg.setReceiverId(rule.getReceiver());
            broker.publishTask(rule.getRoute(), msg);
        }

        for (Identifier adapterId : adapterIds) {
            broker.publishRequest(adapterId, new Message());
        }

        int counterOld = -1;
        int counter;
        while ((counter = listener.counter.get()) != counterOld) {
            synchronized (this) {
                wait(1000l);
            }
            counterOld = counter;
        }

        assertEquals("Not enough input received!", AMOUNT_OF_PEERS, counter);

        System.out.println("remove");

        manager.removeOutputAdapter(statefulAdapterId);

        for (RoutingRule rule : rules) {
            Message msg = new Message();
            msg.setReceiverId(rule.getReceiver());
            broker.publishTask(rule.getRoute(), msg);
        }

        for (Identifier adapterId : adapterIds) {
            broker.publishRequest(adapterId, new Message());
        }

        counterOld = -1;
        int counter2;
        while ((counter2 = listener.counter.get()) != counterOld) {
            synchronized (this) {
                wait(1000l);
            }
            counterOld = counter2;
        }

        assertThat("No more requests handled after removed one (of two) output adapters!", listener.counter.get(), greaterThan(counter));
    }
    
    private class PMCallbackImpl implements PMCallback {
        @Override
        public Collection<PeerAddress> getPeerAddress(Identifier id) {
            List<PeerAddress> addresses = new ArrayList<>();

            addresses.add(new PeerAddress(id, Identifier.adapter("stateless"), Collections.EMPTY_LIST));
            addresses.add(new PeerAddress(id, Identifier.adapter("stateful"), Collections.EMPTY_LIST));

            Collections.shuffle(addresses);

            return addresses;
        }

        @Override
        public boolean authenticate(Identifier peerId, String password) {
            return false;
        }
    }

    private class InputListener implements MessageListener {
        AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void onMessage(Message message) {
            counter.getAndIncrement();
        }
    }
}