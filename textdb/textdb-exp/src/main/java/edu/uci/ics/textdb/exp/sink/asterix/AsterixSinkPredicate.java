package edu.uci.ics.textdb.exp.sink.asterix;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uci.ics.textdb.exp.common.PredicateBase;
import edu.uci.ics.textdb.exp.common.PropertyNameConstants;

public class AsterixSinkPredicate extends PredicateBase {
    
    private final String host;
    private final Integer port;
    private final String dataverse;
    private final String dataset;
    
    @JsonCreator
    public AsterixSinkPredicate(
            @JsonProperty(value = PropertyNameConstants.ASTERIX_HOST, required = true)
            String host,
            @JsonProperty(value = PropertyNameConstants.ASTERIX_PORT, required = true)
            Integer port,
            @JsonProperty(value = PropertyNameConstants.ASTERIX_DATAVERSE, required = true)
            String dataverse,
            @JsonProperty(value = PropertyNameConstants.ASTERIX_DATASET, required = true)
            String dataset
            ) {
        this.host = host.trim();
        this.port = port;
        this.dataverse = dataverse.trim();
        this.dataset = dataset.trim();
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

    @Override
    public AsterixSink newOperator() {
        return new AsterixSink(this);
    }

}
