package edu.uci.ics.texera.dataflow.udf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.uci.ics.texera.api.constants.SchemaConstants;
import edu.uci.ics.texera.api.constants.test.TestConstants;
import edu.uci.ics.texera.api.dataflow.IOperator;
import edu.uci.ics.texera.api.exception.TexeraException;
import edu.uci.ics.texera.api.field.ListField;
import edu.uci.ics.texera.api.field.TextField;
import edu.uci.ics.texera.api.schema.AttributeType;
import edu.uci.ics.texera.api.schema.Schema;
import edu.uci.ics.texera.api.span.Span;
import edu.uci.ics.texera.api.tuple.Tuple;
import edu.uci.ics.texera.api.utils.TestUtils;
import edu.uci.ics.texera.dataflow.sampler.Sampler;
import edu.uci.ics.texera.dataflow.sampler.SamplerPredicate;
import edu.uci.ics.texera.dataflow.sampler.SamplerPredicate.SampleType;
import edu.uci.ics.texera.dataflow.source.scan.ScanBasedSourceOperator;
import edu.uci.ics.texera.dataflow.source.scan.ScanSourcePredicate;
import edu.uci.ics.texera.dataflow.source.tuple.TupleSourceOperator;
import edu.uci.ics.texera.dataflow.utils.DataflowUtils;
import edu.uci.ics.texera.storage.DataWriter;
import edu.uci.ics.texera.storage.RelationManager;
import edu.uci.ics.texera.storage.constants.LuceneAnalyzerConstants;

/**
 * @author Qinhua Huang
 */

public class UserDFOperatorTest {
	public static final String SAMPLER_TABLE = "sampler_test";
    private static int indexSize;
    @BeforeClass
    public static void setUp() throws TexeraException {
        RelationManager relationManager = RelationManager.getInstance();
        // Create the people table and write tuples
        
        RelationManager.getInstance().deleteTable(SAMPLER_TABLE);
        relationManager.createTable(SAMPLER_TABLE, TestUtils.getDefaultTestIndex().resolve(SAMPLER_TABLE), 
                TestConstants.SCHEMA_PEOPLE, LuceneAnalyzerConstants.standardAnalyzerString());
        DataWriter dataWriter = relationManager.getTableDataWriter(SAMPLER_TABLE);
        dataWriter.open();
        indexSize = 0;
        for (Tuple tuple : TestConstants.getSamplePeopleTuples()) {
            dataWriter.insertTuple(tuple);
            indexSize ++;
        }
        dataWriter.close();
    }
    
    @AfterClass
    public static void cleanUp() throws TexeraException {
        RelationManager.getInstance().deleteTable(SAMPLER_TABLE);
    }
    
    public static List<Tuple> computeSampleResults(String tableName, int k,
            SampleType sampleType) throws TexeraException {
        
        ScanBasedSourceOperator scanSource = new ScanBasedSourceOperator(new ScanSourcePredicate(tableName));
        Sampler tupleSampler = new Sampler(new SamplerPredicate(k, sampleType));
        tupleSampler.setInputOperator(scanSource);
        
        List<Tuple> results = new ArrayList<>();
        Tuple tuple;
        
        tupleSampler.open();
        while((tuple = tupleSampler.getNextTuple()) != null) {
            results.add(tuple);
        }
        tupleSampler.close();
        return results;
    }
    
    /*
     * To test if all the sampled tuples are in the sampler table.
     */
    public static boolean containedInSamplerTable(List<Tuple> sampleList) throws TexeraException {
        ScanBasedSourceOperator scanSource = 
                new ScanBasedSourceOperator(new ScanSourcePredicate(SAMPLER_TABLE));
        
        scanSource.open();
        Tuple nextTuple = null;
        List<Tuple> returnedTuples = new ArrayList<Tuple>();
        while ((nextTuple = scanSource.getNextTuple()) != null) {
            returnedTuples.add(nextTuple);
        }
        scanSource.close();
        boolean contains = TestUtils.containsAll(returnedTuples, sampleList);
        return contains;
    }
    
