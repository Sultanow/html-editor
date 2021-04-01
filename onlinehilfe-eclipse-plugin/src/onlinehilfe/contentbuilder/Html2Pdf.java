package onlinehilfe.contentbuilder;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.navigator.OnlinehilfeNavigatorContentProvider;

public class Html2Pdf {
	
	private final File htmlRoot;
	private final File transformationXsl;
	private final Properties properties;
	
		
	public Html2Pdf(File htmlRoot, File transformationXsl, Properties properties) {
		this.htmlRoot = htmlRoot;
		this.transformationXsl = transformationXsl;
		this.properties = properties;

	}
	
	public void generatePdf(File contentHtml, OutputStream debugfopOutputStream, OutputStream outputStream) throws IOException, FOPException, TransformerConfigurationException, TransformerException, MultiException {
		
		FopFactory fopFactory = FopFactory.newInstance(htmlRoot.toURI());
		
		//Tranformiere zu XHTML
		org.jsoup.nodes.Document jsoupDocument = Jsoup.parse(contentHtml, FilesUtil.CHARSET_STRING);
		jsoupDocument.outputSettings().syntax(Syntax.xml);
		String output = jsoupDocument.outerHtml();
		output = HtmlToXMLTransformUtil.substituteInStringBySubstitutorMap(output);
				
		StreamSource xslSource = new StreamSource(new FileInputStream(transformationXsl));		

		//FO-XML Ausgabe (Debug)
		Result resDebug = new StreamResult(new BufferedWriter(new OutputStreamWriter(debugfopOutputStream, FilesUtil.CHARSET)));
		
		// Zu PDF -------------------------------------------------------------
		// Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, outputStream);
				
		// Resulting SAX events (the generated FO) must be piped through to FOP
		Result res = new SAXResult(fop.getDefaultHandler());
		
		// Setup XSLT
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformerDebug = transformerFactory.newTransformer(new StreamSource(new FileInputStream(transformationXsl)));
		Transformer transformer = transformerFactory.newTransformer(new StreamSource(new FileInputStream(transformationXsl)));

		//sorgt für lesbare Fehlermeldugen
		MultiException exception = new MultiException();
		transformerDebug.setErrorListener(new ErrorListener() {				
			@Override
			public void warning(TransformerException arg0) throws TransformerException {
				exception.addWarning(arg0);
			}
			@Override
			public void fatalError(TransformerException arg0) throws TransformerException {
				exception.addFatalError(arg0);
			}
			@Override
			public void error(TransformerException arg0) throws TransformerException {
				exception.addError(arg0);
			}
		});
		transformer.setErrorListener(new ErrorListener() {				
			@Override
			public void warning(TransformerException arg0) throws TransformerException {
				exception.addWarning(arg0);
			}
			@Override
			public void fatalError(TransformerException arg0) throws TransformerException {
				exception.addFatalError(arg0);
			}
			@Override
			public void error(TransformerException arg0) throws TransformerException {
				exception.addError(arg0);
			}
		});
		
		
		
		transformerDebug.transform(new StreamSource(new ByteArrayInputStream(output.getBytes(FilesUtil.CHARSET))), resDebug);
		
		// Start XSLT transformation and FOP processing
		// That's where the XML is first transformed to XSL-FO and then
		// PDF is created
		transformer.transform(new StreamSource(new ByteArrayInputStream(output.getBytes(FilesUtil.CHARSET))), res);
		
		
		
		if (exception.hasCollectedErrors()) {
			throw exception;
		}
	}
		
	public static class MultiException extends Exception {
		private static final long serialVersionUID = 8942795048891313060L;
		
		private boolean collected = false;
		
		public void addWarning(Exception e) {
			addSuppressed(new Exception("WARNING: " +  e.getMessage()));
			collected = true;
		}
		
		public void addFatalError(Exception e) {
			addSuppressed(new Exception("FATAL: " +  e.getMessage()));
			collected = true;
		}
		
		public void addError(Exception e) {
			addSuppressed(new Exception("ERROR: " +  e.getMessage()));
			collected = true;
		}
		
		public boolean hasCollectedErrors() {
			return collected;
		}
	} 
}
