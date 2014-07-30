package at.ac.tuwien.dsg.smartcom.demo.adapters;

import at.ac.tuwien.dsg.smartcom.adapter.InputPullAdapter;
import at.ac.tuwien.dsg.smartcom.adapter.exception.AdapterException;
import at.ac.tuwien.dsg.smartcom.model.Message;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class DropboxInputAdapter implements InputPullAdapter {
    private static final Logger log = LoggerFactory.getLogger(RESTOutputAdapter.class);

    private final DbxClient client;
    private final String folder;
    private final String filePrefix;
    private final Message returnMessage;

    public DropboxInputAdapter(String accessToken, String folder, String file, Message returnMessage) {
        this.folder = folder;
        this.filePrefix = file;
        this.returnMessage = returnMessage;
        DbxRequestConfig config = new DbxRequestConfig("SmartCom Adapter", Locale.getDefault().toString());
        client = new DbxClient(config, accessToken);

        try {
            log.debug("Linked account: " + client.getAccountInfo().displayName);
        } catch (DbxException e) {
            log.error("Error while accessing account information of dropbox client!", e);
        }
    }

    @Override
    public Message pull() throws AdapterException {
        try {
            log.debug("Searching for file {}", filePrefix);
            if (log.isTraceEnabled()) {
                DbxEntry.WithChildren listing = client.getMetadataWithChildren("/" + folder);

                log.trace("Files in the root path (actual file might be in subdirectory):");
                for (DbxEntry child : listing.children) {
                    log.trace("	" + child.name + ": " + child.toString());
                }
            }

            DbxEntry found = null;
            List<DbxEntry> dbxEntries = client.searchFileAndFolderNames("/" + folder, filePrefix);
            if (dbxEntries.size()>0) {
                found = dbxEntries.get(0);
            }

            if (found == null) {
                return null;
            } else {
                log.debug("Found requested file {}", found.name);
                return returnMessage;
            }
        } catch (DbxException e) {
            log.error("Error while accessing dropbox account", e);
        }

        return null;
    }
}
