package edu.uci.ics.textdb.exp.udf;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.uci.ics.textdb.api.constants.ErrorMessages;
import edu.uci.ics.textdb.api.constants.SchemaConstants;
import edu.uci.ics.textdb.api.constants.DataConstants.TextdbProject;
import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.api.dataflow.ISourceOperator;
import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.exception.TextDBException;
import edu.uci.ics.textdb.api.field.IField;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.schema.Attribute;
import edu.uci.ics.textdb.api.schema.AttributeType;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.span.Span;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.api.utils.Utils;
import edu.uci.ics.textdb.exp.common.AbstractSingleInputOperator;
//import edu.uci.ics.textdb.sandbox.helloworldexamples.DummyHelloWorld;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class UdfOperator implements IOperator, SignalHandler {
    private final static String PYTHON = "python3";
    private final static String inputMmapFile = Utils.getResourcePath("id-text.csv", TextdbProject.TEXTDB_EXP);
    private final static String resultPath = Utils.getResourcePath("result-id-class.csv", TextdbProject.TEXTDB_EXP);
    
    private final static String outputMmapFile = Utils.getResourcePath("outPut.csv", TextdbProject.TEXTDB_EXP);
//    private final static String PYTHONSCRIPT = "'print( \"Print First msg\")'; 'print( \"Print Second msg\")'";
  
    private final static String PYTHONSCRIPT = "";
    
    private final static char SEPARATOR = ',';
    private final static char QUOTECHAR = '"';


    private int cursor = CLOSED;
    private UdfPredicate predicate;
    
    
    private String pythonPID;
    private String javaPID;
    private List<Tuple> tupleBuffer;
    HashMap<String, String> idClassMap;
    
    Tuple currentTuple;
    private IOperator inputOperator;
    
    MappedByteBuffer inputBuffer;
    MappedByteBuffer outputBuffer;
    
    boolean getPythonResult;
    private List<Span> currentSentenceList = new ArrayList<Span>();
    private Schema outputSchema;
    
    
    FileChannel fileChannel;
    FileChannel outputFileChannel;

    public UdfOperator(UdfPredicate predicate) {
        this.predicate = predicate;
        runSignal("USR2");
    }
    
    public void setInputOperator(IOperator inputOperator) {
        this.inputOperator = inputOperator;
    }
    
    @Override
    public void open() throws TextDBException {
        if (cursor != CLOSED) {
            return;
        }
        if (inputOperator == null) {
            throw new DataFlowException(ErrorMessages.INPUT_OPERATOR_NOT_SPECIFIED);
        }
        inputOperator.open();
        Schema inputSchema = inputOperator.getOutputSchema();
        
        // check if the input schema is presented
        if (! inputSchema.containsField(predicate.getInputAttributeName())) {
            throw new RuntimeException(String.format(
                    "input attribute %s is not in the input schema %s",
                    predicate.getInputAttributeName(),
                    inputSchema.getAttributeNames()));
        }
        
        // check if the attribute type is valid
        AttributeType inputAttributeType = 
                inputSchema.getAttribute(predicate.getInputAttributeName()).getAttributeType();
        boolean isValidType = inputAttributeType.equals(AttributeType.STRING) || 
                inputAttributeType.equals(AttributeType.TEXT);
        if (! isValidType) {
            throw new RuntimeException(String.format(
                    "input attribute %s must have type String or Text, its actual type is %s",
                    predicate.getInputAttributeName(),
                    inputAttributeType));
        }
        
        // generate output schema by transforming the input schema
        outputSchema = transformSchema(inputOperator.getOutputSchema());
        
        cursor = OPENED;
    }
    
    /*
     * add a new field to the schema, with name resultAttributeName and type String
     */
    private Schema transformSchema(Schema inputSchema){
        if (inputSchema.containsField(predicate.getResultAttributeName())) {
            throw new RuntimeException(String.format(
                    "result attribute name %s is already in the original schema %s", 
                    predicate.getResultAttributeName(),
                    inputSchema.getAttributeNames()));
        }
        return Utils.addAttributeToSchema(inputSchema, 
                new Attribute(predicate.getResultAttributeName(), AttributeType.STRING));
    }
    
    private String getJavaPID() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        int p = vmName.indexOf("@");
        return vmName.substring(0, p);
    }
