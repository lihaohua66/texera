package edu.uci.ics.textdb.sandbox.helloworldexamples;

import java.io.BufferedReader;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.uci.ics.textdb.api.constants.TestConstants;
import edu.uci.ics.textdb.api.dataflow.IOperator;
import edu.uci.ics.textdb.api.exception.DataFlowException;
import edu.uci.ics.textdb.api.field.IField;
import edu.uci.ics.textdb.api.field.TextField;
import edu.uci.ics.textdb.api.schema.Schema;
import edu.uci.ics.textdb.api.span.Span;
import edu.uci.ics.textdb.api.tuple.Tuple;
import edu.uci.ics.textdb.storage.DataWriter;
import edu.uci.ics.textdb.storage.RelationManager;
import edu.uci.ics.textdb.storage.constants.LuceneAnalyzerConstants;

import sun.misc.Signal;
import sun.misc.SignalHandler;
//import jtux.*;
 
/**
 * Hello world!
 *
 */
public class DummyHelloWorld implements SignalHandler {
    private String name;
    private int size;
    private long semaphore;
    private long mapfile; // File descriptor for mmap file

    static private String pid;
    
    public static final String LAWDOC_TABLE = "chinalaw";
    private static String bigExcelFile = "test.txt";
    
    // varible for tst
    private static String inputFilePath = "/tmp/input.txt";
    private static String outputFilePath = "/tmp/output.txt";
    
    private static String inputFilePath2 = "/tmp/input2.txt";
    private static String outputFilePath2 = "/tmp/output2.txt";
    
    
    private static String inputFilePath3 = "/tmp/input3.txt";
    private static String outputFilePath3 = "/tmp/output3.txt";
    
    private static String textInput = "This is a test";
    private static String PYTHON = "python3";
    private static String PYTHONSCRIPT = "/tmp/pythonscript.py";
    private static String PYTHONSCRIPT2 = "/tmp/pythonscript2.py";
    private static String PYTHONSCRIPT_NOMMAP = "/tmp/pythonscript_nommap.py";

    
    private static boolean getPythonResult = false;
    
    public static boolean resultSig = true;
    public static boolean resultBenchmark = true;
    
    public static final int testRounds = 10000;
    
    public static Process processPython;
    /*
    public DummyHelloWorld(String name, int size, boolean create, int flags, int perms ) {
        this.name = name;
        this.size = size;
        int shm;
        if (create) {
            flags = flags | UConstant.O_CREAT;
            shm = UPosixIPC.shm_open(name, flags, UConstant.O_RDWR);
        } else {
            shm = UPosixIPC.shm_open(name, flags, UConstant.O_RDWR);
        }
        this.mapfile = UPosixIPC.mmap(..., this.size, ..., flags, shm, 0);
        return;
    }
    */
    public DummyHelloWorld() {
//        runSignal(12);
    }
    private void signalCallBack(Signal sn)
    {
        getPythonResult = true;
        System.out.println(sn.getName()+" is recevied.");
    }
    
    public void runSignal(int sn) {
//        System.out.println("got signal handle\n");
        Signal.handle(new Signal("USR2"), (SignalHandler) this);   // kill -12
    }
    
    @Override
    public void handle(Signal sn) {
        
//        signalCallBack(sn);
//        System.out.println("got signal handle\n");
        if (sn.getNumber() == 12) {
            getPythonResult = true;
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        // Bad comments
        // TOBE deleted
 //       writeChinaLawTable();
        /*
        while (true) {
            System.out.println("Stop me!");
        }
        */
        /*
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        int p = vmName.indexOf("@");
        pid = vmName.substring(0, p);
        writeMFile();
        readMFile();
        */

        /*
        DummyHelloWorld tsh = new DummyHelloWorld();
        
//        Signal.handle(new Signal("TERM"), tsh);     // kill -15 common kill
//        Signal.handle(new Signal("INT"), tsh);      // Ctrl+c
        //Signal.handle(new Signal("KILL"), tsh);   // kill -9  no Support
//        Signal.handle(new Signal("USR1"), tsh);   // kill -10
//        Signal.handle(new Signal("USR2"), (SignalHandler) this);   // kill -12
*/
//        singleExec("8521");

        
 //       ooopt();
        

//        javaByteBuffer();
        
        
        long startTimeSigMMap = System.nanoTime();
//        readWriteTestUsingMmapSignal();
        long endTimeSigMMAP = System.nanoTime();
//        processPython.destroy();
        
        System.out.println("start No map test!");
        long startTimeNoMMap = System.nanoTime();
//        readWriteTestNoMmap();
        long endTimeNoMMap = System.nanoTime();
//        processPython.destroy();
       
//        long duration1 = (endTime1 - startTime1);
//        System.out.println("start process2");
        long startTimeBenchmark = System.nanoTime();
        readWriteTestBenchmark();
        long endTimeBenchmark = System.nanoTime();
        long durationBenchmark = (endTimeBenchmark - startTimeBenchmark);
        System.out.print("using no mmap, no signal: " + durationBenchmark/1000000);
        
        System.out.println("using  mmap and signal: " + (endTimeSigMMAP - startTimeSigMMap)/1000000);
        
        System.out.println("using no mmap: "+ (endTimeNoMMap - startTimeNoMMap)/1000000);
//        long divider = 1000000;
//        if (resultSig && resultBenchmark) {
//            System.out.print(duration1/divider + ": " + duration2/divider);
//        }
        
///        processPython.destroy();
    }
    
    public static void ooopt() {
        try {
            
            // create a new RandomAccessFile with filename test
            RandomAccessFile raf = new RandomAccessFile("/tmp/output3.txt", "r");

            // write something in the file
//            raf.writeBytes("Hello World");

            // set the file pointer at 0 position
            raf.seek(0);

            // read the first byte and print it
            System.out.println("" + raf.readLine());

            // set the file pointer at 4rth position
            raf.seek(10);

            // read the first byte and print it
            System.out.println("" + raf.readLine());
            
         } catch (IOException ex) {
            ex.printStackTrace();
         }
    }
    
    private static String getJavaPID() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        int p = vmName.indexOf("@");
        return vmName.substring(0, p);
    }
    
