package bio_classes;

import java.util.ArrayList;

/**
 * Represent an organism
 */
public class Organism {
    public ArrayList<Replicon> replicons;
    public String name;

    public String kingdom;
    public String group;
    public String sub_group;
    public String modify_date; // format string plus simple à comparer
    public String bioProject = ""; //TODO Rajouté pour fix la ref d  ans ExcelHandler, utile?
    //public String bioProject; inutile pour le moment

    public Organism(String p_name, String p_kingdom, String p_group, String p_sub_group, ArrayList<Replicon> p_list, String p_modifyDate) {
        name = p_name.replaceAll("[\"/\\*?<>|:]+", "");  
        kingdom = p_kingdom.replaceAll("[\"/\\*?<>|:]+", "");
        group = p_group.replaceAll("[\"/\\*?<>|:]+", "");
        sub_group = p_sub_group.replaceAll("[\"/\\*?<>|:]+", "");
        replicons = p_list;
        modify_date = p_modifyDate.replace("\"", ""); //vérifier le format pour l'instant
    }


    /**
     * @return concatenation from kingdom to organism name
     */
    public String getFilePath() {
        return kingdom + "/" + group + "/" + sub_group + "/";
    }

    /**
     * @return path of the Excel file for this organism
     */
    public String getExcelPath() {
        return  "Results/" + getFilePath()+"/"+name+".xlsx";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Organism))
            return false;
        if (obj == this)
            return true;

        Organism org = (Organism) obj;
        return this.getFilePath().equals(org.getFilePath());
    }
}
