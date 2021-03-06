package fr.inria;

import fr.inria.DataStructure.*;
import fr.inria.DataStructure.Compare.CompareExecution;
import fr.inria.DataStructure.Compare.CompareTraces;
import fr.inria.IOs.*;
import fr.inria.View.WebStrataView;
import org.apache.commons.io.FileUtils;
import processing.core.PApplet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * Hello world!
 *
 */
public class App
{

    public boolean save;

    @Parameter(names = {"--help", "-h"}, help = true, description = "Display this message.")
    private boolean help;
    @Parameter(names = {"--properties", "-p"}, description = "File containing the trace to be cleaned.")
    private String propFile;
    @Parameter(names = {"--properties-2", "-q"}, description = "File containing the trace to be cleaned.")
    private String propFile2;
    @Parameter(names = {"--default-level", "-d"}, description = "Default package (for classes not begining by any known prefixes.")
    private int defaultLevel = 0;
    @Parameter(names = {"--packages", "-k"}, description = "List of packages")
    private String packages = "";
    @Parameter(names = {"--traces", "-t"}, description = "File containing traces")
    private String traces;
    @Parameter(names = {"--traces-2", "-u"}, description = "File containing traces")
    private String traces2;
    @Parameter(names = {"--x86-log", "-x"}, description = "File containing compilation logs")
    private String x86logs;
    @Parameter(names = {"--jars", "-j"}, description = "List of jar")
    private String jars;
    @Parameter(names = {"--syscalls", "-s"}, description = "File containing sys calls")
    private String sysCalls;
    @Parameter(names = {"--outputDir", "-o"}, description = "File containing sys calls")
    private String outputDir = "";
    @Parameter(names = {"--excludes", "-e"}, description = "File containing sys calls")
    private String excludes = "";
    @Parameter(names = {"--name", "-n"}, description = "File containing sys calls")
    private String name = "CallTree";
    @Parameter(names = {"--entry", "-i"}, description = "First method to display")
    private String entry = "";
    @Parameter(names = {"--from", "-f"}, description = "Start analysis from a specific node")
    private String from;
    @Parameter(names = {"--cut", "-r"}, description = "Cut nodes starting by ")
    private String cut;
    @Parameter(names = {"--command", "-c"}, description = "Command")
    private String command = "CallTree";
    @Parameter(names = {"--screen-size", "-z"}, description = "Screen size. Default 1200px")
    private int screenSize = 1200;


    public static void printUsage(JCommander jcom) {
        jcom.usage();
    }


    public static void printCmdList() {
        System.out.println(" -- Command list --");
        System.out.println("\t callTree: Generates an image of the call tree contained in the traces.");
        System.out.println("\t callTreeGif: Generates an image of the call tree contained in the traces.");
        System.out.println("\t compCallTree: Generates an image of the comparison of call trees contained in the traces.");
        System.out.println("\t webReport: Generates a full web report.");
        System.out.println("\t compTraces: Compute the distance between two traces");
        System.out.println("\t byteCodeTree: Generate an image of the bytecode executed");
        System.out.println("\t x86Tree: Generate an image of the assembly executed");
    }

    public static void callTree() {

    }