    private static int startPythonProcess() throws IOException {
        String pythonScriptPath = PYTHONSCRIPT2;
        
        List<String> args = new ArrayList<String>(
                Arrays.asList(PYTHON, pythonScriptPath));
        
        ProcessBuilder processBuilder = new ProcessBuilder(args);
//        processBuilder.redirectOutput(new File("/tmp/pythonoutput.txt"));
        processPython = processBuilder.start();
        return 1;
    }
    
    private static int startPythonProcessNoMmap() throws IOException {
        String pythonScriptPath = PYTHONSCRIPT_NOMMAP;
        
        List<String> args = new ArrayList<String>(
                Arrays.asList(PYTHON, pythonScriptPath));
        
        ProcessBuilder processBuilder = new ProcessBuilder(args);
//        processBuilder.redirectOutput(new File("/tmp/pythonoutput.txt"));
        processPython = processBuilder.start();        
        return 1;
    }
    
    public static void singleExec(String pythonPID) throws IOException {
        Runtime.getRuntime().exec("kill -SIGUSR2 " + pythonPID);
    }
    public static void readWriteTestUsingMmapSignal() {
//      Java                Python
        //start python
        // mmap input output File
                            //mmap input output File
        // loop:
            //1. write input file.
            //                      2.read input file
            //                      3.write output file
            //4. read ouput file.
        //close mmap
        //close file
        
        
        String pythonPID = null;
        String javaPID = null;
        MappedByteBuffer inputBuffer = null;
        MappedByteBuffer outputBuffer = null;
        
        FileChannel fileChannel;
        FileChannel outputFileChannel;
        //start Python process
        DummyHelloWorld tsh = new DummyHelloWorld();
        Signal.handle(new Signal("USR2"), tsh);
        try {
            if (pythonPID == null || pythonPID.isEmpty()) {
                // build mmap and write to input file
                File inputFile = new File(inputFilePath);
                
                //Delete the file; we will create a new file
                inputFile.delete();
                // Get file channel in readwrite mode
                fileChannel = new RandomAccessFile(inputFile, "rw").getChannel();
                
                // Get direct byte buffer access using channel.map() operation,    320*K bytes
                inputBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024* 8);

                javaPID = getJavaPID()+"\n";
//                System.out.println("Java PID:" + javaPID);
                inputBuffer.position(0);
                inputBuffer.put(javaPID.getBytes());
                
                //build output Buffer mmap
                File outputFile = new File(outputFilePath);
                outputFile.delete();
                outputFileChannel = new RandomAccessFile(outputFile, "rw").getChannel();
                
                // Get direct byte buffer access using channel.map() operation,    320*K bytes
                outputBuffer = outputFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024 * 8);
                startPythonProcess();
            }
            
