package net.sf.l2j.gameserver.scripts;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ScriptDocument
{
	private Document _document;
	private String _name;

	public ScriptDocument(String name, InputStream input)
	{
		_name = name;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			_document = builder.parse(input);

		}
		catch(SAXException sxe)
		{
			// Error generated during parsing)
			Exception x = sxe;
			if(sxe.getException() != null)
			{
				x = sxe.getException();
			}
			x.printStackTrace();

		}
		catch(ParserConfigurationException pce)
		{
			// Parser with specified options can't be built
			pce.printStackTrace();
		}
		catch(IOException ioe)
		{
			// I/O error
			ioe.printStackTrace();
			
		}finally{
			
			if(input!= null)
				try
				{
					input.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
		}
	}

	public Document getDocument()
	{
		return _document;
	}

	/**
	 * @return Returns the _name.
	 */
	public String getName()
	{
		return _name;
	}

	@Override
	public String toString()
	{
		return _name;
	}

}