/*
    @Override
    public Tuple getNextTuple() throws TextDBException {
        Tuple inputTuple = inputOperator.getNextTuple();
        List<IField> outputFields = new ArrayList<>();
        outputFields.addAll(inputTuple.getFields());
        
        String outputUdf = "We will deal with this text later";
        outputFields.add(new TextField( outputUdf ));
        System.out.print("Doing");
        return new Tuple(outputSchema, outputFields);
    }
    */

    public void executePython() {

        String PYTHONSCRIPT = "print( \"Print test msg\")";
   //     try{
            /*
             *  In order to use the NLTK package to do classification, we start a
             *  new process to run the package, and wait for the result of running
             *  the process as the class label of this text field.
             *  Python call format:
             *      #python3 nltk_sentiment_classify picklePath dataPath resultPath
             * */
            /*
            List<String> args = new ArrayList<String>(
                    Arrays.asList(PYTHON, PYTHONSCRIPT, filePath, resultPath));
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            
            Process p = processBuilder.start();
            p.waitFor();
            
            //Read label result from file generated by Python.
            CSVReader csvReader = new CSVReader(new FileReader(resultPath), SEPARATOR, QUOTECHAR, 1);
            List<String[]> allRows = csvReader.readAll();
               
            idClassMap = new HashMap<String, String>();
            //Read CSV line by line
            for(String[] row : allRows){
                idClassMap.put(row[0], row[1]);
            }
            csvReader.close();
        }catch(Exception e){
            throw new DataFlowException(e.getMessage(), e);
        }
        */
    }
    
    private boolean computeTupleBuffer() {
        tupleBuffer = new ArrayList<Tuple>();
        //write [ID,text] to a CSV file.
        List<String[]> csvData = new ArrayList<>();
        int i = 0;
        int batchSize = 100;
        while (i < batchSize){
            Tuple inputTuple;
            if ((inputTuple = inputOperator.getNextTuple()) != null) {
                tupleBuffer.add(inputTuple);
                String[] idTextPair = new String[2];
                idTextPair[0] = inputTuple.getField(SchemaConstants._ID).getValue().toString();
                idTextPair[1] = inputTuple.<IField>getField(predicate.getInputAttributeName()).getValue().toString();
                csvData.add(idTextPair);
                i++;
            } else {
                break;
            }
        }
        if (tupleBuffer.isEmpty()) {
            return false;
        }
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(inputMmapFile));
            writer.writeAll(csvData);
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new DataFlowException(e.getMessage(), e);
        }
        return true;
    }
    
    @SuppressWarnings("restriction")
    public void runSignal(String signal) {
        Signal.handle(new Signal(signal), (SignalHandler) this);   // kill -12
    }
    
    @Override
    public Tuple getNextTuple() throws TextDBException {
        if (cursor == CLOSED) {
            return null;
        }
        /* to be removed
        if (tupleBuffer == null){
            if (computeTupleBuffer()) {
                executePythonScript(BatchedFiles);
            } else {
                return null;
            }
        }
        return popupOneTuple();
        */
        Tuple inputTuple;
        if ((inputTuple = inputOperator.getNextTuple()) == null) {
            return null;
        }
        //start Python process
        try {
            if (pythonPID == null || pythonPID.isEmpty()) {
                // build mmap and write to input file
                File inputFile = new File(inputMmapFile);
                
                //Delete the file; we will create a new file
                inputFile.delete();
                // Get file channel in readonly mode
                fileChannel = new RandomAccessFile(inputFile, "rw").getChannel();
                
                // Get direct byte buffer access using channel.map() operation,    320*K bytes
                inputBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096 * 8 * 8);
                
                javaPID = getJavaPID();
                inputBuffer.position(0);
                inputBuffer.putInt(Integer.valueOf(javaPID));
                
                //build output Buffer mmap
                File outputFile = new File(outputMmapFile);
                outputFile.delete();
                outputFileChannel = new RandomAccessFile(outputFile, "rw").getChannel();
                
                // Get direct byte buffer access using channel.map() operation,    320*K bytes
                outputBuffer = outputFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096 * 8 * 8);
                
                //Start python process
                startPythonProcess(inputMmapFile);
                
            }
            //write attribute content to input file
            //    0              10               ....
            //    | PID  , length |   text
            String attributeText = inputTuple.<IField>getField(predicate.getInputAttributeName()).getValue().toString();
            int lenAttributeText = attributeText.length();
            inputBuffer.position(10);
            inputBuffer.putInt(lenAttributeText);
            inputBuffer.position(20);
            inputBuffer.put(attributeText.getBytes());
            //notify the python process
            
            
            Signal.handle(new Signal("USR2"), (SignalHandler) this);   // kill -12
            
            if (pythonPID != null) {
                Runtime.getRuntime().exec("kill -SIGUSR2 " + pythonPID);
            }
            //waiting for python to notify result
            for( ; ; ) {
                Thread.sleep(20);
                if (getPythonResult) {
                    System.out.println("running ......"+ javaPID);
                    getPythonResult = false;
                    break;
                }
            }
            
            outputBuffer.position(0);
            if (pythonPID == null || pythonPID.isEmpty()) {
                pythonPID = String.valueOf(outputBuffer.getInt(0));
            }
            //get length of output text;
            int lenOutput = outputBuffer.getInt(1);
            outputBuffer.position(10);
            String textOutPut = "";
            for (int i = 0; i < lenOutput; i++)
            {
                textOutPut += ((char) outputBuffer.get()); //Print the content of file
            }
            System.out.println(textOutPut+"End of output");
            
            //populate tuple
            Tuple outputTuple = inputTuple;
            List<IField> outputFields = new ArrayList<>();
            outputFields.addAll(outputTuple.getFields());
            
