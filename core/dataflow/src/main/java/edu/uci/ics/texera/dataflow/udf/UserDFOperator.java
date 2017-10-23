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
import edu.uci.ics.texera.api.constants.DataConstants.TexeraProject;
import edu.uci.ics.texera.api.dataflow.IOperator;
import edu.uci.ics.texera.api.dataflow.ISourceOperator;
import edu.uci.ics.texera.api.exception.DataflowException;
import edu.uci.ics.texera.api.exception.TexeraException;
import edu.uci.ics.texera.api.schema.AttributeType;
import edu.uci.ics.texera.api.schema.Schema;
import edu.uci.ics.texera.api.tuple.Tuple;
import edu.uci.ics.texera.api.utils.Utils;
import edu.uci.ics.texera.dataflow.common.AbstractSingleInputOperator;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * @author Qinhua Huang
 */

public class UserDFOperator extends AbstractSingleInputOperator implements SignalHandler, ISourceOperator{
    private UserDFOperatorPredicate predicate;
    private Schema outputSchema;
    
    /* 2 input signal:
     *      SIG_NULL= "": result NULL
     *      SIG_LEN = length of text
     * 3 ouput signal:
     *      SIG_NULL= "": result NULL
     *      SIG_WAIT= Character.unsigned:   need to wait for next
     *      SIG_LEN = length of text
     */
    // Signal write to buffer
    int POSITION_PID = 0;
    int POSITION_SIG = 10;
    int POSITION_JSON = 20;
    
    String SIG_NULL = "0\n";
    String SIG_WAIT = "w";
    String SIG_LEN;
    
    //Signal used between processes
    int IPC_SIG = 12;
    
    // Named buffer in file system
//    private String inputFilePath = "/tmp/input.txt";
//    private String outputFilePath = "/tmp/output.txt";
    
    private String inputFilePath = Utils.getResourcePath("input_java_python.txt", TexeraProject.TEXERA_DATAFLOW).toString();
    private String outputFilePath = Utils.getResourcePath("output_java_python.txt", TexeraProject.TEXERA_DATAFLOW).toString();
    private String pythonDebugOutputFilePath = Utils.getResourcePath("debug_output_python.txt", TexeraProject.TEXERA_DATAFLOW).toString();
    final int mmapBufferSize = 1024* 8;
    
    private String PYTHON = "python3";
    private String PYTHONSCRIPT = Utils.getResourcePath("udf_operator.py", TexeraProject.TEXERA_DATAFLOW).toString();
    private boolean getPythonResult = false;
    public Process processPython;
    private String pythonPID;
    public boolean resultSig = true;
    
    public Tuple outputTuple;
    
    private MappedByteBuffer inputBuffer;
    private MappedByteBuffer outputBuffer;
    
    private FileChannel inputFileChannel;
    private FileChannel outputFileChannel;
    
    public UserDFOperator(UserDFOperatorPredicate predicate) {
        this.predicate = predicate;
        this.pythonPID = null;
        this.inputBuffer = null;
        this.outputBuffer = null;
    }
    
    private String getJavaPID() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        int p = vmName.indexOf("@");
        return vmName.substring(0, p);
    }
    
    @SuppressWarnings({ "restriction", "resource" })
    private boolean handShake() {
        // This function will initiate the communication between Java and the launched process.
        try {
            File inputFile = new File(inputFilePath);
            
            //Delete the file; we will create a new file
            inputFile.delete();
            // Get file channel in readwrite mode
            inputFileChannel = new RandomAccessFile(inputFile, "rw").getChannel();
            
            // Get direct byte buffer access using channel.map() operation,    320*K bytes
            inputBuffer = inputFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, mmapBufferSize);
            
            inputBuffer.position(0);
            inputBuffer.put((getJavaPID()+"\n").getBytes());
            
            //build output Buffer mmap
            File outputFile = new File(outputFilePath);
            outputFile.delete();
            outputFileChannel = new RandomAccessFile(outputFile, "rw").getChannel();
            
            outputBuffer = outputFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, mmapBufferSize);
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
        } catch (Exception e) {
            throw new TexeraException("Hands shaking Failed!");

        }
        return true;
    }
    
    
    /* Return SIG-WAIT, SIG_NULL or SIG_LEN
     * */
    public String getPythonPID() {
        return readStringFromMMap(outputBuffer, POSITION_PID);
    }
    
    public String getSignal() {
        String strSig = "";
        outputBuffer.position(POSITION_SIG);
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
        //return the Tuple Json length
        return strSig;
    }
    
    public void putSignalIntoInputBuffer(String signal) {
        inputBuffer.position(POSITION_SIG);
        inputBuffer.put((signal+ "\n").getBytes());
        inputBuffer.putChar((char) Character.UNASSIGNED);
    }
    
    public void putStringIntoInputBuffer(String string) {
        inputBuffer.position(POSITION_JSON);
        inputBuffer.put((string).getBytes());
        inputBuffer.putChar((char) Character.UNASSIGNED);
    }
    
    public String getJsonFromOutputBuffer() {
        return readStringFromMMap(outputBuffer, POSITION_JSON);
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
        String pythonScriptPath = PYTHONSCRIPT;
        
        List<String> args = new ArrayList<String>(
                Arrays.asList(PYTHON, pythonScriptPath, inputFilePath, outputFilePath));
        
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        processBuilder.redirectOutput(new File(pythonDebugOutputFilePath));
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
        if (arg0.getNumber() == IPC_SIG) {
            getPythonResult = true;
        }
    }
    
    @SuppressWarnings("restriction")
    @Override
    protected void setUp() throws TexeraException {
        // TODO Auto-generated method stub
        Schema inputSchema = inputOperator.getOutputSchema();
        Signal.handle(new Signal("USR2"), this);
        handShake();
        
        outputSchema = inputSchema;
    }

    @Override
    protected Tuple computeNextMatchingTuple() throws TexeraException {
        // TODO Auto-generated method stub
        
        try {
            Tuple inputTuple = inputOperator.getNextTuple();
            //write attribute content to input file
            //    0          10               .20
            //    | JavaPID  |length or signal | Tuple Json  text
            if (inputTuple == null) {
                putSignalIntoInputBuffer(SIG_NULL);
            } else {
                String inputTupleText = new ObjectMapper().writeValueAsString(inputTuple);
                putSignalIntoInputBuffer(String.valueOf((new ObjectMapper().writeValueAsString(inputTuple)).length()));
                putStringIntoInputBuffer(inputTupleText);
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
            
            if (strLenSig == SIG_WAIT) {
                this.getNextTuple();
            }
            
            if(strLenSig == SIG_NULL) {
                processPython.destroy();
                return null;
            }
            
            String outputTupleJsonStr = getJsonFromOutputBuffer();
            
            outputTuple = new ObjectMapper().readValue(outputTupleJsonStr.trim(), Tuple.class);
            outputSchema = outputTuple.getSchema();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new TexeraException("MMap Operation Failed!");
        }
        return outputTuple;
    }

    @Override
    public Tuple processOneInputTuple(Tuple inputTuple) throws TexeraException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void cleanUp() throws TexeraException {
        // TODO Auto-generated method stub
        
    }
    
}
