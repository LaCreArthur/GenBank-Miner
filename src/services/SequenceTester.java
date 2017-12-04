package services;

/**
 * Bunch of methods to check the validity of a sequence
 */
public class SequenceTester {
    public static Boolean isValid (StringBuilder sequence) {
        return testSize(sequence)
            && testCodonStart(sequence)
            && testCodonStop(sequence);
    }

    private static Boolean testSize(StringBuilder sequence) {
        return sequence.length() > 3
            && sequence.length() % 3 == 0;
    }

    private static Boolean testCodonStart(StringBuilder sequence) {
        switch (sequence.substring(0, 3)) {
            case "ATA":
                return true;
            case "ATC":
                return true;
            case "ATG":
                return true;
            case "ATT":
                return true;
            case "CTG":
                return true;
            case "GTG":
                return true;
            case "TTA":
                return true;
            case "TTG":
                return true;
            default:
                return false; //ne commence pas par un codon_start
        }
    }

    private static Boolean testCodonStop(StringBuilder sequence) {
        int s = sequence.length();
        String str_end = sequence.substring(s-3, s);
        switch (str_end) {
            case "TAA":
                return true;
            case "TAG":
                return true;
            case "TGA":
                return true;
            case "TTA":
                return true;
            default:
                return false; //ne finit pas par un codon stop
        }
    }
}
