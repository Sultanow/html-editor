package onlinehilfe.contentbuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings.Syntax;

public final class XslTransformUtil {
	
	private XslTransformUtil() {}
	
	private static Map<String, String> htmlCharacterSequenceStringSubstitutorMap = new HashMap<>();

	static {
		//EscapeCharMapping
		htmlCharacterSequenceStringSubstitutorMap.put("&tilde;", "&#126;");
		htmlCharacterSequenceStringSubstitutorMap.put("&florin;", "&#131;");
		htmlCharacterSequenceStringSubstitutorMap.put("&elip;", "&#133;");
		htmlCharacterSequenceStringSubstitutorMap.put("&dag;", "&#134;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ddag;", "&#135;");
		htmlCharacterSequenceStringSubstitutorMap.put("&cflex;", "&#136;");
		htmlCharacterSequenceStringSubstitutorMap.put("&permil;", "&#137;");
		htmlCharacterSequenceStringSubstitutorMap.put("&uscore;", "&#138;");
		htmlCharacterSequenceStringSubstitutorMap.put("&OElig;", "&#140;");
		htmlCharacterSequenceStringSubstitutorMap.put("&lsquo;", "&#145;");
		htmlCharacterSequenceStringSubstitutorMap.put("&rsquo;", "&#146;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ldquo;", "&#147;");
		htmlCharacterSequenceStringSubstitutorMap.put("&rdquo;", "&#148;");
		htmlCharacterSequenceStringSubstitutorMap.put("&bullet;", "&#149;");
		htmlCharacterSequenceStringSubstitutorMap.put("&endash;", "&#150;");
		htmlCharacterSequenceStringSubstitutorMap.put("&emdash;", "&#151;");
		htmlCharacterSequenceStringSubstitutorMap.put("&trade;", "&#153;");
		htmlCharacterSequenceStringSubstitutorMap.put("&oelig;", "&#156;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Yuml;", "&#159;");
		htmlCharacterSequenceStringSubstitutorMap.put("&nbsp;", "&#160;");
		htmlCharacterSequenceStringSubstitutorMap.put("&iexcl;", "&#161;");
		htmlCharacterSequenceStringSubstitutorMap.put("&cent;", "&#162;");
		htmlCharacterSequenceStringSubstitutorMap.put("&pound;", "&#163;");
		htmlCharacterSequenceStringSubstitutorMap.put("&curren;", "&#164;");
		htmlCharacterSequenceStringSubstitutorMap.put("&yen;", "&#165;");
		htmlCharacterSequenceStringSubstitutorMap.put("&brvbar;", "&#166;");
		htmlCharacterSequenceStringSubstitutorMap.put("&sect;", "&#167;");
		htmlCharacterSequenceStringSubstitutorMap.put("&uml;", "&#168;");
		htmlCharacterSequenceStringSubstitutorMap.put("&copy;", "&#169;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ordf;", "&#170;");
		htmlCharacterSequenceStringSubstitutorMap.put("&laquo;", "&#171;");
		htmlCharacterSequenceStringSubstitutorMap.put("&not;", "&#172;");
		htmlCharacterSequenceStringSubstitutorMap.put("&shy;", "&#173;");
		htmlCharacterSequenceStringSubstitutorMap.put("&reg;", "&#174;");
		htmlCharacterSequenceStringSubstitutorMap.put("&macr;", "&#175;");
		htmlCharacterSequenceStringSubstitutorMap.put("&deg;", "&#176;");
		htmlCharacterSequenceStringSubstitutorMap.put("&plusmn;", "&#177;");
		htmlCharacterSequenceStringSubstitutorMap.put("&sup2;", "&#178;");
		htmlCharacterSequenceStringSubstitutorMap.put("&sup3;", "&#179;");
		htmlCharacterSequenceStringSubstitutorMap.put("&acute;", "&#180;");
		htmlCharacterSequenceStringSubstitutorMap.put("&micro;", "&#181;");
		htmlCharacterSequenceStringSubstitutorMap.put("&para;", "&#182;");
		htmlCharacterSequenceStringSubstitutorMap.put("&middot;", "&#183;");
		htmlCharacterSequenceStringSubstitutorMap.put("&cedil;", "&#184;");
		htmlCharacterSequenceStringSubstitutorMap.put("&sup1;", "&#185;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ordm;", "&#186;");
		htmlCharacterSequenceStringSubstitutorMap.put("&raquo;", "&#187;");
		htmlCharacterSequenceStringSubstitutorMap.put("&frac14;", "&#188;");
		htmlCharacterSequenceStringSubstitutorMap.put("&frac12;", "&#189;");
		htmlCharacterSequenceStringSubstitutorMap.put("&frac34;", "&#190;");
		htmlCharacterSequenceStringSubstitutorMap.put("&iquest;", "&#191;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Agrave;", "&#192;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Aacute;", "&#193;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Acirc;", "&#194;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Atilde;", "&#195;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Auml;", "&#196;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Aring;", "&#197;");
		htmlCharacterSequenceStringSubstitutorMap.put("&AElig;", "&#198;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ccedil;", "&#199;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Egrave;", "&#200;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Eacute;", "&#201;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ecirc;", "&#202;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Euml;", "&#203;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Igrave;", "&#204;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Iacute;", "&#205;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Icirc;", "&#206;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Iuml;", "&#207;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ETH;", "&#208;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ntilde;", "&#209;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ograve;", "&#210;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Oacute;", "&#211;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ocirc;", "&#212;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Otilde;", "&#213;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ouml;", "&#214;");
		htmlCharacterSequenceStringSubstitutorMap.put("&times;", "&#215;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Oslash;", "&#216;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ugrave;", "&#217;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Uacute;", "&#218;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Ucirc;", "&#219;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Uuml;", "&#220;");
		htmlCharacterSequenceStringSubstitutorMap.put("&Yacute;", "&#221;");
		htmlCharacterSequenceStringSubstitutorMap.put("&THORN;", "&#222;");
		htmlCharacterSequenceStringSubstitutorMap.put("&szlig;", "&#223;");
		htmlCharacterSequenceStringSubstitutorMap.put("&agrave;", "&#224;");
		htmlCharacterSequenceStringSubstitutorMap.put("&aacute;", "&#225;");
		htmlCharacterSequenceStringSubstitutorMap.put("&acirc;", "&#226;");
		htmlCharacterSequenceStringSubstitutorMap.put("&atilde;", "&#227;");
		htmlCharacterSequenceStringSubstitutorMap.put("&auml;", "&#228;");
		htmlCharacterSequenceStringSubstitutorMap.put("&aring;", "&#229;");
		htmlCharacterSequenceStringSubstitutorMap.put("&aelig;", "&#230;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ccedil;", "&#231;");
		htmlCharacterSequenceStringSubstitutorMap.put("&egrave;", "&#232;");
		htmlCharacterSequenceStringSubstitutorMap.put("&eacute;", "&#233;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ecirc;", "&#234;");
		htmlCharacterSequenceStringSubstitutorMap.put("&euml;", "&#235;");
		htmlCharacterSequenceStringSubstitutorMap.put("&igrave;", "&#236;");
		htmlCharacterSequenceStringSubstitutorMap.put("&iacute;", "&#237;");
		htmlCharacterSequenceStringSubstitutorMap.put("&icirc;", "&#238;");
		htmlCharacterSequenceStringSubstitutorMap.put("&iuml;", "&#239;");
		htmlCharacterSequenceStringSubstitutorMap.put("&eth;", "&#240;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ntilde;", "&#241;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ograve;", "&#242;");
		htmlCharacterSequenceStringSubstitutorMap.put("&oacute;", "&#243;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ocirc;", "&#244;");
		htmlCharacterSequenceStringSubstitutorMap.put("&otilde;", "&#245;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ouml;", "&#246;");
		htmlCharacterSequenceStringSubstitutorMap.put("&oslash;", "&#248;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ugrave;", "&#249;");
		htmlCharacterSequenceStringSubstitutorMap.put("&uacute;", "&#250;");
		htmlCharacterSequenceStringSubstitutorMap.put("&ucirc;", "&#251;");
		htmlCharacterSequenceStringSubstitutorMap.put("&uuml;", "&#252;");
		htmlCharacterSequenceStringSubstitutorMap.put("&yacute;", "&#253;");
		htmlCharacterSequenceStringSubstitutorMap.put("&thorn;", "&#254;");
		htmlCharacterSequenceStringSubstitutorMap.put("&yuml;", "&#255;");
		htmlCharacterSequenceStringSubstitutorMap.put("&euro;", "&#x20AC;");
	}
	