    /* 
     * To test if the sampled tuples are equal to the first K tuples of the sampler table 
     * in both the order and content.
     */
    public static boolean matchSamplerTable(List<Tuple> sampleList) throws TexeraException {
        ScanBasedSourceOperator scanSource = 
                new ScanBasedSourceOperator(new ScanSourcePredicate(SAMPLER_TABLE));
        
        scanSource.open();
        ListIterator<Tuple> iter = null;
        iter = sampleList.listIterator();
        while (iter.hasNext()) {
            Tuple nextTableTuple = scanSource.getNextTuple();
            Tuple nextSampledTuple = iter.next();
            
            if (!nextSampledTuple.equals(nextTableTuple)) {
                scanSource.close();
                return false;
            }
        }
        scanSource.close();
        return true;
    }
    
    /*
     * Sample 0 tuple in FIRST_K_ARRIVAL mode
     */
    @Test(expected = TexeraException.class)
    public void test1() throws TexeraException {
        computeSampleResults(SAMPLER_TABLE,0, SampleType.FIRST_K_ARRIVAL);
    }
    
    /*
     * FIRST_K_ARRIVAL mode: sample the first tuple.
     */
    @Test
    public void test2() throws TexeraException {
        List<Tuple> results = computeSampleResults(SAMPLER_TABLE,1, SampleType.FIRST_K_ARRIVAL);
        Assert.assertEquals(results.size(), 1);
        Assert.assertTrue(matchSamplerTable(results));
    }
    
    /*
     * FIRST_K_ARRIVAL mode: Sample all the tuples.
     */
    @Test
    public void test3() throws TexeraException {
        List<Tuple> results = computeSampleResults(SAMPLER_TABLE,indexSize, SampleType.FIRST_K_ARRIVAL);
        Assert.assertEquals(results.size(), indexSize);
        Assert.assertTrue(matchSamplerTable(results));
    }
	/*
    private Tuple testTuple1;    
    private Tuple testTuple2;    
    private Tuple testTuple3;
    
    private Schema inputSchema;
    private IOperator inputOperator;
    
    @BeforeClass
    public void setUp() throws TexeraException {
    	testTuple1 = new Tuple.Builder()
                .add("content", AttributeType.TEXT, new TextField("test1") )
                .add(SchemaConstants.PAYLOAD_ATTRIBUTE, new ListField<Span>(
                        DataflowUtils.generatePayload("content", "test", LuceneAnalyzerConstants.getStandardAnalyzer())))
                .build();
        
        testTuple2 = new Tuple.Builder()
                .add("content", AttributeType.TEXT, new TextField("test2") )
                .add(SchemaConstants.PAYLOAD_ATTRIBUTE, new ListField<Span>(
                        DataflowUtils.generatePayload("content", "test", LuceneAnalyzerConstants.getStandardAnalyzer())))
                .build();
        
        testTuple3 = new Tuple.Builder()
                .add( "content", AttributeType.TEXT, new TextField("test3") )
                .add(SchemaConstants.PAYLOAD_ATTRIBUTE, new ListField<Span>(
                        DataflowUtils.generatePayload("content", "test", LuceneAnalyzerConstants.getStandardAnalyzer())))
                .build();
        
        inputSchema = testTuple1.getSchema();
    	inputOperator = new TupleSourceOperator(Arrays.asList(testTuple1, testTuple2, testTuple3), inputSchema);
    }
    
    @AfterClass
    public static void cleanUp() throws TexeraException {
    	
    }
    
    @Test
    public void test1() throws TexeraException {
    	
    	UserDFOperator userDFOperator = new UserDFOperator(new UserDFOperatorPredicate());
        userDFOperator.setInputOperator(inputOperator);
        
        userDFOperator.open();
        Tuple tuple = null;
        int i = 0;
        while ((tuple = userDFOperator.getNextTuple()) != null) {
            System.out.println(i++);
            System.out.println(tuple.toString());
        }
        userDFOperator.close();
        
        Assert.assertEquals(true, true);
    }
*/

}
