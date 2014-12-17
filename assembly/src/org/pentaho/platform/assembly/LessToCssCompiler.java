package org.pentaho.platform.assembly;

import org.apache.tools.ant.DirectoryScanner;
import org.lesscss.*;
import org.apache.tools.ant.Task;

import java.io.File;

public class LessToCssCompiler extends Task {

    private String sourceDirectory;
    private String outputDirectory;

    @Override
    public void execute() {

        System.out.println("sourceDirectory = " + sourceDirectory);
        System.out.println("outputDirectory = " + outputDirectory);

        long start = System.currentTimeMillis();
        LessCompiler lessCompiler = new LessCompiler();

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{"*_ltr.less","*_rtl.less"});
        scanner.setBasedir(sourceDirectory);
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] files = scanner.getIncludedFiles();

        for(String file : files) {
            File currentFile = new File(sourceDirectory,file);
            try {
                System.out.println("currentFile = " + currentFile.getCanonicalPath());
                File outputFile = new File(outputDirectory + System.getProperty("file.separator") + currentFile.getName().replace(".less",".css"));
                System.out.println("outputFile = " + outputFile.getCanonicalPath());
                lessCompiler.compile(currentFile, outputFile);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        long duration = System.currentTimeMillis() - start;
        System.out.println("Done in " + duration + " ms");
    }

    // Setters
    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}