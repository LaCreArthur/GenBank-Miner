package services;

import bio_classes.Organism;
import bio_classes.Replicon;
import main.Main;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;


import static java.awt.Color.WHITE;

/**
 * Classe chargee de generer les fichiers Excel
 */
public class ExcelHandler {

	protected static String[] allBases = {"AAA", "AAC", "AAG", "AAT", "ACA", "ACC", "ACG", "ACT", "AGA", "AGC", "AGG", "AGT", "ATA", "ATC", "ATG", "ATT",
            "CAA", "CAC", "CAG", "CAT", "CCA", "CCC", "CCG", "CCT", "CGA", "CGC", "CGG", "CGT", "CTA", "CTC", "CTG", "CTT",
            "GAA", "GAC", "GAG", "GAT", "GCA", "GCC", "GCG", "GCT", "GGA", "GGC", "GGG", "GGT", "GTA", "GTC", "GTG", "GTT",
            "TAA", "TAC", "TAG", "TAT", "TCA", "TCC", "TCG", "TCT", "TGA", "TGC", "TGG", "TGT", "TTA", "TTC", "TTG", "TTT",
            "AA", "AC", "AG", "AT", "CA", "CC", "CT", "CG", "GA", "GC", "GG", "GT", "TA", "TC", "TG", "TT"};

    protected static final String[] listSheets = {"Sum_Chromosome", "Sum_Mitochondrion", "Sum_Chloroplast",
            "Sum_Plasmid", "Sum_DNA", "Sum_Unknown"};

    protected static final String[] typeReplicon = {"Chromosome", "Mitochondrion", "Chloroplast",
            "Plasmid", "DNA", "Unknown"};

    protected static final String[] colonnes = {"B", "C", "D", "E", "F", "G", "N", "O", "P", "Q"};



    /**
     * @return cree le fichier excel avec les statisques
     */
    public static boolean creatExcel(Organism organism) {
        if (organism.replicons == null) return true;
        String fichierOrgansim = Main.path + "/" + organism.getExcelPath();
        setFolderHierarchy(organism);
        boolean res = false;
        File file = new File(fichierOrgansim);
        if (!file.isFile() && !file.exists()) {
            XSSFWorkbook wb = new XSSFWorkbook();
            initInfoSheet(wb, organism);
            int[] statTypereplicon = new int[8];
            for (Replicon replicon : organism.replicons) {
                writeStatsInExcel(wb, replicon);
                creatSumsheet(replicon, wb);
                writeStatsSumInExcel(fichierOrgansim, wb, replicon);
                statTypereplicon = countRepliconType(replicon, statTypereplicon);
                statTypereplicon[6] += replicon.sequences_analyzed;
                statTypereplicon[7] += replicon.sequences_dropped;
            }
            updateInfoSheet(wb, statTypereplicon);
            writeOnDisk(fichierOrgansim, wb);

            //ajout des stats dans les sous-fichiers excel
            SubExcelHandler.writeInSubExcel(organism, wb);

            res = true;
        }
        return res;
    }


    /**
     * methode de creation d'onglet "sum"
     */
    public static void creatSumsheet(Replicon replicon, XSSFWorkbook wb) {
        String onglet = "Sum_" + typeReplicon(replicon.name);
        if (wb.getSheet(onglet) == null) {
            initSheet(wb, onglet);
            wb.setSheetOrder(onglet, 1);
        }
    }

    public static void setNumericCell(Cell cell, double v) {
        cell.setCellType(CellType.NUMERIC);
        cell.setCellValue(v);
    }

