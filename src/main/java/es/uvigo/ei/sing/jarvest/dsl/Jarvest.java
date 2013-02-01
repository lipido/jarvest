/*

Copyright 2012 Daniel Gonzalez Peña and Óscar González Fernández


This file is part of the jARVEST Project. 

jARVEST Project is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

jARVEST Project is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with jARVEST Project.  If not, see <http://www.gnu.org/licenses/>.
*/
package es.uvigo.ei.sing.jarvest.dsl;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ReflectPermission;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.PropertyPermission;

import org.jruby.CompatVersion;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import org.w3c.dom.Document;

import es.uvigo.ei.sing.jarvest.core.OutputHandler;
import es.uvigo.ei.sing.jarvest.core.Transformer;
import es.uvigo.ei.sing.jarvest.core.Util;
import es.uvigo.ei.sing.jarvest.core.XMLInputOutput;


/**
 * 
 * @author ogf
 *
 */
public class Jarvest {

    private static String extractString(Reader reader) {
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[1024];
        int read = -1;
        try {
            while ((read = reader.read(buffer)) != -1) {
                result.append(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    private static InputStreamReader loadFile(String fileName) {
        return new InputStreamReader(
                Jarvest.class.getResourceAsStream(fileName));
    }

    private static final String TRANSFORMER_RB = extractString(loadFile("transformer.rb"));

    private final ScriptingContainer engine;

    public Jarvest() {
        engine = new ScriptingContainer(LocalContextScope.SINGLETHREAD);
        engine.setCompatVersion(CompatVersion.RUBY1_9);
        engine.runScriptlet(TRANSFORMER_RB);
    }

    public Transformer eval(File file) {
        return eval(file, Charset.forName("utf8"));
    }

    public Transformer eval(File file, Charset charset) {
        try {
            return eval(new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), charset)), file.getName());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Transformer eval(Reader reader) {
        return eval(extractString(reader));
    }

    public Transformer eval(Reader reader, String fileName) {
        return eval(extractString(reader), fileName, null);
    }

    public Transformer eval(Reader reader, String fileName, Integer line) {
        return eval(extractString(reader), fileName, line);
    }

    public Transformer eval(String minilanguageProgram, String fileName,
            Integer lineNumber) {
        return XMLInputOutput.loadTransformer(callScriptFunction(
                Document.class, "get_xml", minilanguageProgram, fileName,
                lineNumber));
    }

    public Transformer eval(String minilanguageProgram) {
        return eval(minilanguageProgram, null, null);
    }

    public String[] exec(String minilanguageProgram, String... input) {
        return Util.runRobot(eval(minilanguageProgram), input);
    }

    public String[] exec(File minilanguageProgram, String... input) {
        return Util.runRobot(eval(minilanguageProgram), input);
    }

    public String[] exec(Reader minilanguageProgram, String... input) {
        return Util.runRobot(eval(minilanguageProgram), input);
    }

    public String[] exec(Reader minilanguageProgram, String name,
            int line, String... input) {
        return Util.runRobot(eval(minilanguageProgram, name, line), input);
    }

    public String[] exec(String minilanguageProgram, OutputHandler handler, String... input) {
        return Util.runRobot(eval(minilanguageProgram), input, handler);
    }

    public String[] exec(File minilanguageProgram, OutputHandler handler, String... input) {
        return Util.runRobot(eval(minilanguageProgram), input, handler);
    }

    public String[] exec(Reader minilanguageProgram, OutputHandler handler, String... input) {
        return Util.runRobot(eval(minilanguageProgram), input, handler);
    }

    public String[] exec(Reader minilanguageProgram, String name,
            int line, OutputHandler handler, String... input) {
        return Util.runRobot(eval(minilanguageProgram, name, line), input, handler);
    }

    
    public String xmlToLanguage(File file) {
        try {
            return xmlToLanguage(new BufferedInputStream(new FileInputStream(
                    file)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String xmlToLanguage(InputStream inputStream) {
        return xmlToLanguage(XMLInputOutput.asDoc(inputStream));
    }

    public String xmlToLanguage(Document document) {
        return callScriptFunction(String.class, "to_minilanguage", document);
    }

    private <T> T callScriptFunction(final Class<T> klass,
            final String function, final Object... parameters) {

        return AccessController.doPrivileged(new PrivilegedAction<T>() {

            @Override
            public T run() {
                return engine.callMethod(null, function, parameters, klass);
            }
        }, sandboxContext());
    }

    private static AccessControlContext CACHED_CONTEXT = null;

    private static AccessControlContext sandboxContext() {
        if (CACHED_CONTEXT != null) {
            return CACHED_CONTEXT;
        }
        CodeSource dummyCodeSource = new CodeSource(null, new Certificate[0]);
        AccessControlContext result = new AccessControlContext(
                new ProtectionDomain[] { new ProtectionDomain(dummyCodeSource,
                        permissionsNeededForJRuby()) });
        return CACHED_CONTEXT = result;
    }

    private static Permissions permissionsNeededForJRuby() {
        Permissions result = new Permissions();
        result.add(new PropertyPermission("jruby.*", "read"));
        result.add(new RuntimePermission("accessDeclaredMembers"));
        result.add(new ReflectPermission("suppressAccessChecks"));
        return result;
    }

    public static void main(String[] args) throws MalformedURLException,
            IOException {
        Jarvest minilanguage = new Jarvest();
        for (String s : minilanguage.exec(
                "url | xpath('//a/@href') | patternMatcher('(http://.*)') ",
                "http://www.google.com")) {
            System.out.println(s);
        }

    }

}
