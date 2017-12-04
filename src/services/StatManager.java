package services;


import bio_classes.Replicon;
import stat_classes.StatisticsInThreePhases;

/**
 * All methods related to computation of the statistics
 */
public class StatManager {

    public static void analyzeReplicon(Replicon r) {
        for (StringBuilder s : r.sequenceList) {
//            if (threadSuspended) pause("D&eacute;but du traitement d'une séquence");
            if (!SequenceTester.isValid(s)) {
                r.sequences_dropped++;
            } else {
                // Counting letters
                try {
                    StatisticsInThreePhases seq_occ = StatManager.countNucleotids(s);
                    StatisticsInThreePhases seq_pref = StatManager.computePreferentialPhases(seq_occ);
                    r.occurrences.sum(seq_occ);
                    r.preferentialPhases.sum(seq_pref);
                    r.sequences_analyzed++;
                } catch (Exception e) {
                    r.sequences_dropped++;
                }
            }
            //System.out.println("------bonnes : " + r.sequences_analyzed + "\tmauvaises : " + r.sequences_dropped);
        }
    }

    /**
     * Analyze a string to check : start, stop, size
     * and count the occurences of di/tri-nucleotides
     */
    private static StatisticsInThreePhases countNucleotids(StringBuilder sequence) throws Exception {

        StatisticsInThreePhases result = new StatisticsInThreePhases();

        // we do not count the last trinucleotid for phase 0,
        // that is why the loop iterator stops at : (length-1)-3
        String currentTri;
        String currentDi = null;
        for (int i = 0; i < sequence.length()-3; i++) {
            currentDi = sequence.substring(i, i+2);
            try {
                result.incrementDi(i%2, currentDi);
            } catch (Exception e) {
                throw new Exception("Invalid sequence, containing the dinucleotid \'" + currentDi + "\'.");
            }

            currentTri = sequence.substring(i, i+3);
            try {
                result.incrementTri(i%3, currentTri);
            } catch (Exception e) {
                throw new Exception("Invalid sequence, containing the trinucleotid \'" + currentTri + "\'.");
            }
        }

        // we need to remove the last dinucleotid so that
        // there is as much in both phases
        if (currentDi != null && sequence.length()%2==0) {
            result.decrementDi(0, currentDi);
        }

        return result;
    }

    private static StatisticsInThreePhases computePreferentialPhases (StatisticsInThreePhases seq_occ) {
        StatisticsInThreePhases result = new StatisticsInThreePhases();
        String[] nucleotides = new String[] {"A", "C", "G", "T"};
        int[] occurrencesTri = new int[3];
        int[] occurrencesDi = new int [2];

        for (String char1 : nucleotides) {
            for (String char2 : nucleotides) {
                occurrencesDi[0] = seq_occ.getDiValue(0, char1+char2);
                occurrencesDi[1] = seq_occ.getDiValue(1, char1+char2);

                //compute preferential phase(s) for dinucleotid
                if (occurrencesDi[0]>occurrencesDi[1]) {
                    result.incrementDi(0, char1 + char2);
                } else {
                    result.incrementDi(1, char1 + char2);
                    if (occurrencesDi[0] == occurrencesDi[1])
                        result.incrementDi(0, char1 + char2);
                }

                for (String char3 : nucleotides) {
                    String trinucleotid = char1+char2+char3;
                    occurrencesTri[0] = seq_occ.getTriValue(0, trinucleotid);
                    occurrencesTri[1] = seq_occ.getTriValue(1, trinucleotid);
                    occurrencesTri[2] = seq_occ.getTriValue(2, trinucleotid);

                    //compute preferential phase(s) for the trinucleotid
                    if (occurrencesTri[0] > occurrencesTri[1]){
                        if (occurrencesTri[0] > occurrencesTri[2]){
                            result.incrementTri(0, trinucleotid);
                        }
                        else {
                            result.incrementTri(2, trinucleotid);
                            if (occurrencesTri[0] == occurrencesTri[2])
                                result.incrementTri(0, trinucleotid);
                        }
                    } else { //1>=0
                        if (occurrencesTri[2] > occurrencesTri[1]){
                            result.incrementTri(2, trinucleotid);
                        }
                        else {// 1>=2
                            result.incrementTri(1, trinucleotid);
                            if (occurrencesTri[1] == occurrencesTri[0])
                                result.incrementTri(0, trinucleotid);
                            if (occurrencesTri[1] == occurrencesTri[2])
                                result.incrementTri(2, trinucleotid);
                        }
                    }//else

                }
            }
        }
        return result;
    }
}
