package fr.inria;

import fr.inria.DataStructure.CallTree;
import fr.inria.Inputs.VisualvmReader;
import junit.framework.TestCase;

import java.io.File;

/**
 * Created by nharrand on 09/03/17.
 */
public class VisualvmReaderTest extends TestCase {
    public void testRead() throws Exception {

        VisualvmReader r = new VisualvmReader();
        CallTree t = r.readFromFile(new File("inputsFiles/trace.json"));
        assertTrue(t.depth == 44);
        System.out.print("c");
    }

}