package net.sf.l2j.launcher.drops;

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public final class XmlUtil
{
	public static Document load(File file)
	{
		try
		{
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			f.setIgnoringComments(false);
			f.setNamespaceAware(false);
			return f.newDocumentBuilder().parse(file);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static void save(Document doc, File file)
	{
		try
		{
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			t.transform(new DOMSource(doc), new StreamResult(file));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
