/*
 * Created on Dec 22, 2004
 *
 */
package org.culpan.herosim.inputchar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;
import org.jdom.Element;

/**
 * @author CulpanH
 *  
 */
public class InputChar {
    protected final static Logger logger = Logger.getLogger(InputChar.class);

    protected String baseDir = System.getProperty("user.dir");

    protected String outputFilename;

    class CharStats {
        public String fullName;

        public String speed;

        public String dex;
        
        public Element toXml() {
            return toXml(null);
        }

        public Element toXml(String charElementName) {
            Element root;
            
            if (charElementName != null) {
                root = new Element(charElementName);
            } else {
                root = new Element("character");
            }

            root.addContent(new Element("name").setText(fullName));

            Element chars = new Element("characteristics");
            chars.addContent(new Element("dex").addContent(new Element("total").setText(dex)));
            Element spd = new Element("spd");
            spd.addContent(new Element("total").setText(speed));
            spd.addContent(new Element("notes").setText(formatPhases(speed)));
            chars.addContent(spd);

            root.addContent(chars);

            return root;
        }

        public String formatPhases(String speed) {
            String result = "Phases: ";
            int speedInt = Integer.parseInt(speed);

            result += Integer.toString(Person.PHASES[speedInt - 1][0]);
            for (int i = 1; i < Person.PHASES[speedInt - 1].length; i++) {
                result += ", " + Integer.toString(Person.PHASES[speedInt - 1][i]);
            }

            return result;
        }

        public String toString() {
            return fullName + ", " + speed + ", " + dex;
        }
    }

    protected static Options getOptions() {
        Options result = new Options();

        return result;
    }

    protected void run(String[] args) throws Exception {
        PosixParser parser = new PosixParser();
        CommandLine line = parser.parse(getOptions(), args);

        if (line.getArgList().size() != 1) {
            throw new Exception("Can only have one filename.");
        }
        
        setOutputFilename(line.getArgList().get(0).toString());

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        
        Element base = new Element("characters");
        boolean notDone = true;
        while (notDone) {
            String heroOrVillain = "d";
            System.out.print("(H)ero, (V)illain, or (D)one");
            heroOrVillain = input.readLine();
            
            if (!heroOrVillain.toLowerCase().equals("d")) {
                CharStats thisChar = getInformation(input, heroOrVillain);
                if (heroOrVillain.toLowerCase().equals("h")) {
                    base.addContent(thisChar.toXml("hero"));
                } else {
                    base.addContent(thisChar.toXml("villain"));
                }
            } else {
                notDone = false;
            }
        }
        Utils.saveXml(base, outputFilename);
    }

    protected CharStats getInformation(BufferedReader input, String heroOrVillain) throws Exception {
        CharStats result = new CharStats();

        Field[] fields = CharStats.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (!Modifier.isFinal(fields[i].getModifiers())) {
                System.out.print(fields[i].getName() + " : ");
                String response = input.readLine();
                fields[i].set(result, response);
            }
        }

        return result;
    }

    public static void displayHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("inputchar", getOptions());
    }

    public static void main(String[] args) {
        try {
            InputChar i = new InputChar();
            i.run(args);
        } catch (Exception e) {
            logger.error(e);
            displayHelp();
        }
    }

    /**
     * @return Returns the outputFilename.
     */
    public String getOutputFilename() {
        return outputFilename;
    }

    /**
     * @param outputFilename
     *            The outputFilename to set.
     */
    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }
}