    public static Execution readProperties(App app) {
        Execution e = new Execution();

        String trace, jars, x86log, syscalls, outputDir, sScreenSize, sSave;
        e.name = app.name;
        e.screenSize = app.screenSize;
        System.out.println("Screen size: " + e.screenSize);

        e.save = true;

        String packages[] = app.packages.split(",");
        e.nbLevel = packages.length;
        e.defaultLevel = app.defaultLevel;

        e.packages = new HashMap<>();

        for(int i =0; i < e.nbLevel; i++) {
            Set<String> s = new HashSet<>();
            s.add(packages[i]);
            e.packages.put(new Integer(i), s);
        }
        jars = app.jars;
        if(jars != null) {
            e.jars = new HashSet<>();
            for(String s : jars.replace(" ", "").split(",")) {
                try {
                    e.jars.add(new JarFile(s));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        String ex = app.excludes;
        e.excludes = new HashSet<>();
        if(ex != null && !ex.equals("")) {

            for(String s : ex.replace(" ", "").split(",")) {
                e.excludes.add(s);
            }
        }

        trace = app.traces;
        if(trace != null) e.trace = new File(trace);
        x86log = app.x86logs;
        if(x86log != null) e.x86log = new File(x86log);
        syscalls = app.sysCalls;
        if(syscalls != null) e.syscalls = new File(syscalls);
        outputDir = app.outputDir;
        if(outputDir != null) e.outputDir = new File(outputDir);

        return e;
    }

    public static void main( String ... args ) {
        App app = new App();
        JCommander jcom = new JCommander(app,args);

        if(app.help || (app.propFile == null && app.traces == null)) {
            printUsage(jcom);
        } else if (app.command != null && !app.command.equals("")){
            switch (app.command) {
                case "callTree":
                    if(app.propFile != null) Context.currentExec = PropertiesReader.readProperties(new File(app.propFile));
                    else Context.currentExec = readProperties(app);
                    PApplet.main("fr.inria.View.Legend");
                    if(Context.currentExec.branch) {
                        PApplet.main("fr.inria.View.BranchView");
                    } else {
                        PApplet.main("fr.inria.View.CallTreeAlterView");
                    }
                    break;
                case "byteCodeTree":
                    if(app.propFile != null) Context.currentExec = PropertiesReader.readProperties(new File(app.propFile));
                    else Context.currentExec = readProperties(app);

                    PApplet.main("fr.inria.View.ByteCodeView");
                    break;

                case "x86Tree":
                    if(app.propFile != null) Context.currentExec = PropertiesReader.readProperties(new File(app.propFile));
                    else Context.currentExec = readProperties(app);
                    PApplet.main("fr.inria.View.X86View");
                    break;

                case "compCallTree":

                    generateComparaison(new File(app.propFile), new File(app.propFile2));
                    break;
                case "webReport":
                    generateCodeStrata(new File(app.propFile));
                    break;
                case "compTraces":
                    if (app.traces != null && app.traces2 != null) {
                        Set<String> toRemove = new HashSet<>();
                        if(app.cut != null && !app.cut.equals("")) toRemove.add(app.cut);

                        new CompareTraces(new File(app.traces),
                                new File(app.traces2),
                                new SimpleReader(),
                                toRemove,
                                app.from
                        );
                    }
                    break;
                case "callTreeGif":
                    if (app.traces != null) {
                        Context.currentExec = readProperties(app);
                        PApplet.main("fr.inria.View.CallTreeGifView");
                    }
                    break;
                default:
                    printUsage(jcom);
                    printCmdList();
            }
        } else if (app.traces != null && app.traces2 != null) {

            Context.currentCompareExec = new CompareExecution();
            Context.currentCompareExec.e1 = Execution.defaultJunitExecution();
            Context.currentCompareExec.e1.trace = new File(app.traces);
            Context.currentCompareExec.e2 = Execution.defaultJunitExecution();
            Context.currentCompareExec.e2.trace = new File(app.traces2);
            /*Context.currentExec = Context.currentCompareExec.e1;
            PApplet.main("fr.inria.View.CallTreeAlterView");
            Context.currentExec = Context.currentCompareExec.e2;
            PApplet.main("fr.inria.View.CallTreeAlterView");*/
            //PApplet.main("fr.inria.View.Compare.CompareCallTreeAlterView");
            PApplet.main("fr.inria.View.Compare.CompareCallTreeYAView");
        } else if (app.traces != null) {
            Context.currentExec = readProperties(app);
            PApplet.main("fr.inria.View.CallTreeAlterView");
        } else if (app.propFile != null && app.propFile2 != null) {
            generateComparaison(new File(app.propFile), new File(app.propFile2));
        } else if (app.propFile != null) {
            generateCodeStrata(new File(app.propFile));
        }








        //generateCodeStrata(new File(args[0]));

        /*Context.currentCompareExec = new CompareExecution();
        //Context.currentCompareExec.e1 = PropertiesReader.readProperties(new File("inputsFiles/Base32/Base32Test.properties"));
        //Context.currentCompareExec.e2 = PropertiesReader.readProperties(new File("inputsFiles/Base32/Base32Test_addMI.properties"));
        Context.currentCompareExec.e1 = PropertiesReader.readProperties(new File("inputsFiles/Sort/QuickSortTest.properties"));
        Context.currentCompareExec.e2 = PropertiesReader.readProperties(new File("inputsFiles/Sort/QuickSortTest_arraylist.properties"));
        Context.currentExec = Context.currentCompareExec.e1;
        PApplet.main("fr.inria.View.CallTreeAlterView");
        Context.currentExec = Context.currentCompareExec.e2;*/
        //PApplet.main("fr.inria.View.CallTreeAlterView");
        //PApplet.main("fr.inria.View.Compare.CompareCallTreeAlterView");

        //export();

        //generateCodeStrata(new File("inputsFiles/Base32/Base32InputStreamTest.properties"));
        //generateCodeStrata(new File("inputsFiles/Base32/Base32InputStreamTest_addMI.properties"));

        //generateCodeStrata(new File("inputsFiles/Base32/Base32OutputStreamTest.properties"));
        //generateCodeStrata(new File("inputsFiles/Base32/Base32OutputStreamTest_addMI.properties"));

        //generateCodeStrata(new File("inputsFiles/Base32/Base32Test.properties"));
        //generateCodeStrata(new File("inputsFiles/Base32/Base32Test_addMI.properties"));


        //generateCodeStrata(new File("inputsFiles/Sort/QuickSortTest.properties"));
        //generateCodeStrata(new File("inputsFiles/Sort/QuickSortTest_full.properties"));
        //generateCodeStrata(new File("inputsFiles/Sort/BoGoSortTest.properties"));
        //generateCodeStrata(new File("inputsFiles/Sort/BoGoSortTest_full.properties"));

        //Context.currentExec = PropertiesReader.readProperties(new File("inputsFiles/Sort/QuickSortTest.properties"));
        //Context.currentExec = PropertiesReader.readProperties(new File("inputsFiles/Sort/QuickSortTest_addMI.properties"));
        //PApplet.main("fr.inria.View.CallTreeAlterView");
        //Context.currentExec = PropertiesReader.readProperties(new File("inputsFiles/Sort/QuickSortTest.properties"));
        //PApplet.main("fr.inria.View.CallTreeAlterView");


        /*Context.currentExec = PropertiesReader.readProperties(new File("inputsFiles/simple-java-editor/simple-java-editor.properties"));
        ExecutionWritter w = new ExecutionWritter();
        w.toJSON();*/
        //Context.currentExec = PropertiesReader.readProperties(new File("inputsFiles/simple-java-editor/simple-java-editor.properties"));
        //Context.currentExec = PropertiesReader.readProperties(new File("inputsFiles/Base32/Base32Test_addMI.properties"));
        //PApplet.main("fr.inria.View.CallTreeAlterView");
        //PApplet.main("fr.inria.View.ByteCodeAlterView");
        //PApplet.main("fr.inria.View.x86AlterView");
    }

    public static void flatTrace() {}

    public static void generateComparaison(File prop1, File prop2) {
        Context.currentCompareExec = new CompareExecution();
        Context.currentCompareExec.e1 = PropertiesReader.readProperties(prop1);
        Context.currentCompareExec.e2 = PropertiesReader.readProperties(prop2);
        /*Context.currentExec = Context.currentCompareExec.e1;
        PApplet.main("fr.inria.View.CallTreeAlterView");
        Context.currentExec = Context.currentCompareExec.e2;
        PApplet.main("fr.inria.View.CallTreeAlterView");*/
        //PApplet.main("fr.inria.View.Compare.CompareCallTreeAlterView");
        PApplet.main("fr.inria.View.Compare.CompareCallTreeYAView");
    }

    public static void generateCodeStrata(File properties) {
        Context.currentExec = PropertiesReader.readProperties(properties);

        try {
            FileUtils.copyFile(new File(App.class.getClassLoader().getResource("question-mark.png").getFile()),
                    new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_default.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int strataNb = 8;
        Map<Integer, File> strataImages = new HashMap<>();
        strataImages.put(7, new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_default.png"));
        //strataImages.put(7, new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_demo.gif"));
        strataImages.put(6, new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_calltree_app.png"));
        strataImages.put(5, new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_calltree.png"));
        strataImages.put(4, new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_bytecode.png"));
        strataImages.put(3, new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_syscall.png"));
        strataImages.put(2, new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_default.png"));
        strataImages.put(1, new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_default.png"));
        strataImages.put(0, new File(Context.currentExec.outputDir, "img/" + Context.currentExec.name + "_x86.png"));

        Map<Integer, String> strataDesc = new HashMap<>();
        strataDesc.put(7,"\t\t\t\t\t\t\t<h3>Application</h3>\n" +
                "\t\t\t\t\t\t\t<p>\n" +
                "\t\t\t\t\t\t\t\tApplication as seen by the user.\n" +
                "\t\t\t\t\t\t\t</p>");
        strataDesc.put(6,"\t\t\t\t\t\t\t<h3>Application calls</h3>\n" +
                "\t\t\t\t\t\t\t<p>\n" +
                "\t\t\t\t\t\t\t\tEach rectangle represents the invocation of a java method written by the application author(s). White links represent the way each method call sub methods.\n" +
                "\t\t\t\t\t\t\t</p>");
        strataDesc.put(5,"\t\t\t\t\t\t\t<h3>Application and libraries calls</h3>\n" +
                "\t\t\t\t\t\t\t<p>\n" +
                "\t\t\t\t\t\t\t\tEach rectangle represents the invocation of a java method, in blue are methods of the application, whereas in green are methods from the library javafx (handling the graphical interface). White links represents the way each method call sub methods.\n" +
                "\t\t\t\t\t\t\t</p>");
        strataDesc.put(4,"\t\t\t\t\t\t\t<h3>Byte code</h3>\n" +
                "\t\t\t\t\t\t\t<p>\n" +
                "\t\t\t\t\t\t\t\tThe jvm (java virtual machine) doesn't read java code. Instead this code is transformed in a more compact form called bytecode. Each color segment represents a bytecode instruction.\n" +
                "\t\t\t\t\t\t\t</p>");
        strataDesc.put(3,"\t\t\t\t\t\t\t<h3>System calls</h3>\n" +
                "\t\t\t\t\t\t\t<p>\n" +
                "\t\t\t\t\t\t\t\tEach square represent a system call. A system call is an operation handled by the operating system.\n" +
                "\t\t\t\t\t\t\t</p>");
        strataDesc.put(2, "");
        strataDesc.put(1, "");
        strataDesc.put(0, "\t\t\t\t\t\t\t<h3>x86</h3>\n" +
                "\t\t\t\t\t\t\t<p>\n" +
                "\t\t\t\t\t\t\t\tIn order to executed by micro processor, a program must be converted in a serie of instructions that the processor understand. A common instruction set nodays is x86.\n" +
                "\t\t\t\t\t\t\t\tEach color segment represents an x86 instruction.\n" +
                "\t\t\t\t\t\t\t</p>");

        if(!Context.currentExec.outputDir.exists()) Context.currentExec.outputDir.mkdirs();

        WebStrataView web = new WebStrataView(Context.currentExec, strataNb, strataImages, strataDesc);
        web.generateWeb();

        PApplet.main("fr.inria.View.CallTreeAlterView");
        PApplet.main("fr.inria.View.CallTreeView");
        PApplet.main("fr.inria.View.ByteCodeView");
        PApplet.main("fr.inria.View.SysCallView");
        PApplet.main("fr.inria.View.X86View");
        PApplet.main("fr.inria.View.MiniMapView");
    }

    public static void export() {
        JSONReader r = new SimpleReader();
        CallTree t1 = r.readFromFile(Context.currentCompareExec.e1.trace);
        CallTree t2 = r.readFromFile(Context.currentCompareExec.e2.trace);
        RawWriter w = new RawWriter();
        w.writeInFile(t1, new File("QuickSort.raw"), false);
        w.writeInFile(t2, new File("QuickSort_addMI.raw"), false);

    }
}
