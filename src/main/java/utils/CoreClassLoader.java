package utils;


import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.ProtectionDomain;

public class CoreClassLoader extends URLClassLoader {
    public CoreClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public CoreClassLoader(ClassLoader parent) {
        this(new URL[0], parent);
    }

    public CoreClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
        addURL((URL)null);
    }

    public void addJar(String fileName) throws MalformedURLException {
        addJar(new URL(fileName));
    }

    public void addJar(URL url) {
        addURL(url);
    }

    public Class defineClass0(String name, byte[] b, int off, int len, ProtectionDomain protectionDomain) {
        return defineClass(name, b, off, len, protectionDomain);
    }

}
