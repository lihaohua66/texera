package edu.uci.ics.texera.dataflow.udf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uci.ics.texera.api.constants.ErrorMessages;
import edu.uci.ics.texera.api.dataflow.IOperator;
import edu.uci.ics.texera.api.exception.DataflowException;
import edu.uci.ics.texera.api.exception.TexeraException;
import edu.uci.ics.texera.api.schema.AttributeType;
import edu.uci.ics.texera.api.schema.Schema;
import edu.uci.ics.texera.api.tuple.Tuple;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class UserDFOperator implements IOperator, SignalHandler {
    private UserDFOperatorPredicate predicate;
    private int cursor = CLOSED;
    private IOperator inputOperator;
    private Schema outputSchema;
    
    /* 2 input signal:
     *      SIG_NULL= "": result NULL
     *      SIG_LEN = length of text
     * 3 ouput signal:
     *      SIG_NULL= "": result NULL
     *      SIG_WAIT= Character.unsigned:   need to wait for next
     *      SIG_LEN = length of text
     */
    int PID_POSITION = 0;
    int SIG_POSITION = 10;
    int JSON_POSTION = 20;
    
    String SIG_NULL = "0";
    String SIG_WAIT = "w";
    String SIG_LEN;
    
    int OSSIG = 12;
    
    // varible for tst
    private String inputFilePath = "/tmp/input.txt";
    private String outputFilePath = "/tmp/output.txt";
    
    private String PYTHON = "python3";
    private String PYTHONSCRIPT2 = "/home/sjwn/pythonscript_textd_udf/pythonscript2.py";

    
    private boolean getPythonResult = false;
    
    public boolean resultSig = true;
        
    public Process processPython;
    
    public Tuple outputTuple;
    
    
    private String pythonPID;
    private String javaPID;
    private MappedByteBuffer inputBuffer;
    private MappedByteBuffer outputBuffer;
    
    private FileChannel inputFileChannel;
    private FileChannel outputFileChannel;
    
    
    public UserDFOperator(UserDFOperatorPredicate predicate) {
        this.predicate = predicate;
        this.pythonPID = null;
        this.javaPID = null;
        this.inputBuffer = null;
        this.outputBuffer = null;
    }

    @Override
    public void open() throws TexeraException {
        // TODO Auto-generated method stub
        if (cursor != CLOSED) {
            return;
        }
        if (inputOperator == null) {
            throw new DataflowException(ErrorMessages.INPUT_OPERATOR_NOT_SPECIFIED);
        }
        inputOperator.open();
        Schema inputSchema = inputOperator.getOutputSchema();
        Signal.handle(new Signal("USR2"), this);
        handshake();
        /*
        // check if the input schema is presented
        if (! inputSchema.containsAttribute(predicate.getInputAttributeName())) {
            throw new TexeraException(String.format(
                    "input attribute %s is not in the input schema %s",
                    predicate.getInputAttributeName(),
                    inputSchema.getAttributeNames()));
        }
        
        // check if the attribute type is valid
        AttributeType inputAttributeType = 
                inputSchema.getAttribute(predicate.getInputAttributeName()).getType();
        boolean isValidType = inputAttributeType.equals(AttributeType.STRING) || 
                inputAttributeType.equals(AttributeType.TEXT);
        if (! isValidType) {
            throw new TexeraException(String.format(
                    "input attribute %s must have type String or Text, its actual type is %s",
                    predicate.getInputAttributeName(),
                    inputAttributeType));
        }
        
        // generate output schema by transforming the input schema
        outputSchema = transformSchema(inputOperator.getOutputSchema());
        */
        
        
        outputSchema = inputSchema;
        cursor = OPENED;
    }
    
    private String getJavaPID() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        int p = vmName.indexOf("@");
        return vmName.substring(0, p);
    }
    
    @SuppressWarnings("restriction")
    private boolean handshake() {
        
        try {
            File inputFile = new File(inputFilePath);
            
            //Delete the file; we will create a new file
            inputFile.delete();
            // Get file channel in readwrite mode
            inputFileChannel = new RandomAccessFile(inputFile, "rw").getChannel();
            
            // Get direct byte buffer access using channel.map() operation,    320*K bytes
            inputBuffer = inputFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024* 8);
            
            inputBuffer.position(0);
            inputBuffer.put((getJavaPID()+"\n").getBytes());
            
            //build output Buffer mmap
            File outputFile = new File(outputFilePath);
            outputFile.delete();
            outputFileChannel = new RandomAccessFile(outputFile, "rw").getChannel();
            
            // Get direct byte buffer access using channel.map() operation,    320*K bytes
            outputBuffer = outputFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024 * 8);
            startPythonProcess();
            while ( true ) {
                Thread.sleep(200);
                if (getPythonResult) {
                    getPythonResult = false;
                    break;
                }
            }
            pythonPID = getPythonPID();
            
            if (pythonPID == null || pythonPID.length() == 0) {
                return false;
            }
            System.out.println("hands shaking over: get python PID: " + pythonPID);
        } catch (Exception e) {
            
        }
        return true;
    }
    
    
    /* Return SIG-WAIT, SIG_NULL or SIG_LEN
     * */
    public String getPythonPID() {
        return readStringFromMMap(outputBuffer, PID_POSITION);
    }
    
    public String getSignal() {
        String strSig = "";
        outputBuffer.position(SIG_POSITION);
        while (true)
        {
            char ch;
            if ((ch = (char) outputBuffer.get()) == Character.UNASSIGNED) {
                break;
            } else if (ch == 'w') {
                strSig = SIG_WAIT;
                break;
            }
            strSig += ch;
        }
        if(strSig.startsWith("0")) {
            strSig =  SIG_NULL;
        }
        //return the length
        return strSig;
    }
    
    public void putSignal(String signal) {
        inputBuffer.position(SIG_POSITION);
        inputBuffer.put((signal+ "\n").getBytes());
        inputBuffer.putChar((char) Character.UNASSIGNED);
    }
    
    public void putStringToInputBuffer(String string) {
        inputBuffer.position(JSON_POSTION);
        inputBuffer.put((string).getBytes());
        inputBuffer.putChar((char) Character.UNASSIGNED);
    }
    
    public String getJsonFromOutputBuffer() {
        return readStringFromMMap(outputBuffer, JSON_POSTION);
    }
 // read a piece of buffer ended with unsigend character
    public String readStringFromMMap(MappedByteBuffer outputBuffer, int startPos) {
        String str = "";
        outputBuffer.position(startPos);
        while (true)
        {
            char ch;
            if ((ch = (char) outputBuffer.get()) == Character.UNASSIGNED) {
                break;
            }
            str += ch;
        }
        if (str.trim().length() == 0) {
            return null;
        }
        return str.trim();
    }
    
    private int startPythonProcess() throws IOException {
        String pythonScriptPath = PYTHONSCRIPT2;
        
        List<String> args = new ArrayList<String>(
                Arrays.asList(PYTHON, pythonScriptPath));
        
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectOutput(new File("/tmp/pythonoutput.txt"));
        processPython = processBuilder.start();
        return 1;
    }
    
    
    public static void singleExec(String pythonPID) throws IOException {
        Runtime.getRuntime().exec("kill -SIGUSR2 " + pythonPID);
    }
    public void setInputOperator(IOperator operator) {
        if (cursor != CLOSED) {
            throw new TexeraException("Cannot link this operator to another operator after the operator is opened");
        }
        this.inputOperator = operator;
    }

    @Override
    public Tuple getNextTuple() throws TexeraException {
        // TODO Auto-generated method stub
        if (cursor == CLOSED) {
            return null;
        }
        try {
            Tuple inputTuple = inputOperator.getNextTuple();
            //write attribute content to input file
            //    0          10               .20
            //    | JavaPID  |length or signal | Tuple Json  text
            if (inputTuple == null) {
                putSignal(SIG_NULL);
            } else {
                String inputTupleText = new ObjectMapper().writeValueAsString(inputTuple);
                putSignal(String.valueOf((new ObjectMapper().writeValueAsString(inputTuple)).length()));
                putStringToInputBuffer(inputTupleText);
            }
            // notify python data is ready
            Runtime.getRuntime().exec("kill -SIGUSR2 "+ pythonPID.trim());
            for( ; ; ) {
                Thread.sleep(200);
                if (getPythonResult) {
                    getPythonResult = false;
                    break;
                }
            }
            //Output from buffer
            
            String strLenSig = getSignal();
            
            if(strLenSig == SIG_NULL) {
                processPython.destroy();
                return null;
            }
            
            String outputTupleJsonStr = getJsonFromOutputBuffer();
            
            outputTuple = new ObjectMapper().readValue(outputTupleJsonStr.trim(), Tuple.class);
            outputSchema = outputTuple.getSchema();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return outputTuple;
    }

    @Override
    public void close() throws TexeraException {
        // TODO Auto-generated method stub
        if (cursor == CLOSED) {
            return;
        }
        if (inputOperator != null) {
            inputOperator.close();
        }
        cursor = CLOSED;
        return;
    }

    @Override
    public Schema getOutputSchema() {
        // TODO Auto-generated method stub
        return outputSchema;
    }

    @SuppressWarnings("restriction")
    @Override
    public void handle(Signal arg0) {
        // TODO Auto-generated method stub
        if (arg0.getNumber() == OSSIG) {
            getPythonResult = true;
        }
    }
    
}
