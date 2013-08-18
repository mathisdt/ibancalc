package org.zephyrsoft.ibancalc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import com.ancientprogramming.fixedformat4j.format.FixedFormatManager;
import com.ancientprogramming.fixedformat4j.format.impl.FixedFormatManagerImpl;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.zephyrsoft.ibancalc.model.Bank;

/**
 * IBAN calculation for Germany (assumed IBAN country code is "DE").
 * 
 * @author Mathis Dirksen-Thedens
 */
public class Start {
	
	@Option(
		name = "--csv",
		usage = "use this semicolon-separated CSV file as input - it has to contain at least the columns \"bank\" and \"account\"",
		metaVar = "FILE")
	private File csv = null;
	
	@Option(name = "--bank", usage = "use this bank code", metaVar = "NUMBER")
	private String bankCode = null;
	@Option(name = "--account", usage = "use this account number", metaVar = "NUMBER")
	private String accountNumber = null;
	
	private Set<Bank> banks;
	
	private Start(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			// handle bad argument combinations
			if ((csv == null && bankCode == null && accountNumber == null)
				|| (bankCode != null && accountNumber == null) || (bankCode == null && accountNumber != null)) {
				throw new CmdLineException(parser, "print help");
			}
		} catch (CmdLineException e) {
			parser.setUsageWidth(80);
			parser.printUsage(System.err);
			System.err.println();
			return;
		}
		
		loadBankData();
		
		// single value has precedence
		if (bankCode != null && accountNumber != null) {
			handleSingle();
		} else if (csv != null) {
			handleFile();
		}
	}
	
	private void handleSingle() {
		try {
			System.out.println("IBAN: " + calculateIban(bankCode, accountNumber));
		} catch (CheckDigitException e) {
			System.err.println("error while calculating the IBAN");
		}
		System.out.println("BIC:  " + lookupBIC(bankCode));
	}
	
	private void handleFile() {
		int line = 1;
		try {
			// initialize
			CsvReader input = new CsvReader(csv.getAbsolutePath(), ';', Charset.forName("UTF-8"));
			CsvWriter output = new CsvWriter(System.out, ';', Charset.forName("UTF-8"));
			
			// read header line
			input.readHeaders();
			String[] headers = input.getHeaders();
			
			// write header line
			for (String header : headers) {
				output.write(header);
			}
			output.write("iban");
			output.write("bic");
			output.endRecord();
			
			line++;
			
			while (input.readRecord()) {
				// read data from input
				String accountFromFile = input.get("account");
				String bankFromFile = input.get("bank");
				
				// calculate IBAN
				String iban = calculateIban(bankFromFile, accountFromFile);
				
				// get BIC
				String bic = lookupBIC(bankFromFile);
				
				// write data to output
				for (String header : headers) {
					output.write(input.get(header));
				}
				output.write(iban);
				output.write(bic);
				output.endRecord();
				
				line++;
			}
			
			// clean up
			input.close();
			output.close();
		} catch (FileNotFoundException e) {
			System.err.println("file not found: " + csv.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("error in line " + line);
		}
	}
	
	private void loadBankData() {
		banks = new HashSet<>();
		FixedFormatManager manager = new FixedFormatManagerImpl();
		
		try {
			// find latest file in classpath
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("classpath*:BLZ_*.txt");
			Resource latest = null;
			for (Resource resource : resources) {
				// TODO what if it is not a file?
				if (latest == null || latest.getFilename().compareTo(resource.getFilename()) < 0) {
					latest = resource;
				}
			}
			
			// read file
			if (latest != null) {
				BufferedReader reader =
					new BufferedReader(new InputStreamReader(latest.getInputStream(), Charset.forName("ISO-8859-15")));
				String line;
				while ((line = reader.readLine()) != null) {
					Bank bank = manager.load(Bank.class, line);
					if (bank.isFilled()) {
						banks.add(bank);
					}
				}
				reader.close();
			}
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}
	
	private String lookupBIC(String oldBankCode) {
		for (Bank bank : banks) {
			if (oldBankCode.equals(bank.getBlz())) {
				return bank.getBic();
			}
		}
		// nothing found
		return "";
	}
	
	private static String calculateIban(String singleBankCode, String singleAccountNumber) throws CheckDigitException {
		String ibanTail = prependZeroes(singleBankCode, 8) + prependZeroes(singleAccountNumber, 10);
		return "DE" + IBANCheckDigit.IBAN_CHECK_DIGIT.calculate("DE00" + ibanTail) + ibanTail;
	}
	
	private static String prependZeroes(String input, int targetLength) {
		String result = input;
		while (result != null && result.length() < targetLength) {
			result = "0" + result;
		}
		return result;
	}
	
	public static void main(String[] args) {
		new Start(args);
	}
	
}
