/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.masklabelingplugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;


import gov.nist.itl.ssd.wipp.masklabelingplugin.utils.BioFormatsUtils;
import gov.nist.itl.ssd.wipp.masklabelingplugin.utils.ConnectedComponents;
import gov.nist.itl.ssd.wipp.masklabelingplugin.utils.Connectedness;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.codec.CompressionType;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.services.OMEXMLService;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

/**
 * @author Antoine Vandecreme
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
public class MaskLabeling {

	// Tile size used in WIPP
	private static final int TILE_SIZE = 1024;

	private final File inputFolder;
	private final File outputFolder;
	private final Connectedness connectedness;

	public MaskLabeling(File inputFolder, File outputFolder,
			Connectedness connectedness) {
		this.inputFolder = inputFolder;
		this.outputFolder = outputFolder;
		this.connectedness = connectedness;
	}

	public void run() throws Exception {

		if (inputFolder == null) {
			throw new NullPointerException("Input folder is null");
		}

		File[] tiles =  inputFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".tif");
			}
		});

		if (tiles == null) {
			throw new NullPointerException("Input folder is empty");
		}
		
		outputFolder.mkdirs();

		for(File tile : tiles){
			//Reading a tiled tiff with Bioformats and converting it to an ImagePlus 
			ImagePlus imp = BioFormatsUtils.readImage(tile.getAbsolutePath());
			if (imp == null) {
				throw new IOException("Unable to read image " + tile.getName());
			}

			ImageProcessor ip = imp.getProcessor();
			ip = label(ip);
			
			//Get Pixel values as shorts 
			short[] sBytes= (short[]) ip.getPixels();
			File outputFile = new File(outputFolder, tile.getName());
			
			//Converting short array to bytes array
			ByteBuffer byteBuf = ByteBuffer.allocate(sBytes.length*2);
			for (short s: sBytes){
				byteBuf.putShort(s);
			}
			byte[] bytes = byteBuf.array();
			
			OMEXMLMetadata metadata = getMetadata(tile);
			
			//Writing the output tiled tiff
			try (OMETiffWriter imageWriter = new OMETiffWriter()) {
				imageWriter.setMetadataRetrieve(metadata);
				imageWriter.setTileSizeX(TILE_SIZE);
				imageWriter.setTileSizeY(TILE_SIZE);
				imageWriter.setInterleaved(metadata.getPixelsInterleaved(0));
				imageWriter.setCompression(CompressionType.LZW.getCompression());
				imageWriter.setId(outputFile.getPath());
				imageWriter.saveBytes(0, bytes);

			} catch (FormatException | IOException ex) {
				throw new RuntimeException("No image writer found for file "
						+ outputFile, ex);
			}
		}
	}

	// Taken from https://vm-070.nist.gov/gitweb/?p=Image-Analysis;a=blob;f=Utilities/Java/Plugins/Connected_Components_Labeling/src/gov/nist/isg/Connected_Components_Labeling.java;hb=HEAD
	private ImageProcessor label(ImageProcessor ip) {
		ip = ip.convertToShort(false);

		int width = ip.getWidth();
		int height = ip.getHeight();

		// convert the pixels into binary
		int[] label = new int[ip.getHeight()*ip.getWidth()];

		for(int i = 0; i < ip.getHeight(); ++i) {
			for(int j = 0; j < ip.getWidth(); ++j) {
				int val = ip.get(j,i); // get(x,y)
				label[i*ip.getWidth()+j] = (val > 0) ? 1 : 0;
			}
		}

		switch (this.connectedness) {
		case FOUR_CONNECTED:
			ConnectedComponents.bwlabel4(label, width, height);
			break;
		case HEIGHT_CONNECTED:
			ConnectedComponents.bwlabel8(label, width, height);
			break;
		default:
			throw new UnsupportedOperationException("Connectedness "
					+ this.connectedness + " not supported.");
		}

		// pack the result into a uint16 image
		// replace with set(int x, int y, int value)

		for(int i = 0; i < ip.getHeight(); ++i) {
			for(int j = 0; j < ip.getWidth(); ++j) {
				int val = label[i*ip.getWidth() + j];
				ip.set(j,i,val); // set(x,y,value)
			}
		}
		return ip;
	}
	
	//Inspired from the WIPP-image-assembling-plugin
	private OMEXMLMetadata getMetadata(File tile) {
		OMEXMLMetadata metadata;
		try {
			OMEXMLService omeXmlService = new ServiceFactory().getInstance(
					OMEXMLService.class);
			metadata = omeXmlService.createOMEXMLMetadata();
		} catch (DependencyException ex) {
			throw new RuntimeException("Cannot find OMEXMLService", ex);
		} catch (ServiceException ex) {
			throw new RuntimeException("Cannot create OME metadata", ex);
		}
		try (ImageReader imageReader = new ImageReader()) {
			IFormatReader reader;
			reader = imageReader.getReader(tile.getPath());
			reader.setOriginalMetadataPopulated(false);
			reader.setMetadataStore(metadata);
			reader.setId(tile.getPath());
		} catch (FormatException | IOException ex) {
			throw new RuntimeException("No image reader found for file "
					+ tile, ex);
		}

		metadata.setPixelsType(PixelType.UINT16, 0);
		metadata.setPixelsSignificantBits(new PositiveInteger(16), 0);

		return metadata;
	}

}
