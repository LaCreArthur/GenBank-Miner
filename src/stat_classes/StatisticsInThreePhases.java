package stat_classes;

/**
 * Represent the number of each possible trinucleotide, for the 3 different phases
 */
public class StatisticsInThreePhases {
    private TrinucleotidsOccurrences[] triPhases;
    private DinucleotidsOccurrences[] diPhases;

    public StatisticsInThreePhases() {
        this.triPhases = new TrinucleotidsOccurrences[] {
            new TrinucleotidsOccurrences(),
            new TrinucleotidsOccurrences(),
            new TrinucleotidsOccurrences()
        };
        this.diPhases = new DinucleotidsOccurrences[] {
            new DinucleotidsOccurrences(),
            new DinucleotidsOccurrences()
        };
    }

    public void incrementTri(int phase, String trinucleotid) {
        this.triPhases[phase].increment(trinucleotid);
    }
    public void incrementDi(int phase, String dinucleotid) {
        this.diPhases[phase].increment(dinucleotid);
    }
    public void decrementDi(int phase, String dinucleotid) {
        this.diPhases[phase].decrement(dinucleotid);
    }

    /**
     * Sum the number of occurrence of each trinucleotid per phase.
     * @param other
     */
    public void sum(StatisticsInThreePhases other) {
        other.diPhases[0].forEach( (k,v) -> this.diPhases[0].merge(k, v, Integer::sum) );
        other.diPhases[1].forEach( (k,v) -> this.diPhases[1].merge(k, v, Integer::sum) );
        other.triPhases[0].forEach( (k,v) -> this.triPhases[0].merge(k, v, Integer::sum) );
        other.triPhases[1].forEach( (k,v) -> this.triPhases[1].merge(k, v, Integer::sum) );
        other.triPhases[2].forEach( (k,v) -> this.triPhases[2].merge(k, v, Integer::sum) );
    }

    public int getTriValue(int phase, String trinucleotid) {
        return this.triPhases[phase].get(trinucleotid);
    }

    public int getDiValue(int phase, String dinucleotid) {
        return this.diPhases[phase].get(dinucleotid);
    }

    @Override
    public String toString() {
        return "\nDinucleotides :\n"
            + "Phase 1 : " + this.diPhases[0]
            + "\nPhase 2 : " + this.diPhases[1]
            + "\nTrinucleotides :"
            + "\nPhase 0 : " + this.triPhases[0]
            + "\nPhase 1 : " + this.triPhases[1]
            + "\nPhase 3 : " + this.triPhases[2];
    }
}
