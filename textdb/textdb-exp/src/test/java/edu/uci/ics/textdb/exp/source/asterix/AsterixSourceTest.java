package edu.uci.ics.textdb.exp.source.asterix;

import java.util.List;

import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.exp.sink.tuple.TupleSink;

public class AsterixSourceTest {
    
    public static void test1() {
        AsterixSourcePredicate predicate = new AsterixSourcePredicate(
                "textdb.ics.uci.edu", 19002, "twitter", "ds_tweet", "text", "zika", 2);
        AsterixSource asterixSource = predicate.newOperator();
        
        TupleSink tupleSink = new TupleSink();
        tupleSink.setInputOperator(asterixSource);
        
        tupleSink.open();
        List<Tuple> results = tupleSink.collectAllTuples();
        tupleSink.close();
        
        System.out.println(results);
        
    }

}
