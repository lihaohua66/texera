package edu.uci.ics.textdb.exp.sink.table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uci.ics.textdb.exp.common.PredicateBase;
import edu.uci.ics.textdb.exp.common.PropertyNameConstants;
import edu.uci.ics.textdb.storage.constants.LuceneAnalyzerConstants;

public class TableSinkPredicate extends PredicateBase {
    
    private final String tableName;
    private final String luceneAnalyzerStr;
    
    @JsonCreator
    public TableSinkPredicate(
            @JsonProperty(value = PropertyNameConstants.TABLE_NAME, required = true)
            String tableName,
            @JsonProperty(value = PropertyNameConstants.LUCENE_ANALYZER_STRING, required = false)
            String luceneAnalyzerStr
            ) {
        this.tableName = tableName.trim();
        if (luceneAnalyzerStr == null || luceneAnalyzerStr.trim().isEmpty()) {
            this.luceneAnalyzerStr = LuceneAnalyzerConstants.standardAnalyzerString();
        } else {
            this.luceneAnalyzerStr = luceneAnalyzerStr;
        }
    }
    
    @JsonProperty(value = PropertyNameConstants.TABLE_NAME)
    public String getTableName() {
        return tableName;
    }
    
    @JsonProperty(value = PropertyNameConstants.LUCENE_ANALYZER_STRING)
    public String getLuceneAnalyzerStr() {
        return luceneAnalyzerStr;
    }
    
    @Override
    public TableSink newOperator() {
        return new TableSink(this);
    }
    
}