//            String className = idClassMap.get(outputTuple.getField(SchemaConstants._ID).getValue().toString());
            outputFields.add(new TextField(textOutPut));
            return new Tuple(outputSchema, outputFields);
            
        }catch(Exception e){
            throw new DataFlowException(e.getMessage(), e);
        }
    }
    
    private int startPythonProcess(String filePath) throws IOException {
        String pythonScriptPath = getPythonScript();
        
        List<String> args = new ArrayList<String>(
                Arrays.asList(PYTHON, pythonScriptPath, filePath, resultPath));
        
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        
        Process p = processBuilder.start();
        return 1;
    }
    
    // Process the data file using NLTK
    private String executePythonScript(String filePath) {
        try{
            /*
             *  In order to use the NLTK package to do classification, we start a
             *  new process to run the package, and wait for the result of running
             *  the process as the class label of this text field.
             *  Python call format:
             *      #python3 nltk_sentiment_classify picklePath dataPath resultPath
             * */
            String pythonScriptPath = getPythonScript();
            
            List<String> args = new ArrayList<String>(
                    Arrays.asList(PYTHON, pythonScriptPath, filePath, resultPath));
            
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            
            Process p = processBuilder.start();
            p.waitFor();
            
            //Read label result from file generated by Python.
            System.out.println("resultpath is: " + resultPath);
            CSVReader csvReader = new CSVReader(new FileReader(resultPath), SEPARATOR, QUOTECHAR, 1);
            List<String[]> allRows = csvReader.readAll();
               
            idClassMap = new HashMap<String, String>();
            //Read CSV line by line
            for(String[] row : allRows){
                idClassMap.put(row[0], row[1]);
            }
            csvReader.close();
            Files.delete(Paths.get(pythonScriptPath));
        }catch(Exception e){
            throw new DataFlowException(e.getMessage(), e);
        }
        return null;
    }
    
    protected String getRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
    private String getPythonScript() {
//      String pyFileName = getRandomString() +".py";
      
      String pyPath = Utils.getResourcePath(getRandomString() +".py", TextdbProject.TEXTDB_EXP);
      List<String> scriptPy = new ArrayList<String>();
      String textScript = "#Python Script3\n";
      
      scriptPy.add("import sys");
      scriptPy.add("import csv");
      
      scriptPy.add("dataFullPathFileName = sys.argv[1]");
      scriptPy.add("resultFullPathFileName = sys.argv[2]");
      scriptPy.add("inputDataMap = {}");
      scriptPy.add("recordLabelMap = {}\n");
      
      scriptPy.add("def main():");
      scriptPy.add("    readData()");
      scriptPy.add("    processText()");
      scriptPy.add("    writeResults()\n");

      scriptPy.add("def writeResults():");
      scriptPy.add("    with open(resultFullPathFileName, 'w', newline='') as csvfile:");
      scriptPy.add("        resultWriter = csv.writer(csvfile, delimiter=',', quotechar='\"', quoting=csv.QUOTE_MINIMAL)");
      
      scriptPy.add("        resultWriter.writerow([\"TupleID\", \"ClassLabel\"])");
      scriptPy.add("        for id, classLabel in recordLabelMap.items():");
      scriptPy.add("            resultWriter.writerow([id, classLabel])\n");

      
      scriptPy.add(this.predicate.getScript());
      
      scriptPy.add("\ndef processText():");
      scriptPy.add("    for key, value in inputDataMap.items():");
//      scriptPy.add("        recordLabelMap[key] = 'result for ID: ' + key\n");
      scriptPy.add("        recordLabelMap[key] = UDF(key, value)\n");
              
      scriptPy.add("def readData():");
      scriptPy.add("    with open(dataFullPathFileName, newline='') as csvfile:");
      scriptPy.add("        dataReader = csv.reader(csvfile, delimiter=',', quotechar='\"')");
      scriptPy.add("        for record in dataReader:");
      scriptPy.add("            inputDataMap[record[0]] = record[1]\n");
                  
      scriptPy.add("if __name__ == \"__main__\":");
      scriptPy.add("    main()\n");
      
      for (String strTmp : scriptPy) {
          textScript += strTmp+"\n";
      }
      System.out.print(textScript);
      
      
//      String tmpScript = "/tmp/tmpscript.py";
      try {
          Files.write(Paths.get(pyPath), textScript.getBytes(), StandardOpenOption.CREATE_NEW);
      }catch(Exception e){
          throw new DataFlowException(e.getMessage(), e);
      }
      return pyPath;
  }
    
    /*
    private String getPythonScript() {
//        String pyFileName = getRandomString() +".py";
        
        String pyPath = Utils.getResourcePath(getRandomString() +".py", TextdbProject.TEXTDB_EXP);
        List<String> scriptPy = new ArrayList<String>();
        String textScript = "#Python Script3\n";
        
        scriptPy.add("import sys");
        scriptPy.add("import csv");
        
        scriptPy.add("dataFullPathFileName = sys.argv[1]");
        scriptPy.add("resultFullPathFileName = sys.argv[2]");
        scriptPy.add("inputDataMap = {}");
        scriptPy.add("recordLabelMap = {}\n");
        
        scriptPy.add("def main():");
        scriptPy.add("    readData()");
        scriptPy.add("    processText()");
        scriptPy.add("    writeResults()\n");

        scriptPy.add("def writeResults():");
        scriptPy.add("    with open(resultFullPathFileName, 'w', newline='') as csvfile:");
        scriptPy.add("        resultWriter = csv.writer(csvfile, delimiter=',', quotechar='\"', quoting=csv.QUOTE_MINIMAL)");
        
        scriptPy.add("        resultWriter.writerow([\"TupleID\", \"ClassLabel\"])");
        scriptPy.add("        for id, classLabel in recordLabelMap.items():");
        scriptPy.add("            resultWriter.writerow([id, classLabel])\n");

        
        scriptPy.add(this.predicate.getScript());
        
        scriptPy.add("\ndef processText():");
        scriptPy.add("    for key, value in inputDataMap.items():");
//        scriptPy.add("        recordLabelMap[key] = 'result for ID: ' + key\n");
        scriptPy.add("        recordLabelMap[key] = UDF(key, value)\n");
                
        scriptPy.add("def readData():");
        scriptPy.add("    with open(dataFullPathFileName, newline='') as csvfile:");
        scriptPy.add("        dataReader = csv.reader(csvfile, delimiter=',', quotechar='\"')");
        scriptPy.add("        for record in dataReader:");
        scriptPy.add("            inputDataMap[record[0]] = record[1]\n");
                    
        scriptPy.add("if __name__ == \"__main__\":");
        scriptPy.add("    main()\n");
        
        for (String strTmp : scriptPy) {
            textScript += strTmp+"\n";
        }
        System.out.print(textScript);
        
        
//        String tmpScript = "/tmp/tmpscript.py";
        try {
            Files.write(Paths.get(pyPath), textScript.getBytes(), StandardOpenOption.CREATE_NEW);
        }catch(Exception e){
            throw new DataFlowException(e.getMessage(), e);
        }
        return pyPath;
    }
    */
    private Tuple popupOneTuple() {
        Tuple outputTuple = tupleBuffer.get(0);
        tupleBuffer.remove(0);
        if (tupleBuffer.isEmpty()) {
            tupleBuffer = null;
        }
        
        List<IField> outputFields = new ArrayList<>();
        outputFields.addAll(outputTuple.getFields());
        
        String className = idClassMap.get(outputTuple.getField(SchemaConstants._ID).getValue().toString());
        outputFields.add(new TextField( className ));
        return new Tuple(outputSchema, outputFields);
    }
    
    @Override
    public void close() throws TextDBException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Schema getOutputSchema() {
        // TODO Auto-generated method stub
        return outputSchema;
    }

    @Override
    public void handle(Signal arg0) {
        // TODO Auto-generated method stub
//        signalCallBack(sn);
        if (arg0.getNumber() == 12) {
            getPythonResult = true;
        }
    }
    
}
