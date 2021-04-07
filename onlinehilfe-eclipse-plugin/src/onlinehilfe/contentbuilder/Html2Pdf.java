package onlinehilfe.contentbuilder;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings.Syntax;

import onlinehilfe.contentbuilder.XslTransformUtil.MultiException;
import onlinehilfe.contentbuilder.XslTransformUtil.MultiExceptionCollector;

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
		
		String contentXhtmlString = XslTransformUtil.preConvertHtml2XhtmlInputStreamAsString(new FileInputStream(contentHtml), contentHtml.getAbsolutePath());
			
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
		MultiExceptionCollector multiExceptionCollector = new MultiExceptionCollector(); 
		transformerDebug.setErrorListener(multiExceptionCollector);
		transformer.setErrorListener(multiExceptionCollector);
				
		transformerDebug.transform(new StreamSource(IOUtils.toInputStream(contentXhtmlString, FilesUtil.CHARSET_STRING)), resDebug);
		
		// Start XSLT transformation and FOP processing
		// That's where the XML is first transformed to XSL-FO and then
		// PDF is created
		transformer.transform(new StreamSource(IOUtils.toInputStream(contentXhtmlString, FilesUtil.CHARSET_STRING)), res);
		
		multiExceptionCollector.throwsErrorsIfCollected();
	}
		 
}
