package edu.uci.ics.textdb.exp.source.asterix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uci.ics.textdb.exp.common.PredicateBase;
import edu.uci.ics.textdb.exp.common.PropertyNameConstants;

public class AsterixSourcePredicate extends PredicateBase {
    
    private final String host;
    private final Integer port;
    private final String dataverse;
    private final String dataset;
    private final String field;
    private final String keyword;
    private final Integer limit;
    
    @JsonCreator
    public AsterixSourcePredicate(
            @JsonProperty(value = PropertyNameConstants.ASTERIX_HOST, required = true)
            String host,
            @JsonProperty(value = PropertyNameConstants.ASTERIX_PORT, required = true)
            Integer port,
            @JsonProperty(value = PropertyNameConstants.ASTERIX_DATAVERSE, required = true)
            String dataverse,
            @JsonProperty(value = PropertyNameConstants.ASTERIX_DATASET, required = true)
            String dataset,
            @JsonProperty(value = PropertyNameConstants.ASTERIX_QUERY_FIELD, required = false)
            String field,
            @JsonProperty(value = PropertyNameConstants.KEYWORD_QUERY, required = false)
            String keyword,
            @JsonProperty(value = PropertyNameConstants.LIMIT, required = false)
            Integer limit
            ) {
        this.host = host.trim();
        this.port = port;
        this.dataverse = dataverse.trim();
        this.dataset = dataset.trim();
        if (field == null || field.trim().isEmpty()) {
            this.field = null;
        } else {
            this.field = field.trim();
        }
        if (keyword == null || keyword.trim().isEmpty()) {
            this.keyword = null;
        } else {
            this.keyword = keyword.trim();
        }
        if (limit == null || limit < 0) {
            this.limit = null;
        } else {
            this.limit = limit;
        }
    }
    
    @JsonProperty(value = PropertyNameConstants.ASTERIX_HOST)
    public String getHost() {
        return host;
    }
    
    @JsonProperty(value = PropertyNameConstants.ASTERIX_PORT)
    public Integer getPort() {
        return port;
    }
    
    @JsonProperty(value = PropertyNameConstants.ASTERIX_DATAVERSE)
    public String getDataverse() {
        return dataverse;
    }
    
    @JsonProperty(value = PropertyNameConstants.ASTERIX_DATASET)
    public String getDataset() {
        return dataset;
    }
    
    @JsonProperty(value = PropertyNameConstants.ASTERIX_QUERY_FIELD)
    public String getField() {
        return field;
    }
    
    @JsonProperty(value = PropertyNameConstants.KEYWORD_QUERY)
    public String getKeyword() {
        return keyword;
    }
    
    @JsonProperty(value = PropertyNameConstants.LIMIT)
    public Integer getLimit() {
        return limit;
    }
    
    @Override
    public AsterixSource newOperator() {
        return new AsterixSource(this);
    }
    
}
