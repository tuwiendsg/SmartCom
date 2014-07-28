package at.ac.tuwien.dsg.smartcom.messaging.logging.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.Message;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBLoggingDAO implements LoggingDAO {
    private static final Logger log = LoggerFactory.getLogger(MongoDBLoggingDAO.class);
    private static final String LOGGING_COLLECTION = "MESSAGE_LOGGING_COLLECTION";

    private final DBCollection coll;

    public MongoDBLoggingDAO(String host, int port, String database) throws UnknownHostException {
        this(new MongoClient(host, port), database, LOGGING_COLLECTION);
    }

    public MongoDBLoggingDAO(MongoClient client, String database, String collection) {
        coll = client.getDB(database).getCollection(collection);
    }

    @Override
    public void persist(Message message) {
        log.trace("Persisting message to log: {}", message);
        BasicDBObject dbObject = serializeMessage(message);
        log.trace("Created document for message: {}", dbObject);
        coll.insert(dbObject);
    }

    private BasicDBObject serializeMessage(Message message) {
        return new BasicDBObject()
                .append("_id", message.getId().getId())
                .append("type", message.getType())
                .append("subtype", message.getSubtype())
                .append("sender", serializeIdentifier(message.getSenderId()))
                .append("receiver", serializeIdentifier(message.getReceiverId()))
                .append("content", message.getContent())
                .append("conversationId", message.getConversationId())
                .append("ttl", message.getTtl())
                .append("language", message.getLanguage())
                .append("securityToken", message.getSecurityToken()); //TODO should we log the security token too?
    }

    private BasicDBObject serializeIdentifier(Identifier identifier) {
        return new BasicDBObject()
                .append("type", identifier.getType().toString())
                .append("id", identifier.getId());
    }
}