	public static String substituteInStringBySubstitutorMap(final String text) {
		String internalText = text;
		for (Map.Entry<String, String> substitiontionEntry: htmlCharacterSequenceStringSubstitutorMap.entrySet()) {
			internalText = internalText.replace(substitiontionEntry.getKey(), substitiontionEntry.getValue());
		}
		return internalText;
	}
	
	public static class MultiExceptionCollector implements ErrorListener {
		private MultiException exception = new MultiException();
		
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
		
		public boolean hasCollectedErrors() {
			return exception.hasCollectedErrors();
		}
		
		public void throwsErrorsIfCollected() throws MultiException {
			if (exception.hasCollectedErrors()) {
				throw exception;
			}
		}
		
		public void reset() {
			if (exception.hasCollectedErrors()) {
				exception = new MultiException();
			}
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
	
	public static String preConvertHtml2XhtmlInputStreamAsString(String html, String docRootUri) throws IOException {
		//Tranformiere HTML zu XHTML
		org.jsoup.nodes.Document jsoupDocument = Jsoup.parse(html, docRootUri);
		jsoupDocument.outputSettings().syntax(Syntax.xml);
		return substituteInStringBySubstitutorMap(jsoupDocument.outerHtml());
	}
	
	public static String preConvertHtml2XhtmlInputStreamAsString(InputStream inputStream, String docRootUri) throws IOException {
		//Tranformiere HTML zu XHTML
		org.jsoup.nodes.Document jsoupDocument = Jsoup.parse(inputStream, FilesUtil.CHARSET_STRING, docRootUri);
		jsoupDocument.outputSettings().syntax(Syntax.xml);
		return substituteInStringBySubstitutorMap(jsoupDocument.outerHtml());
	}
	
	public static InputStream preConvertHtml2XhtmlInputStream(String html, String docRootUri) throws IOException {
		return IOUtils.toInputStream(preConvertHtml2XhtmlInputStreamAsString(html, docRootUri), FilesUtil.CHARSET_STRING);
	}
	
	public static InputStream preConvertHtml2XhtmlInputStream(InputStream inputStream, String docRootUri) throws IOException {
		return IOUtils.toInputStream(preConvertHtml2XhtmlInputStreamAsString(inputStream, docRootUri), FilesUtil.CHARSET_STRING);
	}
	
}
