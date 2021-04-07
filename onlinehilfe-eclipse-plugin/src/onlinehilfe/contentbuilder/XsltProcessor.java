package onlinehilfe.contentbuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.function.Supplier;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;

import onlinehilfe.contentbuilder.XslTransformUtil.MultiException;
import onlinehilfe.contentbuilder.XslTransformUtil.MultiExceptionCollector;

public class XsltProcessor {
	
	private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
	private final ThreadLocal<Transformer> transformerTL;
	
	public XsltProcessor(Supplier<InputStream> transformationXslInputStreamSupplier) {
		super();
		this.transformerTL = ThreadLocal.withInitial(() -> {
			try {
				Transformer  transformer = transformerFactory.newTransformer(new StreamSource(transformationXslInputStreamSupplier.get()));;
				MultiExceptionCollector multiExceptionCollector = new MultiExceptionCollector();
				transformer.setErrorListener(multiExceptionCollector);
				return transformer;
			} catch (Throwable e) {
				throw new RuntimeException("Feher beim Instanzieren eines Tranformers", e);
			}
		} );
	}

	public void transform(InputStream inputStream, OutputStream outputStream) throws IOException, TransformerException, MultiException {
		
		Transformer transformer = transformerTL.get();
		transformer.reset();
		((MultiExceptionCollector)transformer.getErrorListener()).reset();
		
		transformer.transform(new StreamSource(inputStream), new StreamResult(new BufferedWriter(new OutputStreamWriter(outputStream, FilesUtil.CHARSET))));
		
		((MultiExceptionCollector)transformer.getErrorListener()).throwsErrorsIfCollected();			
	}
	
}
