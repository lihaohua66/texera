package edu.uci.ics.textdb.exp.twitter;

import edu.uci.ics.textdb.api.constants.ErrorMessages;
import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.tuple.Tuple;

public class TwitterConverter implements IOperator {
    
    private final TwitterConverterPredicate predicate;
    
    private IOperator inputOperator;
    private Schema outputSchema;
    private int cursor = CLOSED;
    
    public TwitterConverter(TwitterConverterPredicate predicate) {
        this.predicate = predicate;
    }

    @Override
    public void open() throws TextDBException {
        if (cursor == OPENED) {
            return;
        }
        if (inputOperator == null) {
            throw new DataFlowException(ErrorMessages.INPUT_OPERATOR_NOT_SPECIFIED);
        }
        inputOperator.open();
        cursor = OPENED;
    }

    @Override
    public Tuple getNextTuple() throws TextDBException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() throws TextDBException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Schema getOutputSchema() {
        // TODO Auto-generated method stub
        return this.outputSchema;
    }

}