            //wait to input data and output result using a signals
            Signal.handle(new Signal("USR2"), tsh);   // kill -12
            
//            System.out.print("enter a loop:\n");
            for (int i = 0; i < testRounds; i++){
                String attributeText = "This is test"+ i +"\n";
                //write attribute content to input file
                //    0              10               ....
                //    | PID  , length |   text
                int lenAttributeText = attributeText.length();
                inputBuffer.position(10);
                inputBuffer.put((String.valueOf(lenAttributeText)+"\n").getBytes());
                inputBuffer.position(20);
                inputBuffer.put((attributeText+"\n").getBytes());
                //notify the python process
                
                if (pythonPID != null) {
                    //do not need to send signal at the first round
                    Runtime.getRuntime().exec("kill -SIGUSR2 "+ pythonPID.trim());
                }
                
                //waiting for python to notify result
                for( ; ; ) {
                    Thread.sleep(2);
                    if (getPythonResult) {
                        getPythonResult = false;
                        break;
                    }
                }
                
                outputBuffer.position(0);
                if (pythonPID == null || pythonPID.isEmpty()) {
                    
                    String tmpPID="";
                    for (int k = 0; k < 10; k++)
                    {
                        char ch;
                        if ((ch = (char) outputBuffer.get()) != '\n') {
                            tmpPID += ch; //Print the content of file
                        }
                    }
                    pythonPID = tmpPID.trim();
//                    System.out.println("pythonPID:" +pythonPID);
                }
                outputBuffer.position(20);
                String textOutPut = "";
//                System.out.println("Python ID is"+pythonPID);
                for (int j = 0; j < 100; j++)
                {
                    char ch = (char) outputBuffer.get();
                    textOutPut += ch;
                    if (ch == '\n') {
                        break;
                    }
                }
                if (textOutPut.equals(attributeText)) {
                    if(i % 100 == 0) {
                        System.out.println("Signal and Map round " + i );//+ ":" + textOutPut);
                    }
                } else {
                    resultSig = false;
                    System.out.println("src::" + attributeText);
                    System.out.println("rst::" + textOutPut);
                }
            }
            
        }catch(Exception e){
            throw new DataFlowException(e.getMessage(), e);
        }
    }
    
    public static void javaByteBuffer() throws IOException {
        File inputFile = new File("/tmp/abc.txt");
        FileChannel fileChannel = null;
        fileChannel = new RandomAccessFile(inputFile, "rw").getChannel();
        ByteBuffer inputBuffer = null;
        inputBuffer = ByteBuffer.allocate(1024 * 8);
        inputBuffer.clear();

        String javaPID = "asdfasfawef\n";

        inputBuffer.position(0);
        inputBuffer.put(javaPID.getBytes());
        inputBuffer.flip();
        fileChannel.write(inputBuffer);
    }
    
    public static void readWriteTestNoMmap() {
//      Java                Python
        //start python
        // mmap input output File
                            //mmap input output File
        // loop:
            //1. write input file.
            //                      2.read input file
            //                      3.write output file
            //4. read ouput file.
        //close mmap
        //close file
        
        
        String pythonPID = null;
        String javaPID = null;
        ByteBuffer inputBuffer = null;
        ByteBuffer outputBuffer = null;
        
        FileChannel fileChannel = null;
        FileChannel outputFileChannel = null;
        
        File inputFile = new File(inputFilePath3);

        //start Python process
        DummyHelloWorld tsh = new DummyHelloWorld();
        Signal.handle(new Signal("USR2"), tsh);
        System.out.println("No map test...........");
        try {
            
            if (pythonPID == null) {
                // build mmap and write to input file
                fileChannel = new RandomAccessFile(inputFile, "rw").getChannel();
                
                inputBuffer = ByteBuffer.allocate(1024 * 8);

                javaPID = getJavaPID()+"\n";

//                inputBuffer.position(0);
                inputBuffer.put(javaPID.getBytes());
                
                //build output Buffer mmap
                File outputFile = new File(outputFilePath3);
                outputFileChannel = new RandomAccessFile(outputFile, "rw").getChannel();
                
                // Get direct byte buffer access using channel.map() operation,    320*K bytes
                outputBuffer = ByteBuffer.allocate(1024 * 8);
                
                //Start python process
                startPythonProcessNoMmap();
                System.out.println("start Python\n");
                
            }
            BufferedReader outputBuff = new BufferedReader(new FileReader(outputFilePath3));
            
            //wait to input data and output result using a signals
            Signal.handle(new Signal("USR2"), tsh);   // kill -12
            
//            System.out.print("enter a loop:\n");
            for (int i = 0; i < testRounds; i++){
                fileChannel = new RandomAccessFile(inputFile, "rw").getChannel();
                fileChannel.truncate(0);
                String attributeText = "This is test"+ i +"\n";
                //write attribute content to input file
                //    0              10               ....
                //    | PID  , length |   text
                int lenAttributeText = attributeText.length();
                inputBuffer.position(10);
                inputBuffer.put((String.valueOf(lenAttributeText)+"\n").getBytes());
                inputBuffer.position(20);
                inputBuffer.put((attributeText+"\n").getBytes());
                
                inputBuffer.flip();
                inputBuffer.clear();

                fileChannel.write(inputBuffer);
                fileChannel.close();
                
                RandomAccessFile raf = new RandomAccessFile(outputFilePath3, "r");
//                fileChannel.close();
                //notify the python process
                
                if (pythonPID != null) {
                    //do not need to send signal at the first round
                    Runtime.getRuntime().exec("kill -SIGUSR2 "+ pythonPID.trim());
                }
                
                //waiting for python to notify result

                for( ; ; ) {
                    if (getPythonResult) {
//                        System.out.println("running ......"+ javaPID);
                        getPythonResult = false;
                        break;
                    }
                    Thread.sleep(2);
                }
                if (pythonPID == null) {
                    raf.seek(0);
                    pythonPID = raf.readLine().trim();
                }
                
                String textOutPut = "";
                raf.seek(20);
                textOutPut = raf.readLine();
//                System.out.print(textOutPut);
                
                if (attributeText.trim().equals(textOutPut)) {
                    if (i % 100 == 0) {
                        System.out.println("Signal round: " + i + textOutPut);
                    }
                } else {
                    resultSig = false;
                    System.out.println("--------------------------------------------结果错误\n");
                    System.out.println("src::" + attributeText);
                    System.out.println("rst::" + textOutPut);
                }
                
            }
            
        }catch(Exception e){
            throw new DataFlowException(e.getMessage(), e);
        }
    }
    
    public static void readWriteTestBenchmark() {
        //     Java                Python
        //loop:
            //1. write input file.
            //2. start python
            //                      3.read input file
            //                      4.write output file
            //                      5.close files
            //                      6.close python
            //7. read ouput file.
        try {
            for (int i=0; i<testRounds; i++) {
                // 1.
                Files.write(Paths.get(inputFilePath2), textInput.getBytes());
                
                //2.              3,4,5,6
                List<String> args = new ArrayList<String>(
                        Arrays.asList(PYTHON, PYTHONSCRIPT));
                ProcessBuilder processBuilder = new ProcessBuilder(args);
                
                Process p = processBuilder.start();
                p.waitFor();
                
                //7.
//                byte[] fileArray;
                String resultStr =  new String(Files.readAllBytes(Paths.get(outputFilePath2)), StandardCharsets.UTF_8);
//                String resultStr = fileArray.toString();
                if (resultStr.equals(textInput) == false) {
                    System.out.println("Result inconsistent!\n");
                    System.out.print(resultStr + "___:___" + textInput);
                    resultBenchmark = false;
                    break;
                }
                if (i % 100 == 0) {
                    System.out.println("benchmark: " + i);
                }
            }
        }catch(Exception e){
            throw new DataFlowException(e.getMessage(), e);
        }
        
    }
    
    public static void readMFile() throws Exception {
        File file = new File("/tmp/hello22.txt");
        
        //Get file channel in readonly mode
        FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel();
         
        //Get direct byte buffer access using channel.map() operation
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
         
        // the buffer now reads the file as if it were loaded in memory.
        System.out.println(buffer.isLoaded());  //prints false
        System.out.println(buffer.capacity());  //Get the size based on content size of file
        System.out.println("start of read");
        //You can read the file from this buffer the way you like.
        String myOutPut = "";
        for (int i = 0; i < buffer.limit(); i++)
        {
            
            myOutPut += ((char) buffer.get()); //Print the content of file
        }
        System.out.println(myOutPut+"End of read");
        buffer.position(0);
        myOutPut = "";
        int lenOutput = buffer.getInt(0);
        int getPid = buffer.getInt(1);
        buffer.position(10);
        for (int i = 0; i < lenOutput; i++)
        {
            myOutPut += ((char) buffer.get()); //Print the content of file
        }
        System.out.println(lenOutput+String.valueOf(getPid) + myOutPut+"End of read");
    }
    public static void writeMFile() throws Exception {
     //   File file = new File(bigExcelFile);
        
        File file = new File("/tmp/hello22.txt");
        //Delete the file; we will create a new file
        file.delete();
 
        // Get file channel in readonly mode
        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
         
        // Get direct byte buffer access using channel.map() operation
        String bufferOutput = "nnnnnnnnnnnnnhowtodoinjava.com\nX0X1howtodoinjava.com\nX0X2howtodoinjava.com\nxxx3howtodoinjava.com\n";
        int lenBuffer = bufferOutput.length();
        String lenStr = String.valueOf(lenBuffer);
        
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, (lenBuffer +10)* 8);
        System.out.println("Length: " +lenBuffer);
        //Write the content using put methods
        buffer.putInt(lenBuffer);
        buffer.putInt(Integer.valueOf(pid));
        buffer.position(10);
        buffer.put(bufferOutput.getBytes());

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