    /**
     * methode qui cree les onglets
     */
    public static void writeStatsSumInExcel(String fichierOrgansim, XSSFWorkbook wb, Replicon replicon) {
        String sheetName = "Sum_" + typeReplicon(replicon.name);
        Sheet sheet = wb.getSheet(sheetName);
        //wb.setSheetOrder(cds, 0);
        Row row = null;
        Cell cell;
        int valeur;
        int phase;
        for (int i = 0; i < 64; i++) {
            //trinucleotides
            row = sheet.getRow(1 + i);
            phase = 0;
            for (int j = 1; j < 7; j++) {
                cell = row.getCell(j);
                valeur = (int) cell.getNumericCellValue();
                if ((j & 1) == 1) {  //phases
                    setNumericCell(cell, valeur + replicon.occurrences.getTriValue(phase, allBases[i]));
                    //Pref phase
                    cell = row.getCell(phase + 7);
                    setNumericCell(cell, replicon.preferentialPhases.getTriValue(phase, allBases[i]));
                    phase++;
                }
            }
            if (i < 16) {    //calcul des dinucl�otide
                phase = 0;
                for (int j = 1; j < 5; j++) {
                    cell = row.getCell(j + 12);
                    valeur = (int) cell.getNumericCellValue();
                    if ((j & 1) == 1) {
                        setNumericCell(cell, valeur + replicon.occurrences.getDiValue(phase, allBases[i + 64]));
                        //Pref phase
                        cell = row.getCell(phase + 17);
                        setNumericCell(cell, replicon.preferentialPhases.getDiValue(phase, allBases[i + 64]));
                        phase++;
                    }
                }
            }

            //complements informations
            if (i == 19) {
                cell = row.getCell(12);
                valeur = (int) cell.getNumericCellValue();
                cell.setCellValue(valeur + replicon.sequences_analyzed);
            }
            if (i == 20) {
                cell = row.getCell(12);
                valeur = (int) cell.getNumericCellValue();
                cell.setCellValue(valeur + replicon.sequences_dropped);
            }
        }
        writeOnDisk(fichierOrgansim, wb);
    }

