package services;

import bio_classes.Organism;
import bio_classes.Replicon;
import jxBrowser.ProgressBar;
import main.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Contains all methods related to the database connection
 */
public class DataManager {

	private static String repliconType = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nuccore&rettype=gb&retmode=text&id=";

	private static String linkEukaryotes = "https://www.ncbi.nlm.nih.gov/genomes/Genome2BE/genome2srv.cgi?action=download&" +
			"orgn=&report=euks&status=50|40|30|20|%3Bnopartial|noanomalous|&group=--%20All%20Eukaryota%20--&subgroup=--%20All%20Eukaryota%20--&format=csv";
	private static String linkProkaryotes = "https://www.ncbi.nlm.nih.gov/genomes/Genome2BE/genome2srv.cgi?action=download&" +
			"orgn=&report=proks&status=50|40|30|20|%3Bnopartial|noanomalous|&group=--%20All%20Prokaryotes%20--&subgroup=--%20All%20Prokaryotes%20--&format=csv";
	private static String linkViruses = "https://www.ncbi.nlm.nih.gov/genomes/Genome2BE/genome2srv.cgi?action=download&" +
			"orgn=&report=viruses&status=50|40|30|20|%3B&host=All&group=--%20All%20Viruses%20--&subgroup=--%20All%20Viruses%20--&format=csv";

	/**
	 * Connect to the database to get the CDS's of a replicon
	 */
	public static void getSequences(Replicon replicon) throws IOException {
		String repliconURL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nuccore&rettype=fasta_cds_na&retmode=text&id=";
		URL request = new URL(repliconURL + replicon.id);
		BufferedReader buff = new BufferedReader(new InputStreamReader(request.openStream()));

		StringBuilder sequence = new StringBuilder();
		String line;
		while ((line = buff.readLine()) != null) {
			if (line.isEmpty() || line.charAt(0) == '>') {
				if (sequence.length()>0) {
//					System.out.println("sequence acquired");
					replicon.sequenceList.add(sequence);
					sequence = new StringBuilder();
				}
			} else {
				sequence.append(line);
			}
		}
	}

	public static ArrayList<Organism> getOrganisms(String kingdomName, ProgressBar progressBar) throws IOException {
		ArrayList<Organism> result = new ArrayList<>();
		URL kingdomURL = initURL(kingdomName, progressBar);
		BufferedReader buff; // buffèèèèèère readèèère
		String line;
		Organism tmp;

		// recuperation de la premiere ligne (contient les noms des colonnes)
		try {
			buff = new BufferedReader(new InputStreamReader(kingdomURL.openStream()));
            line = buff.readLine();
		} catch (java.net.UnknownHostException e) {
			Main.mp.pause("(getOrganisms) Le serveur ou le proxy ne repond pas");
			throw e;
		}

		if (line == null)
            throw new IOException("La ligne des noms de colonnes est vide");

		int[] indexes = getColumnsIndexes(line);

        // boucle sur les organismes (parcours des lignes du buffèèère)
		while ((line = buff.readLine()) != null) {
			tmp = Organism_init(line, kingdomName, indexes);
			if ((tmp != null) && !(isAlreadyHere(result, tmp.name)))
				result.add(tmp);
			progressBar.increment();
		}

		return result;
	}

