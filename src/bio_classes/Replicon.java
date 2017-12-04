/**
 * Part of the organism containing several CDS
 */

package bio_classes;

import stat_classes.StatisticsInThreePhases;
import java.util.ArrayList;
import java.util.List;

public class Replicon {
	
	public String id;
	public String name; // chromosomes, mithocondrie...
	public List<StringBuilder> sequenceList;
	public StatisticsInThreePhases occurrences;
	public StatisticsInThreePhases preferentialPhases;

	public int sequences_analyzed;
	public int sequences_dropped;
	public String modify_date;

	public Replicon(String id, String n) {
		this.id = id;
		this.name = n;
		this.modify_date = "/";
		this.sequences_analyzed = 0;
		this.sequences_dropped = 0;
		this.occurrences = new StatisticsInThreePhases();
		this.preferentialPhases = new StatisticsInThreePhases();
		this.sequenceList = new ArrayList<StringBuilder>();
	}
}
