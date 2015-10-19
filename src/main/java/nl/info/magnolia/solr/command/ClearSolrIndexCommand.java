package nl.info.magnolia.solr.command;

import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.search.solrsearchprovider.MagnoliaSolrBridge;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Command that executes a Solr request to remove all documents from the Solr index. Use with care.
 * <p>
 * Not to be confused with {@link info.magnolia.search.solrsearchprovider.logic.commands.CleanSolrIndexCommand} which
 * 'only' cleans up the Solr index.
 */
public class ClearSolrIndexCommand extends MgnlCommand {

	private static final Logger LOG = LoggerFactory.getLogger(ClearSolrIndexCommand.class);

	private MagnoliaSolrBridge magnoliaSolrBridge;

	@Inject
	public ClearSolrIndexCommand(MagnoliaSolrBridge magnoliaSolrBridge) {
		this.magnoliaSolrBridge = magnoliaSolrBridge;
	}

	/**
	 * Sends query to Solr to remove all documents from Solr index.
	 *
	 * See: https://wiki.apache.org/solr/Solrj#Usage
	 *
	 * @param context Magnolia context; not used
	 * @return always true
	 * @throws SolrServerException, IOException in case the delete query could not be sent
	 */
	@Override
	public boolean execute(Context context) throws SolrServerException, IOException {
		SolrServer solrServer = this.magnoliaSolrBridge.getSolrServer();
		LOG.info("Sending request to Solr server to remove all documents in the Solr index.");
		UpdateResponse updateResponse = solrServer.deleteByQuery("*:*");
		LOG.info("Received response from Solr server: {}", updateResponse);

		LOG.info("Sending commit request to Solr server to effectuate latest request.");
		updateResponse = solrServer.commit();
		LOG.info("Received response from Solr server: {}", updateResponse);

		return true;
	}
}