	private static URL initURL(String kingdomName, ProgressBar progressBar){
		URL kingdomURL = null;
		try {
			if (kingdomName.equals("Eucaryotes")) {
				kingdomURL = new URL(linkEukaryotes);
				progressBar.reset(progressBar.nbEuk);
			} else if (kingdomName.equals("Procaryotes")) {
				kingdomURL = new URL(linkProkaryotes);
				progressBar.reset(progressBar.nbPro);
			} else if (kingdomName.equals("Virus")) {
				kingdomURL = new URL(linkViruses);
				progressBar.reset(progressBar.nbVir);
			} else
				throw new IllegalArgumentException("Le royaume \'"+kingdomName+"\' est inconnu.");

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return kingdomURL;
	}

    /** getColumnsIndexes permet d'obtenir l'indice des colonnes du CSV d'un royaume qui nous intéresse
     * Name, group, subGroup, replicons (segmemts pour les virus) et bioproject
     * Est appelée par getOrganisms
     * @param line la première ligne du CSV d'un royaume, elle contient le nom des colonnes
     * @return t, un tableau de 5 entiers contenant l'indice des colonnes qui nous intéressent
     */

	private static int[] getColumnsIndexes(String line) {
		int i;
		int[] t = new int[5];
		String[] world = line.split(",");
		for (i = 0; i < world.length; i++) {
			if ((world[i]).equals("#Organism/Name"))
				t[0] = i;
			else if ((world[i]).equals("Group"))
				t[1] = i;
			else if ((world[i]).equals("SubGroup"))
				t[2] = i;
			else if ((world[i]).equals("Replicons") || (world[i]).equals("Segmemts"))
				t[3] = i;
			else if ((world[i]).equals("Modify Date"))
				t[4] = i;
		}
		return t;
	}

    /** Organism_init initialise un organisme avec ses caractéristiques s'il doit être traité
     * et return null sinon (pas de réplicon)
     * Est appelée par getOrganisms pour chaque ligne du CSV
     * Appelle getReplicons, le constructeur de Organism
     * @param line la ligne du CSV du royaume à traiter (string)
     * @param kingdom le royaume auquel appartient la ligne (string)
     * @param indexes un tableau contenant les indices des colonnes à regarder (réplicons, nom, etc...)
     * @return
     */

	private static Organism Organism_init(String line, String kingdom, int[] indexes) {
		// Regex qui banalise les chaines de caracteres
		String[] words = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

		//rajouter un traitement des noms (caractères spéciaux)?
		String name = words[indexes[0]];
		ArrayList<Replicon> replicons = getReplicons(words[indexes[3]]);
		Organism org = null;

		// s'il n'y a pas de replicons, il n'y a pas besoin de traitement
		if (replicons != null) {
			String modifyDate = words[indexes[4]];
			String group = words[indexes[1]];
			String subgroup = words[indexes[2]];

			org = new Organism(name, kingdom, group, subgroup, replicons, modifyDate);
		}
		return org;
	}

    /** getReplicons est une fonction qui prend le string contenant tous les noms de réplicons
     * (aussi en doublon) et qui rend la liste des réplicons à traiter en un exemplaire.
     *  Pour chaque réplicon à traiter, on a son nom et son identifiant.
     *  Est appelée par Organism_init
     *  Appelle le constructeur de Replicons
     *  S'il n'y a pas de réplicon à traiter, on renvoie null.
     * @param word la liste des réplicons tel qu'écrite dans la BDD en un bloc
     * @return la liste des réplicons à traiter.
     */
	private static ArrayList<Replicon> getReplicons(String word) {
		if (word.equals("-")) {
			return null;
		}
		ArrayList<Replicon> result = new ArrayList<>();
		//ArrayList<String> unknownReplicon = new ArrayList<>();
		String name;
		String[] tmp, identifs;

		for (String list : word.split(";")) {
			name = null;
			tmp = list.split(":");
			// il n'y a pas de nom pour le réplicon
			if (tmp.length == 1) {
				identifs = tmp[0].split("/");
				// le réplicon a un nom
			} else {
				name = tmp[0];
				identifs = tmp[1].split("/");
			}
			//on regarde s'il y a un identifiant qui commence par NC
			for (String id : identifs) {
				if (id.startsWith("NC")) {
					if ((name == null) || (name.contains("nknown")) || (name.length() < 3))
						//unknownReplicon.add(id);
						result.add(new Replicon(id, "unknown"));
					else
						result.add(new Replicon(id, name));
				}
			}
		}

		/*if (unknownReplicon.size() > 0)
			result.addAll(findRepliconType(unknownReplicon));*/
		if (result.size() == 0)
			return null;
		return result;
	}

	public static ArrayList<Replicon> findRepliconType (ArrayList<String> unknownReplicon){
		String line, identifiant = repliconType;
		ArrayList<Replicon> result = new ArrayList<>();
		String [] tmp;
		int compt = 0;
		for (String id : unknownReplicon){
			identifiant = identifiant + id +",";
		}

		try {
			URL url = new URL(identifiant);
			BufferedReader buff = new BufferedReader(new InputStreamReader(url.openStream()));
			while ((line = buff.readLine()) != null){
				if (line.startsWith("LOCUS")){
					tmp = line.split(" +");
					result.add(new Replicon(unknownReplicon.get(compt), tmp[4]));
					compt++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static boolean isAlreadyHere (ArrayList<Organism> list, String orgaName){
		for (Organism org : list) {
			if (orgaName.equals(org.name))
				return true;
		}
		return false;
	}

}
