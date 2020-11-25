/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
package com.thehuxley.cpd;

import net.sourceforge.pmd.util.FileFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.pmd.cpd.CPD;
import net.sourceforge.pmd.cpd.CPDListener;
import net.sourceforge.pmd.cpd.CPDNullListener;
import net.sourceforge.pmd.cpd.CSVRenderer;
import net.sourceforge.pmd.cpd.Language;
import net.sourceforge.pmd.cpd.LanguageFactory;
import net.sourceforge.pmd.cpd.Match;
import net.sourceforge.pmd.cpd.MatchAlgorithm;
import net.sourceforge.pmd.cpd.Renderer;
import net.sourceforge.pmd.cpd.SimpleRenderer;
import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokens;
import net.sourceforge.pmd.cpd.VSRenderer;
import net.sourceforge.pmd.cpd.XMLRenderer;

public class MyCPD {

	private Map<String, SourceCode> source = new HashMap<String, SourceCode>();
	private CPDListener listener = new CPDNullListener();
	private Tokens tokens = new Tokens();
	private int minimumTileSize;
	private MatchAlgorithm matchAlgorithm;
	private Language language;
	private boolean skipDuplicates;
	public static boolean debugEnable = false;
	private String encoding = System.getProperty("file.encoding");

	private Map<String, Integer> numberOfTokens = new HashMap<String, Integer>();

	public MyCPD(int minimumTileSize, Language language) {
		this.minimumTileSize = minimumTileSize;
		this.language = language;

	}

	public void skipDuplicates() {
		this.skipDuplicates = true;
	}

	public void setCpdListener(CPDListener cpdListener) {
		this.listener = cpdListener;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void go() {
		TokenEntry.clearImages();
		matchAlgorithm = new MatchAlgorithm(source, tokens, minimumTileSize,
				listener);
		matchAlgorithm.findMatches();
	}

	public Iterator<Match> getMatches() {
		return matchAlgorithm.matches();
	}

	public void add(File file) throws IOException {
		add(1, file);
	}

	public void addAllInDirectory(String dir) throws IOException {
		addDirectory(dir, false);
	}

	public void addRecursively(String dir) throws IOException {
		addDirectory(dir, true);
	}

	public void add(List<File> files) {
		for (File f : files) {
			try {
				if (f.exists()) {
					add(files.size(), f);
				}
			} catch (Throwable e) {
				System.err.println("Não foi possível verificar o arquivo: "
						+ f.getName() + ". Motivo: " + e.getMessage());
			}
		}
	}

	private void addDirectory(String dir, boolean recurse) throws IOException {
		if (!(new File(dir)).exists()) {
			throw new FileNotFoundException("Couldn't find directory " + dir);
		}
		FileFinder finder = new FileFinder();
		add(finder.findFilesFrom(dir, language.getFileFilter(), recurse));
	}

	private Set<String> current = new HashSet<String>();

	private void add(int fileCount, File file) throws IOException {

		if (skipDuplicates) {
			String signature = file.getName() + '_' + file.length();
			if (current.contains(signature)) {
				System.err
						.println("Skipping "
								+ file.getAbsolutePath()
								+ " since it appears to be a duplicate file and --skip-duplicate-files is set");
				return;
			}
			current.add(signature);
		}

		if (!file.getCanonicalPath().equals(
				new File(file.getAbsolutePath()).getCanonicalPath())) {
			System.err.println("Skipping " + file
					+ " since it appears to be a symlink");
			return;
		}

		listener.addedFile(fileCount, file);
		SourceCode sourceCode = new SourceCode(new SourceCode.FileCodeLoader(
				file, encoding));
		int before = tokens.size();
		language.getTokenizer().tokenize(sourceCode, tokens);

		numberOfTokens.put(sourceCode.getFileName(), (tokens.size() - before));

		source.put(sourceCode.getFileName(), sourceCode);
	}

	public static Renderer getRendererFromString(String name, String encoding) {
		if (name.equalsIgnoreCase("text") || name.equals("")) {
			return new SimpleRenderer();
		} else if ("xml".equals(name)) {
			return new XMLRenderer(encoding);
		} else if ("csv".equals(name)) {
			return new CSVRenderer();
		} else if ("vs".equals(name)) {
			return new VSRenderer();
		}
		try {
			return (Renderer) Class.forName(name).newInstance();
		} catch (Exception e) {
			System.err.println("Can't find class '" + name
					+ "', defaulting to SimpleRenderer.");
		}
		return new SimpleRenderer();
	}

	public int getNumberOfTokens(String fileName) {
		return numberOfTokens.get(fileName);
	}

	private static boolean findBooleanSwitch(String[] args, String name) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(name)) {
				return true;
			}
		}
		return false;
	}

	private static String findRequiredStringValue(String[] args, String name) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(name)) {
				return args[i + 1];
			}
		}
		System.err.println("No " + name + " value passed in");
		usage();
		throw new RuntimeException();
	}

	private static String findOptionalStringValue(String[] args, String name,
			String defaultValue) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(name)) {
				return args[i + 1];
			}
		}
		return defaultValue;
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			usage();
		}

		try {
			boolean skipDuplicateFiles = findBooleanSwitch(args,
					"--skip-duplicate-files");
			String languageString = findOptionalStringValue(args, "--language",
					"java");
			String formatString = findOptionalStringValue(args, "--format",
					"text");
			String encodingString = findOptionalStringValue(args, "--encoding",
					System.getProperty("file.encoding"));
			int minimumTokens = Integer.parseInt(findRequiredStringValue(args,
					"--minimum-tokens"));
			LanguageFactory f = new LanguageFactory();
			Language language = f.createLanguage(languageString);
			
			CPD cpd = new CPD(minimumTokens, language);
			cpd.setEncoding(encodingString);
			if (skipDuplicateFiles) {
				cpd.skipDuplicates();
			}
			boolean missingFiles = true;
			for (int position = 0; position < args.length; position++) {
				if (args[position].equals("--files")) {
					cpd.addRecursively(args[position + 1]);
					if (missingFiles) {
						missingFiles = false;
					}
				}
			}

			if (missingFiles) {
				System.err.println("No " + "--files" + " value passed in");
				usage();
				throw new RuntimeException();
			}

			cpd.go();
			// System.out.println(renderer.render(cpd.getMatches()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void usage() {
		System.out.println("Usage:");
		System.out
				.println(" java net.sourceforge.pmd.cpd.CPD --minimum-tokens xxx --files xxx [--language xxx] [--encoding xxx] [--format (xml|text|csv|vs)] [--skip-duplicate-files] ");
		System.out.println("i.e: ");
		System.out
				.println(" java net.sourceforge.pmd.cpd.CPD --minimum-tokens 100 --files c:\\jdk14\\src\\java ");
		System.out.println("or: ");
		System.out
				.println(" java net.sourceforge.pmd.cpd.CPD --minimum-tokens 100 --files /path/to/c/code --language c ");
		System.out.println("or: ");
		System.out
				.println(" java net.sourceforge.pmd.cpd.CPD --minimum-tokens 100 --encoding UTF-16LE --files /path/to/java/code --format xml");
	}

}
