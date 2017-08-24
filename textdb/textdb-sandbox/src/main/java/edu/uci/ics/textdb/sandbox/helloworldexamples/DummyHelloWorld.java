package edu.uci.ics.textdb.sandbox.helloworldexamples;

import edu.uci.ics.textdb.api.constants.TestConstants;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.storage.DataWriter;
import edu.uci.ics.textdb.storage.RelationManager;
import edu.uci.ics.textdb.storage.constants.LuceneAnalyzerConstants;

/**
 * Hello world!
 *
 */
public class DummyHelloWorld {
    public static final String LAWDOC_TABLE = "chinalaw";
    
    public static void main(String[] args) {
        // Bad comments
        System.out.println("Hello World!");
        // TOBE deleted
        writeChinaLawTable();
        /*
        while (true) {
            System.out.println("Stop me!");
        }
        */
        
    }
    
    public static void writeChinaLawTable() {
        RelationManager relationManager = RelationManager.getRelationManager();
        // Create the people table and write tuples
        relationManager.createTable(LAWDOC_TABLE, "../index/test_tables/" + LAWDOC_TABLE, 
                ChinaLawConstants.SCHEMA_LAWDOC, LuceneAnalyzerConstants.standardAnalyzerString());
        DataWriter dataWriter = relationManager.getTableDataWriter(LAWDOC_TABLE);
        dataWriter.open();
        for (Tuple tuple : ChinaLawConstants.getSamplePeopleTuples()) {
            dataWriter.insertTuple(tuple);
        }
        dataWriter.close();
    }
}
