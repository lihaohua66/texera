package edu.uci.ics.textdb.exp.sink.asterix;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.api.dataflow.ISink;
import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.field.ListField;
import edu.uci.ics.textdb.api.schema.AttributeType;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.span.Span;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.exp.source.asterix.AsterixSource;
import edu.uci.ics.textdb.exp.twitter.TwitterConverterConstants;

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
                        "query is: " + prepareDatasetStatement + ", " +
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
        queryString += "create type typeTweetMoney if not exists as open{\n" + 
                "    create_at : datetime,\n" + 
                "    id: int64,\n" + 
                "    `text`: string,\n" + 
                "    retweet_count : int64,\n" + 
                "    lang : string,\n" + 
                "    is_retweet: boolean,\n" + 
                "    user : typeUser,\n" + 
                "    geo_tag: typeGeoTag\n" + 
                "}; ";
        // create dataset ds(typeTweet) primary key id
        queryString += "create dataset " + predicate.getDataset() + "(typeTweetMoney) " + "primary key id" + ";\n";
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
            
            queryString += addAttrIntoAdm(rawDataAdm, tuple) + ",";
        }
        queryString += "]);\n";
        
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post(asterixAddress)
                    .queryString("statement", queryString)
                    .field("mode", "immediate")
                    .asJson();
            // if status is not 200 OK, throw exception
            if (jsonResponse.getStatus() != 200) {
                throw new DataFlowException("Send insert data query to asterix failed: " + 
                        "insert query is : \n" + queryString + ",\n" + 
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
        
        rawDataAdm.replace("[", "{{");
        rawDataAdm.replace("]", "}}");
        return rawDataAdm;
    }
    
    @SuppressWarnings("unchecked")
    private String addAttrIntoAdm(String admData, Tuple tuple) {

        List<String> newAttributes = tuple.getSchema().getAttributes().stream()
                .filter(attr -> ! attr.getAttributeType().equals(AttributeType.LIST))
                .filter(attr -> ! attr.getAttributeType().equals(AttributeType._ID_TYPE))
                .filter(attr -> ! attr.getAttributeName().equals(AsterixSource.RAW_DATA))
                .filter(attr -> ! TwitterConverterConstants.additionalAttributes.contains(attr))
                .map(attr -> attr.getAttributeName())
                .collect(Collectors.toList());
        
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        for (String newAttr : newAttributes) {
            objectNode.set(newAttr, JsonNodeFactory.instance.pojoNode(tuple.getField(newAttr).getValue()));
        }
        
        // add "money" attribute if it's present
        String MONEY_FIELD = "money";
        if (tuple.getSchema().containsField(MONEY_FIELD) 
               && tuple.getSchema().getAttribute(MONEY_FIELD).getAttributeType().equals(AttributeType.LIST)) {
            int totalMoney = 0;
            ListField<Span> moneyField = (ListField<Span>) tuple.getField(MONEY_FIELD);
            for (Span span : moneyField.getValue()) {
                String value = span.getValue();
                try {
                    totalMoney += Double.valueOf(Double.parseDouble(value.substring(1).trim())).intValue();
                } catch (NumberFormatException e) {
                    // log and do nothing
                    System.out.println("cannot parse money: " + e.getMessage());
                }
            }
            objectNode.put(MONEY_FIELD, totalMoney);
        }
        
        String newDataStr = objectNode.toString();
        // replace the trailing "}" with ","
        newDataStr = newDataStr.substring(0, newDataStr.length() - 1) + ",";
        // remove the front "{"
        admData = admData.substring(1);
        
        return newDataStr + admData;
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
