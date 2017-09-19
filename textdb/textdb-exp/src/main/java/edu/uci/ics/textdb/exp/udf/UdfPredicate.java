package edu.uci.ics.textdb.exp.udf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.exp.common.PredicateBase;
import edu.uci.ics.textdb.exp.common.PropertyNameConstants;

public class UdfPredicate extends PredicateBase {
    private final String inputAttributeName;
    private final String resultAttributeName;
    private final String udfScript;
    
    @JsonCreator
    public UdfPredicate(
            @JsonProperty(value = PropertyNameConstants.ATTRIBUTE_NAME, required = true)
            String inputAttributeName,
            @JsonProperty(value = PropertyNameConstants.RESULT_ATTRIBUTE_NAME, required = true)
            String resultAttributeName,
            @JsonProperty(value = PropertyNameConstants.UDF_SCRIPT, required = true)
            String udfScript) {
        this.inputAttributeName = inputAttributeName;
        this.resultAttributeName = resultAttributeName;
        this.udfScript = udfScript;
    };
    
    @JsonProperty(PropertyNameConstants.ATTRIBUTE_NAME)
    public String getInputAttributeName() {
        return this.inputAttributeName;
    }
    
    @JsonProperty(PropertyNameConstants.RESULT_ATTRIBUTE_NAME)
    public String getResultAttributeName() {
        return this.resultAttributeName;
    }
    
    @JsonProperty(PropertyNameConstants.UDF_SCRIPT)
    public String getScript() {
        return this.udfScript;
    }
    
    @Override
    public UdfOperator newOperator() {
        // TODO Auto-generated method stub
        return new UdfOperator(this);
    }
}