    public static void writeOnDisk(String fichierOrgansim, XSSFWorkbook wb) {
        try {
            FileOutputStream fileOut = new FileOutputStream(fichierOrgansim);
            wb.write(fileOut);
            fileOut.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void writeStatsInExcel(XSSFWorkbook wb, Replicon replicon) {
        String sheetName = replicon.name + "_" + replicon.id;
        Sheet sheet = initSheet(wb, sheetName);

        Row row = null;
        Cell cell;
        int phase;
        for (int i = 0; i < 64; i++) {
            //trinucleotides
            row = sheet.getRow(1 + i);
            phase = 0;
            for (int j = 1; j < 7; j++) {
                cell = row.getCell(j);
                if ((j & 1) == 1) {  //phases
                    setNumericCell(cell, replicon.occurrences.getTriValue(phase, allBases[i]));
                    //Pref phase
                    cell = row.getCell(phase + 7);
                    setNumericCell(cell, replicon.preferentialPhases.getTriValue(phase, allBases[i]));
                    phase++;
                }
            }
            if (i < 16) {    //calcul des dinucl�otide
                phase = 0;
                for (int j = 1; j < 5; j++) {
                    cell = row.getCell(j + 12);
                    if ((j & 1) == 1) {
                        setNumericCell(cell, replicon.occurrences.getDiValue(phase, allBases[i + 64]));
                        //Pref phase
                        cell = row.getCell(phase + 17);
                        setNumericCell(cell, replicon.preferentialPhases.getDiValue(phase, allBases[i + 64]));
                        phase++;
                    }
                }
            }

            //complements informations
            if (i == 19) {
                cell = row.getCell(12);
                cell.setCellValue(replicon.sequences_analyzed);
            }
            if (i == 20) {
                cell = row.getCell(12);
                cell.setCellValue(replicon.sequences_dropped);
            }
        }
    }


    /**
     * @return le type de replicon
     */
    public static String typeReplicon(String name) {
        if (name.toLowerCase().contains("chromosome"))
            return "Chromosome";
        else if (name.toLowerCase().contains("mitochondrion"))
            return "Mitochondrion";
        else if (name.toLowerCase().contains("chloroplast"))
            return "Chloroplast";
        else if (name.toLowerCase().contains("plasmid"))
            return "Plasmid";
        else if (name.toLowerCase().contains("dna"))
            return "DNA";
        else return "Unknown";
    }

    /**
     * methode d'initialisation d'onglet
     *
     * @return l'onget
     */
    protected static Sheet initSheet(XSSFWorkbook wb, String name) {
        Sheet sheet = null;
        // test pour eviter
        if (wb.getSheet(name) == null) {
            String[] phases = {"Phase 0", "Freq Phase 0", "Phase 1", "Freq Phase 1",
                    "Phase 2", "Freq Phase 2",
                    "Pref Phase 0", "Pref Phase 1", "Pref Phase 2"};

            sheet = wb.createSheet(name);
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);

            for (int i = 0; i < 9; i++) {    //Entete trinucleotide
                cell = row.createCell(i + 1);
                cell.setCellValue(phases[i]);
                if (i < 6)
                    cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.CENTER));
                else
                    cell.setCellStyle(initStyle("brown_title", wb, HorizontalAlignment.CENTER));
                sheet.setColumnWidth(i+1, 12*256);
            }

            cell = row.createCell(11);
            cell.setCellValue("STATISTIQUES DINUCLEOTIDES");
            cell.setCellStyle(initStyle("yellow_title", wb, HorizontalAlignment.CENTER));
            sheet.setColumnWidth(11, 26*256);

            for (int i = 0; i < 4; i++) {    //Entete dinucleotide
                cell = row.createCell(i + 13);
                cell.setCellValue(phases[i]);
                cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.CENTER));
                if (i < 2) { // entete pref
                    cell = row.createCell(i + 17);
                    cell.setCellValue(phases[i + 6]);
                    cell.setCellStyle(initStyle("brown_title", wb, HorizontalAlignment.CENTER));
                    sheet.setColumnWidth(i+17, 12*256);
                }
               sheet.setColumnWidth(i+13, 12*256);
            }
            //trinucleotides
            for (int i = 0; i < 64; i++) {
                row = sheet.createRow(1 + i);
                cell = row.createCell(0);
                cell.setCellType(CellType.STRING);
                cell.setCellValue(allBases[i]);
                cell.setCellStyle(initStyle("bold", wb, HorizontalAlignment.LEFT));
                if ((i & 1) == 1) { //couleur alterne
                    cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.LEFT));
                    creat_cells_of_row(row, 1, 6, initStyle("grey_light", wb, HorizontalAlignment.RIGHT));    //phase et freq
                    creat_cells_of_row(row, 7, 9, initStyle("brown_light", wb, HorizontalAlignment.RIGHT));    //pref
                } else {
                    creat_cells_of_row(row, 1, 9, null);
                }
                if (i < 16) { //Dinucle
                    cell = row.createCell(12);
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(allBases[i + 64]);
                    cell.setCellStyle(initStyle("bold", wb, HorizontalAlignment.LEFT));
                    if ((i & 1) == 1) {
                        cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.LEFT));
                        creat_cells_of_row(row, 13, 18, initStyle("grey_light", wb, HorizontalAlignment.RIGHT));    //phase et freq
                        creat_cells_of_row(row, 17, 18, initStyle("brown_light", wb, HorizontalAlignment.RIGHT));    //pref
                    } else {
                        creat_cells_of_row(row, 13, 18, null);
                    }
                }
                //totaux Dinucleo
                if (i == 16) {
                    //row = sheet.getRow(i);
                    cell = row.createCell(12);
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue("Total");
                    cell.setCellStyle(initStyle("red_title", wb, HorizontalAlignment.LEFT));
                    creat_cells_of_row(row, 13, 16, initStyle("red", wb, HorizontalAlignment.RIGHT));
                }
                //informations
                if (i == 18) {
                    cell = row.createCell(11);
                    cell.setCellValue("Informations");
                    cell.setCellStyle(initStyle("yellow_title", wb, HorizontalAlignment.CENTER));
                }
                if (i == 19) {
                    cell = row.createCell(11);
                    cell.setCellValue("Number of cds sequences");
                    cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.CENTER));
                    cell = row.createCell(12);
                    setNumericCell(cell, 0);
                    cell.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));
                }
                if (i == 20) {
                    cell = row.createCell(11);
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue("Number of invalid cds");
                    cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.CENTER));
                    cell = row.createCell(12);
                    setNumericCell(cell, 0);
                    cell.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));
                }
            }
            row = sheet.createRow(65);
            cell = row.createCell(0);
            cell.setCellType(CellType.STRING);
            cell.setCellValue("Total");

            cell.setCellStyle(initStyle("red", wb, HorizontalAlignment.LEFT));
            creat_cells_of_row(row, 1, 6, initStyle("red", wb, HorizontalAlignment.RIGHT));

            SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
            ConditionalFormattingRule zero_rule =
                    sheetCF.createConditionalFormattingRule(ComparisonOperator.EQUAL, "0");
            FontFormatting font = zero_rule.createFontFormatting();
            font.setFontColorIndex(IndexedColors.RED.index);

            CellRangeAddress[] tri_regions = {
                    CellRangeAddress.valueOf("B2:J65")
            };
            CellRangeAddress[] di_regions = {
                    CellRangeAddress.valueOf("N2:S17")
            };
            sheetCF.addConditionalFormatting(tri_regions, zero_rule);
            sheetCF.addConditionalFormatting(di_regions, zero_rule);

            //on ecrit les formules dans les bonnes cases
            totalPhases(sheet);
            initFormula(sheet);
        } else {
            sheet = wb.getSheet(name);
        }
        return sheet;
    }

    /**
     * creer les cellules de la borne inf et la borne sup incluse
     */
    private static void creat_cells_of_row(Row row, int borneinf, int bornesup, CellStyle style) {
        for (int i = borneinf; i <= bornesup; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(style);
        }
    }

    private static int[] countRepliconType(Replicon replicon, int[] statTypereplicon) {
        switch (typeReplicon(replicon.name)) {
            case "Chromosome":
                statTypereplicon[0]++;
                break;
            case "Mitochondrion":
                statTypereplicon[1]++;
                break;
            case "Chloroplast":
                statTypereplicon[2]++;
                break;
            case "Plasmid":
                statTypereplicon[3]++;
                break;
            case "DNA":
                statTypereplicon[4]++;
                break;
            default:
                statTypereplicon[5]++;
        }
        return statTypereplicon;
    }

    private static void updateInfoSheet(XSSFWorkbook wb, int[] statTypereplicon) {
        Sheet sheet = wb.getSheetAt(0);
        Row row;
        Cell cell;

        row = sheet.getRow(6);
        cell = row.getCell(1);
        cell.setCellValue(statTypereplicon[6]); //nombre de cds valides

        row = sheet.getRow(8);
        cell = row.getCell(1);
        cell.setCellValue(statTypereplicon[7]);    //nombre de cds invalides

        int rowid = 3;
        for (int i = 0; i < 6; i++) {
            if (statTypereplicon[i] > 0) {
                if((row = sheet.getRow(rowid))==null){
                	row = sheet.createRow(rowid);
                }
                cell = row.createCell(5);
                cell.setCellValue(typeReplicon[i]);
                cell.setCellStyle(initStyle("green_light_title", wb, HorizontalAlignment.RIGHT));
                cell = row.createCell(6);
                cell.setCellValue(statTypereplicon[i]);
                cell.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));
                rowid = rowid + 1;
            }
        }
    }

    private static void initInfoSheet(XSSFWorkbook wb, Organism org) {
        Sheet sheet = wb.createSheet("General Information");
        Cell cell;
        Row row;
        String[] champs_infos = {"Organism Name", "Modification Date",
                "Number of CDS sequences", "Number of invalids CDS",
                "Number of Organisms", "Genome"};
        row = sheet.createRow(0);
        cell = row.createCell(0);
        cell.setCellValue("Information");
        cell.setCellStyle(initStyle("yellow_title", wb, HorizontalAlignment.LEFT));

        for (int i = 1; i < 6; i++) {
        	row = sheet.createRow(i * 2);
        	if(i==1){
        		cell = row.createCell(5);
                cell.setCellValue(champs_infos[5]);
                cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.CENTER));
                
               /* cell = row.createCell(6);
                cell.setCellValue("Total");
                cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.CENTER));
                */
                sheet.setColumnWidth(5, 20*256);
                sheet.setColumnWidth(6, 5*256);
        	}
            cell = row.createCell(0);
            cell.setCellValue(champs_infos[i - 1]);
            cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.LEFT));
        }
        //creation des cellules
        row = sheet.getRow(2);
        cell = row.createCell(1);
        cell.setCellValue(org.name);
        cell.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));

        row = sheet.getRow(4);
        cell = row.createCell(1);
        cell.setCellValue(org.modify_date);
        cell.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));

        row = sheet.getRow(6); //n of cds sequence
        cell = row.createCell(1);
        cell.setCellValue(0);
        cell.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));

        row = sheet.getRow(8); //invalides cds
        cell = row.createCell(1);
        cell.setCellValue(0);
        cell.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));

        row = sheet.getRow(10); //number of organisms
        cell= row.createCell(1);
        cell.setCellValue(1);
        cell.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));
        
        sheet.setColumnWidth(0, 23*256);
        sheet.setColumnWidth(1, 23*256);
    }


    

    public static void setBoldAndColor(XSSFCellStyle style, XSSFFont headerFont, XSSFColor color) {
        headerFont.setBold(true);
        style.setFont(headerFont);
        style.setFillForegroundColor(color);
    }

    /**
     * @return Cellstyle appliqu� cellule
     */
    protected static CellStyle initStyle(String color, XSSFWorkbook wb, HorizontalAlignment align) {

        XSSFCellStyle style = wb.createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont headerFont = wb.createFont();
        style.setFont(headerFont);
        style.setAlignment(align);
        switch (color) {
            case "green_title":
                headerFont.setColor(new XSSFColor(WHITE));
                setBoldAndColor(style, headerFont, new XSSFColor(java.awt.Color.decode("#8CC152"))); // vert clair
                break;
            case "brown_title":
                headerFont.setColor(new XSSFColor(WHITE));
                setBoldAndColor(style, headerFont, (new XSSFColor(java.awt.Color.decode("#967ADC")))); // violet !
                break;
            case "red_title":  //ligne stat
                headerFont.setColor(new XSSFColor(WHITE));
                setBoldAndColor(style, headerFont, new XSSFColor(java.awt.Color.decode("#DA4453"))); // rouge foncé
                break;
            case "inf_zero":
                headerFont.setColor(new XSSFColor(java.awt.Color.decode("#ED5565"))); // rouge clair
                setBoldAndColor(style, headerFont, new XSSFColor(WHITE));
                break;
            case "green_light_title":    //ligne type
                setBoldAndColor(style, headerFont, new XSSFColor(java.awt.Color.decode("#A0D468"))); // vert clair
                break;
            case "red":  //ligne stat
                setBoldAndColor(style, headerFont, new XSSFColor(java.awt.Color.decode("#DA4453"))); // rouge foncé
                break;
            case "bold":  //ligne stat
                setBoldAndColor(style, headerFont, new XSSFColor(WHITE));
                break;
            case "yellow_title":  //titre information
                setBoldAndColor(style, headerFont, new XSSFColor(java.awt.Color.decode("#FFCE54"))); // jaune
                break;
            case "brown_light":    //ligne stat
                style.setFillForegroundColor(new XSSFColor(java.awt.Color.decode("#FFCE54"))); // jaune
                break;
            case "grey_light":  //ligne stat
                style.setFillForegroundColor(new XSSFColor(java.awt.Color.decode("#E6E9ED"))); // gris clair
                break;
            case "blue_light":  //ligne stat
                style.setFillForegroundColor(new XSSFColor(java.awt.Color.decode("#4FC1E9"))); // bleu clair
                break;
            case "formula":
            	style.setFillForegroundColor(new XSSFColor(java.awt.Color.decode("#E6E9ED")));
                DataFormat format = wb.createDataFormat();
                style.setDataFormat(format.getFormat("0.00"));
            	break;
            default:
                break;
        }
        return style;
    }

    /**
     * @return cree les dossiers
     */
    public static boolean setFolderHierarchy(Organism o) {
        if (o == null)
            return false;

        return new File(Main.path + "/Results/" + o.getFilePath()).mkdirs();
    }

    //factorisation du code
    public static void setFormula(Cell out, String formule) {
        out.setCellType(CellType.NUMERIC);
        out.setCellFormula(formule);
        CellStyle style = out.getSheet().getWorkbook().createCellStyle();
        DataFormat format = out.getSheet().getWorkbook().createDataFormat();
        style.setDataFormat(format.getFormat("0.00"));
        out.setCellStyle(style);
    }


    public static void initFormula(Sheet sheet) {
        Row row;
        Cell cell;
        for (int i = 0; i < 64; i++) {
            String formule = null;
            //trinucleotides
            row = sheet.getRow(1 + i);
            for (int j = 1; j < 7; j++) {
                cell = row.getCell(j);
                if ((j & 1) != 1) {  //freqs
                    formule = "(" + colonnes[j - 2] + (i + 2) + "/" + colonnes[j - 2] + "66" + ")*100";
                    setFormula(cell, formule);
                    if((i&1) == 1){
                    cell.setCellStyle(initStyle("formula", (XSSFWorkbook) cell.getSheet().getWorkbook(), HorizontalAlignment.RIGHT));
                    }
                }
            }
            if (i < 16) {    //calcul des dinucleotide
                for (int j = 1; j < 5; j++) {
                    cell = row.getCell(j + 12);
                    if ((j & 1) != 1) { //freq
                        formule = "(" + colonnes[j + 4] + (i + 2) + "/" + colonnes[j + 4] + "18" + ")*100";
                        setFormula(cell, formule);
                        if((i&1) == 1){
                            cell.setCellStyle(initStyle("formula", (XSSFWorkbook) cell.getSheet().getWorkbook(), HorizontalAlignment.RIGHT));
                        }
                    }
                }
            }
        }
    }

    /**
     * Ecrit les formules pour les phases préférentielles dans l'onglet s
     *
     * @param s l'onglet à modifier
     */
    private static void totalPhases(Sheet s) {
        Row r;
        Cell c;
        for (int j = 0; j < 3; j++) {     //totaux des phases
            String sum;
            r = s.getRow(65);    //calcul des totaux trinucléotide
            c = r.getCell(j * 2 + 1);
            sum = "SUM(" + colonnes[j * 2] + "2" + ":" + colonnes[j * 2] + "65)";
            c.setCellFormula(sum);
            //somme des freqs
            c = r.getCell(j * 2 + 2);
            sum = "SUM(" + colonnes[j * 2 + 1] + "2" + ":" + colonnes[j * 2 + 1] + "65)";
            c.setCellFormula(sum);
            if (j < 2) {
                r = s.getRow(17);    //calcul des totaux dinucléotide
                c = r.getCell(j * 2 + 13);
                sum = "SUM(" + colonnes[j * 2 + 6] + "2" + ":" + colonnes[j * 2 + 6] + "17)";
                c.setCellFormula(sum);
                //somme des freqs
                c = r.getCell(j * 2 + 14);
                sum = "SUM(" + colonnes[j * 2 + 7] + "2" + ":" + colonnes[j * 2 + 7] + "17)";
                c.setCellFormula(sum);
            }
        }// fin totaux des phases
    }
}
