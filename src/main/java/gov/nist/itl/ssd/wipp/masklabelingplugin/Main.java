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
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import gov.nist.itl.ssd.wipp.masklabelingplugin.utils.Connectedness;

/**
 * 
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */
public class Main {
	private static final Logger LOG = Logger.getLogger(
			Main.class.getName());

	public static void main(String[] args) {

		Options options = new Options();

		Option helpOption = new Option("h", "help", false,
				"Display this help message and exit.");
		options.addOption(helpOption);

		Option inputOption = new Option("i", "inputImages", true,
				"Input images folder - images to convert to dzi.");
		inputOption.setRequired(true);
		options.addOption(inputOption);

		Option connectednessOption = new Option("c", "connectedness", true,
				"four connected or eight connected.");
		connectednessOption.setRequired(true);
		options.addOption(connectednessOption);

		Option outputOption = new Option("o", "output", true,
				"Output folder or file where the output will be generated.");
		outputOption.setRequired(true);
		options.addOption(outputOption);

		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine commandLine = parser.parse(options, args);

			if (commandLine.hasOption(helpOption.getOpt())) {
				printHelp(options);
				return;
			}

			File inputImages = new File(
					commandLine.getOptionValue(inputOption.getOpt()));

			File outputFolder = new File(
					commandLine.getOptionValue(outputOption.getOpt()));
			
			String connectedness = commandLine.getOptionValue(connectednessOption.getOpt());

			try {
				long start = System.currentTimeMillis();

				MaskLabeling ml = new MaskLabeling(inputImages, outputFolder, Connectedness.valueOf(connectedness));	                
				ml.run();

				float duration = (System.currentTimeMillis() - start) / 1000F;
				LOG.info("Images labeled in " + duration + "s.");
			} catch (Exception ex) {
				LOG.severe("Error while labeling the image.");
				ex.printStackTrace();
			}     
		} catch (ParseException ex) {
			LOG.severe(ex.getMessage());
			printHelp(options);
			return;
		}
	}

	/**
	 * Print help
	 * @param options
	 */
	private static void printHelp(Options options) {
		new HelpFormatter().printHelp("wipp-mask-labeling", options);
	}
}
