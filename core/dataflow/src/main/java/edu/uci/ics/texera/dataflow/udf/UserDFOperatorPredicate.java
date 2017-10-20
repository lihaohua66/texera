package edu.uci.ics.texera.dataflow.udf;

import edu.uci.ics.texera.api.dataflow.IOperator;
import edu.uci.ics.texera.dataflow.common.PredicateBase;

public class UserDFOperatorPredicate extends PredicateBase{
    
    public UserDFOperatorPredicate() {
        
    }
    @Override
    public IOperator newOperator() {
        // TODO Auto-generated method stub
        return new UserDFOperator(this);
    }
    
}
