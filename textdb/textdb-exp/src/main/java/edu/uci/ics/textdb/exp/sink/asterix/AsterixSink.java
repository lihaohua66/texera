package edu.uci.ics.textdb.exp.sink.asterix;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.api.dataflow.ISink;
import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.exp.source.asterix.AsterixSource;

public class AsterixSink implements ISink {
    
    private final AsterixSinkPredicate predicate;
    private IOperator inputOperator;
    private int cursor = CLOSED;
    
    private String asterixAddress;
    
    public AsterixSink(AsterixSinkPredicate predicate) {
        this.predicate = predicate;
    }
    
    public void setInputOperator(IOperator inputOperator) {
        this.inputOperator = inputOperator;
    }

    @Override
    public void open() throws TextDBException {
        if (cursor == OPENED) {
            return;
        }
        try {
            inputOperator.open();
            this.asterixAddress = "http://" + predicate.getHost() + ":" + predicate.getPort() + 
                    "/query/service";
            
            // send the query to prepare the dataset for future insertion
            String prepareDatasetStatement = prepareAsterixDataset();
            HttpResponse<JsonNode> jsonResponse = Unirest.post(asterixAddress)
                    .queryString("statement", prepareDatasetStatement)
                    .field("mode", "immediate")
                    .asJson();
            
            // if status is not 200 OK, throw exception
            if (jsonResponse.getStatus() != 200) {
                throw new DataFlowException("Send create dataset query to asterix failed: " + 
                        "error status: " + jsonResponse.getStatusText() + ", " + 
                        "error body: " + jsonResponse.getBody().toString());
            }
            cursor = OPENED;
        } catch (UnirestException e) {
            throw new DataFlowException(e);
        }
    }

    private String prepareAsterixDataset() {
        String queryString = "";
        // use dataverse
        queryString += "use " + predicate.getDataverse() + ";\n";
        // drop dataset ds if exists
        queryString += "drop dataset " + predicate.getDataset() + " if exists" + ";\n";
        // create dataset ds(typeTweet) primary key id
        queryString += "create dataset " + predicate.getDataset() + "(typeTweet) " + "primary key id" + ";\n";
        // create index text_idx on ds_tweet("text") type fulltext;
        queryString += "create index text_idx on " + predicate.getDataset() + "(text) " + "type fulltext" + ";\n";
        return queryString;
    }
    
    @Override
    public void processTuples() throws TextDBException {
        String queryString = prepareAsterixDataset();
        queryString += "use " + predicate.getDataverse() + ";\n";
        queryString += "insert into " + predicate.getDataset() + "([";
        Tuple tuple;
        while ((tuple = inputOperator.getNextTuple()) != null) {
            if (! tuple.getSchema().containsField(AsterixSource.RAW_DATA)) {
                throw new DataFlowException("tuple doesn't have rawData field");
            }
            String rawData = tuple.getField(AsterixSource.RAW_DATA).getValue().toString();
            String rawDataAdm = transformRawData(rawData);
            System.out.println(rawDataAdm);
            queryString += rawDataAdm + ",";
        }
        queryString += "]);\n";
        
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(asterixAddress)
                    .queryString("statement", queryString)
                    .field("mode", "immediate")
                    .asJson();
            // if status is not 200 OK, throw exception
            if (jsonResponse.getStatus() != 200) {
                throw new DataFlowException("Send create dataset query to asterix failed: " + 
                        "error status: " + jsonResponse.getStatusText() + ", " + 
                        "error body: " + jsonResponse.getBody().toString());
            }
        } catch (UnirestException e) {
            throw new DataFlowException(e);
        }
    }
    
    private String transformRawData(String rawData) {
        String[] splitList = rawData.split("create_at\":\""); 
        String rawDataAdm = "";
        rawDataAdm += splitList[0]  
                + "create_at\":datetime(\"" + splitList[1].substring(0, 25) + ")"+splitList[1].substring(25)
                + "create_at\":date(\"" + splitList[2].substring(0, 11) + ")" + splitList[2].substring(11); 
        return rawDataAdm;
    }

    @Override
    public void close() throws TextDBException {
        if (cursor == CLOSED) {
            return;
        }
        inputOperator.close();
        cursor = CLOSED;
    }
    
    @Override
    public Schema getOutputSchema() {
        throw new DataFlowException("no output schema for asterix sink");
    }

}
