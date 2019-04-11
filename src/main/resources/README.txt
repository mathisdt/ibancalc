The supplied BLZ_*.txt files are from the Extranet of the Deutsche Bundesbank:
https://www.bundesbank.de/de/aufgaben/unbarer-zahlungsverkehr/serviceangebot/bankleitzahlen/download-bankleitzahlen-602592

You may add a newer file on the classpath (which includes the directory this file
resides in) and it will be automatically used instead of one of these.

The CSV file you can use as input has to have the columns "account" (account number)
and "bank" (bank code). IBANCalc will add the columns "iban" and "bic" to the
output.

At the moment, this tool only understands German accounts as input!
