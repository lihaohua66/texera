package edu.uci.ics.textdb.sandbox.helloworldexamples;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import org.rosuda.JRI.Rengine;

public class HellowR implements SignalHandler {
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        // Create an R vector in the form of a string.
        String javaVector = "c(1,2,3,4,5)";

        // Start Rengine.
        Rengine engine = new Rengine(new String[] { "--no-save" }, false, null);

        // The vector that was created in JAVA context is stored in 'rVector' which is a variable in R context.
        engine.eval("rVector=" + javaVector);

        //Calculate MEAN of vector using R syntax.
        engine.eval("meanVal=mean(rVector)");

        //Retrieve MEAN value
        double mean = engine.eval("meanVal").asDouble();

        //Print output values
        System.out.println("Mean of given vector is=" + mean);

    }

    @Override
    public void handle(Signal arg0) {
        // TODO Auto-generated method stub
        
    }
    
}
