/*
 * Created on Oct 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.culpan.herosim;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * @author culpanh
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Utils {
	protected static final Logger logger = Logger.getLogger(Utils.class);

	public static Random random = new Random();

	// public static ErrorListener errorListener = null;

	public static void notifyError(Throwable exc) {
		/*
		 * if (errorListener != null) { errorListener.error(exc); } else {
		 * exc.printStackTrace(System.err); }
		 */
		if (logger.isDebugEnabled()) {
			exc.printStackTrace(System.err);
		}
		logger.error(exc);
	}
	
	public static Element loadXml(String xml) {
		Element result = null;
		
		StringReader rdr = new StringReader(xml);
		try {
			SAXBuilder builder = new SAXBuilder();

			Document doc = builder.build(rdr);
			result = doc.getRootElement();
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}
	
	public static Element loadXml(InputStream file) {
		Element result = null;
		try {
			SAXBuilder builder = new SAXBuilder();

			Document doc = builder.build(file);
			result = doc.getRootElement();
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}

	public static Element loadXml(File file) {
		Element result = null;
		try {
			SAXBuilder builder = new SAXBuilder();

			Document doc = builder.build(file);
			result = doc.getRootElement();
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}

		return result;
	}

	public static void saveXml(Element root, String outputFilename) {
		XMLOutputter serializer = new XMLOutputter();
		serializer.setFormat(Format.getPrettyFormat());

		Document doc = root.getDocument();
		if (doc != null) {
			doc.detachRootElement();
		}

		try {
			PrintStream output = null;
			if (outputFilename == null) {
				output = System.out;
			} else {
				output = new PrintStream(new FileOutputStream(outputFilename));
			}

			serializer.output(new Document(root), output);
			output.close();
		} catch (IOException e) {
			notifyError(e);
		}
	}

	public static String toString(Element root) {
		String result = null;
		
		XMLOutputter serializer = new XMLOutputter();
		serializer.setFormat(Format.getRawFormat());

		StringWriter w = new StringWriter();

		Document doc = root.getDocument();
		if (doc != null) {
			doc.detachRootElement();
		} 

		try {
			serializer.output(new Document(root), w);
			w.close();
			result = w.toString().replace('\r', ' ').replace('\n', ' ');
		} catch (IOException e) {
			notifyError(e);
		}
		
		return result;
	}

	public static int rnd(int range) {
		return random.nextInt(range);
	}

	public static double rnd() {
		return random.nextDouble();
	}

	public static int parseInt(String i) {
		return parseInt(i, 0);
	}

	public static int parseInt(String i, int def) {
		int result = def;

		try {
			if (i.charAt(0) == '+') {
				result = Integer.parseInt(i.substring(1).trim());
			} else {
				result = Integer.parseInt(i.trim());
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return result;
	}

	public static long parseLong(String i) {
		return parseLong(i, 0);
	}

	public static long parseLong(String i, int def) {
		long result = def;

		try {
			if (i.charAt(0) == '+') {
				result = Long.parseLong(i.substring(1).trim());
			} else {
				result = Long.parseLong(i.trim());
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return result;
	}
	
	public static boolean isMacOs() {
		return System.getProperty("os.name").equals("Mac OS X");
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	public static BufferedImage createImage(Object o, String path) {
	    java.net.URL imgURL = o.getClass().getResource(path);
	    if (imgURL != null) {
	    	try {
	    		return ImageIO.read(imgURL);
	    	} catch (IOException e) {
	    		logger.error("Unable to load image : " + e.getLocalizedMessage());
	    		return null;
	    	}
	    } else {
	    	logger.info("Incorrect path to image : " + path);
	        return null;
	    }
	}}
