package edu.uci.ics.textdb.exp.sink.table;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;

import edu.uci.ics.textdb.api.constants.SchemaConstants;
import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.api.dataflow.ISink;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.field.IField;
import edu.uci.ics.textdb.api.field.StringField;
import edu.uci.ics.textdb.api.schema.Attribute;
import edu.uci.ics.textdb.api.schema.AttributeType;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.api.utils.Utils;
import edu.uci.ics.textdb.exp.twitter.TwitterConverterConstants;
import edu.uci.ics.textdb.storage.DataReader;
import edu.uci.ics.textdb.storage.DataWriter;
import edu.uci.ics.textdb.storage.RelationManager;

public class TableSink implements ISink {
    
    private final TableSinkPredicate predicate;
    private IOperator inputOperator;
    private Schema tableSchema;
    private int cursor = CLOSED;
        
    public TableSink(TableSinkPredicate predicate) {
        this.predicate = predicate;
    }
    
    public void setInputOperator(IOperator inputOperator) {
        this.inputOperator = inputOperator;
    }

    @Override
    public void open() throws TextDBException {
        if (cursor != CLOSED) {
            return;
        }
        inputOperator.open();
        setupSchema();
        
        String tableName = predicate.getTableName();
        RelationManager relationManager = RelationManager.getRelationManager();
        if (relationManager.checkTableExistence(tableName)) {
//            relationManager.deleteTable(tableName);
        } else {
            relationManager.createTable(
                    tableName, Paths.get(Utils.getTextdbHomePath(), "index", tableName).toString(), 
                    tableSchema, predicate.getLuceneAnalyzerStr());
        }

    }

    @Override
    public void processTuples() throws TextDBException {
        RelationManager relationManager = RelationManager.getRelationManager();
        DataWriter dataWriter = relationManager.getTableDataWriter(predicate.getTableName());
        
        dataWriter.open();
        Tuple tuple;
        while ((tuple = inputOperator.getNextTuple()) != null) {
            if (tuple.getSchema().containsField(TwitterConverterConstants.TWEET_ID)) {
                StringField tweetIDField = tuple.getField(TwitterConverterConstants.TWEET_ID);
                DataReader dataReader = relationManager.getTableDataReader(predicate.getTableName(), 
                        new TermQuery(new Term(TwitterConverterConstants.TWEET_ID, tweetIDField.getValue())));
                dataReader.open();
                if (dataReader.getNextTuple() != null) {
                    continue;
                }
                dataReader.close();
            }
            dataWriter.insertTuple(processOneTuple(tuple));
        }
        dataWriter.close();
    }
    
    private Tuple processOneTuple(Tuple inputTuple) {
        return new Tuple(tableSchema, tableSchema.getAttributeNames().stream()
                .map(attrName -> inputTuple.getField(attrName, IField.class))
                .collect(Collectors.toList()));
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
        return tableSchema;
    }
    
    private void setupSchema() {
        List<Attribute> inputAttributes = inputOperator.getOutputSchema().getAttributes();
        this.tableSchema = new Schema(inputAttributes.stream()
                .filter(attr -> ! attr.getAttributeName().equals(SchemaConstants.PAYLOAD))
                .filter(attr -> ! attr.getAttributeType().equals(AttributeType.LIST))
                .collect(Collectors.toList()));
    }

}
