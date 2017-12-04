package stat_classes;

import java.util.LinkedHashMap;

/**
 * Represent the number of each possible trinucleotide
 */
class TrinucleotidsOccurrences extends LinkedHashMap<String, Integer> {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3593651525676743068L;
	private final static String[] nucleotides = new String[] {"A", "C", "G", "T"};

    public TrinucleotidsOccurrences() {
        super();

        for (String char1 : nucleotides)
            for (String char2 : nucleotides)
                for (String char3 : nucleotides)
                    this.put(char1+char2+char3, 0);

    }

    public void increment (String trinucleotid) {
        Integer computeRes = this.computeIfPresent(trinucleotid, (k, v) -> v+1);
        if ( computeRes == null)
            throw new IndexOutOfBoundsException("The trinucleotid \'"+trinucleotid+"\' does not exist.");
    }
}

class DinucleotidsOccurrences extends LinkedHashMap<String, Integer> {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3999671961626190230L;
	private final static String[] nucleotides = new String[] {"A", "C", "G", "T"};

    public DinucleotidsOccurrences() {
        super();

        for (String char1 : nucleotides)
            for (String char2 : nucleotides)
                this.put(char1+char2, 0);
    }

    public void increment (String dinucleotid) {
        Integer computeRes = this.computeIfPresent(dinucleotid, (k,v) -> v+1);
        if (computeRes == null)
            throw new IndexOutOfBoundsException("The dinucleotid \'"+dinucleotid+"\' does not exist.");
    }

    public void decrement (String dinucleotid) {
        Integer computeRes = this.computeIfPresent(dinucleotid, (k,v) -> v-1);
        if (computeRes == null || computeRes < 0)
            throw new IndexOutOfBoundsException("The dinucleotid \'"+dinucleotid+"\' does not exist.");
    }
}