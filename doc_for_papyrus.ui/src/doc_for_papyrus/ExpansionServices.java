package doc_for_papyrus;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.image.ImageFileFormat;
import org.eclipse.gmf.runtime.diagram.ui.render.util.CopyToImageUtil;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.uml2.uml.Classifier;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.located.LocatedJDOMFactory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class ExpansionServices 
{
	public EList<EList<Classifier>> ancestors(Classifier classifier)
	{		

			EList<Classifier> ancestors = classifier.getGenerals();
			EList<EList<Classifier>> list = new BasicEList<EList<Classifier>>(10);
			if (!ancestors.isEmpty())
			{
				list.add(ancestors);
			}
			EList<EList<Classifier>> grandAnscestors = new BasicEList<EList<Classifier>>(10);  
			for (Classifier ancestor : ancestors)
			{
				EList<EList<Classifier>> myAnscestors = ancestors(ancestor);
				grandAnscestors = union(grandAnscestors, myAnscestors);
			}
		if (!grandAnscestors.isEmpty())
		{
			list.addAll(grandAnscestors);
		}
		return list;
	}
	
	protected EList<EList<Classifier>> union(EList<EList<Classifier>> l1, EList<EList<Classifier>> l2)
	{
		Iterator<EList<Classifier>> i1 = l1.iterator(),
				                    i2 = l2.iterator();
		
		EList<Classifier> slc1, slc2;
		
		EList<EList<Classifier>> fusion = new BasicEList<EList<Classifier>>(20);
		
		while (i1.hasNext() || i2.hasNext())
		{
			if (i1.hasNext())
			{
				EList<Classifier> sl1 = i1.next();
				slc1 = new BasicEList<Classifier>(sl1.size());
				slc1.addAll(sl1);
			}
			else
			{
				slc1 = new BasicEList<Classifier>(0);
			}
			if (i2.hasNext())
			{
				
				EList<Classifier> sl2 = i2.next();
				slc2 = new BasicEList<Classifier>(sl2.size());
				slc2.addAll(sl2);
			}
			else
			{
				slc2 = new BasicEList<Classifier>(0);
			}
			slc1.addAll(slc2);
			fusion.add(slc1);
		}
		return fusion;
	}
	
	public String svg(EObject object, String outputDir, String imagesPath, int width, int height) 
	{
		if (object != null) 
		{
			Diagram diagram = (Diagram) object;

			CopyToImageUtil util = new CopyToImageUtil();
			try 
			{
				byte[] stream = util.copyToImageByteArray(diagram, width, height, ImageFileFormat.SVG, new NullProgressMonitor(), null, false);				
				SAXBuilder jdomBuilder = new SAXBuilder();
				jdomBuilder.setJDOMFactory(new LocatedJDOMFactory());
				Document svgDoc = jdomBuilder.build(new ByteArrayInputStream(stream));
				
				XMLOutputter xml = new XMLOutputter();
				xml.setFormat(Format.getPrettyFormat());
				
				if (svgDoc != null)
				{
					String svgFilename = generateSvgFilename(diagram);
					PrintStream xmlOutput = new PrintStream(outputDir + File.separator + imagesPath + File.separator + svgFilename);
					xmlOutput.println(xml.outputString(svgDoc));
					xmlOutput.close();
					return imagesPath + '/' + svgFilename;
				}
				else
				{
					return "";
				}
				
			} 
			catch (JDOMException | IOException | CoreException e) 
			{
				return "";
			}
		} 
		else 
		{
			return "";
		}
	}
	
	protected String generateSvgFilename(Diagram diagram)
	{
		return Integer.toHexString(diagram.hashCode()) + ".svg";
	}
}
