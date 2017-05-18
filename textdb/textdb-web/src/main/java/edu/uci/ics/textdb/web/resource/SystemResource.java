package edu.uci.ics.textdb.web.resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uci.ics.textdb.exp.planstore.PlanStoreConstants;
import edu.uci.ics.textdb.storage.RelationManager;
import edu.uci.ics.textdb.storage.TableMetadata;
import edu.uci.ics.textdb.web.response.TextdbWebResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource {
	@GET
	@Path("/metadata")
	public TextdbWebResponse getMetadata() throws Exception {
		List<TableMetadata> tableMetadata = RelationManager.getRelationManager().getMetaData();
		tableMetadata = tableMetadata.stream()
		        .filter(metadata -> metadata.getTableName().equalsIgnoreCase(PlanStoreConstants.TABLE_NAME))
		        .collect(Collectors.toList());
		return new TextdbWebResponse(0, new ObjectMapper().writeValueAsString(tableMetadata));
	}
}