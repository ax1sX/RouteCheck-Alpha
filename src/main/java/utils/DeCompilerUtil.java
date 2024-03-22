package utils;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.io.File;
import java.util.*;

public class DeCompilerUtil extends ConsoleDecompiler {


    protected DeCompilerUtil(File destination, Map<String, Object> options, IFernflowerLogger logger) {
        super(destination, options, logger);
    }

    public static void deCompile(String[] args){
        if (args.length < 2) {
            System.out.println("Usage: java -jar fernflower.jar [-<option>=<value>]* [<source>]+ <destination>\nExample: java -jar fernflower.jar -dgs=true c:\\my\\source\\ c:\\my.jar d:\\decompiled\\");
        } else {
            Map<String, Object> mapOptions = new HashMap();
            List<File> sources = new ArrayList();
            List<File> libraries = new ArrayList();
            boolean isOption = true;

            for(int i = 0; i < args.length - 1; ++i) {
                String arg = args[i];
                if (isOption && arg.length() > 5 && arg.charAt(0) == '-' && arg.charAt(4) == '=') {
                    String value = arg.substring(5);
                    if ("true".equalsIgnoreCase(value)) {
                        value = "1";
                    } else if ("false".equalsIgnoreCase(value)) {
                        value = "0";
                    }

                    mapOptions.put(arg.substring(1, 4), value);
                } else {
                    isOption = false;
                    if (arg.startsWith("-e=")) {
                        addPath(libraries, arg.substring(3));
                    } else {
                        addPath(sources, arg);
                    }
                }
            }

            if (sources.isEmpty()) {
                System.out.println("error: no sources given");
            } else {
                File destination = new File(args[args.length - 1]);
                if (!destination.isDirectory()) {
                    System.out.println("error: destination '" + destination + "' is not a directory");
                } else {
                    PrintStreamLogger logger = new PrintStreamLogger(System.out);
                    ConsoleDecompiler decompiler = new DeCompilerUtil(destination, mapOptions, logger);
                    Iterator var8 = sources.iterator();
                    File library;
                    while(var8.hasNext()) {
                        library = (File)var8.next();
                        decompiler.addSource(library);
                    }
                    var8 = libraries.iterator();
                    while(var8.hasNext()) {
                        library = (File)var8.next();
                        decompiler.addLibrary(library);
                    }
                    decompiler.decompileContext();
                }
            }
        }
    }

    private static void addPath(List<? super File> list, String path) {
        File file = new File(path);
        if (file.exists()) {
            list.add(file);
        } else {
            System.out.println("warn: missing '" + path + "', ignored");
        }

    }
}

