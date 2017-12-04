package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import bio_classes.Organism;
import main.Main;

public class SubExcelHandler extends ExcelHandler {
	
    private static ThreadSubExcel thrResults = null;
    private static ThreadSubExcel thrKingdom = null;
    private static ThreadSubExcel thrGroup = null;
    private static ThreadSubExcel thrSubGroup = null;

	private static void initInfoSheetSubExcel(XSSFWorkbook wb, String name) {
        Sheet sheet = wb.createSheet("General Information");
        Cell cell;
        Row row;
        String[] champs_infos = {"Name", "Modification Date", "Number of CDS sequences", "Number of invalids CDS",
                "Number of Organisms"};

        row = sheet.createRow(0);
        cell = row.createCell(0);
        cell.setCellValue("Information");
        cell.setCellStyle(initStyle("yellow_title", wb, HorizontalAlignment.LEFT));
        for (int i = 1; i < 6; i++) {
            row = sheet.createRow(i * 2);
            cell = row.createCell(0);
            cell.setCellValue(champs_infos[i - 1]);
            cell.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.LEFT));
            Cell c = row.createCell(1);;
            switch (i) {
                case 1: {
                    //name
                    c.setCellValue(name);
                    c.setCellType(CellType.STRING);
                    c.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));
                    
                    //Genome
                    c = row.createCell(5);
                    c.setCellValue("Genome");
                    c.setCellStyle(initStyle("green_title", wb, HorizontalAlignment.CENTER));
                    
                    
                    break;
                }
                case 2: {
                    //date
                    Calendar cal = Calendar.getInstance();
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int month = cal.get(Calendar.MONTH);
                    month++;//month est entre 0 et 11
                    int year = cal.get(Calendar.YEAR);
                    c.setCellValue(year + "/" + month + "/" + day);
                    c.setCellType(CellType.STRING);
                    c.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));
                    break;
                }
                case 3:
                case 4:
                case 5: {
                    //Number Org
                    setNumericCell(c, 0);
                    c.setCellStyle(initStyle("blue_light", wb, HorizontalAlignment.RIGHT));
                    break;
                }
                default: {
                    break;
                }
            }
        }
        sheet.setColumnWidth(0, 23*256);
        sheet.setColumnWidth(1, 23*256);
        sheet.setColumnWidth(5, 20*256);
        sheet.setColumnWidth(6, 5*256);
    }
	
	//si additive = false alors on retranche au lieu d'ajouter
    private static void addStats(XSSFWorkbook newWB, XSSFWorkbook wb,boolean additive) {

        //mise a jour de l'onglet "General Information"
        Sheet newSh = newWB.getSheet("General Information");
        Sheet Sh = wb.getSheet("General Information");

        Row row, newRow;
        Cell cell, newCell;

        //n of cds sequence
        cell = Sh.getRow(6).getCell(1);
        newCell = newSh.getRow(6).getCell(1);
        fusionCells(newCell, cell,additive);

        //invalides cds
        cell = Sh.getRow(8).getCell(1);
        newCell = newSh.getRow(8).getCell(1);
        fusionCells(newCell, cell,additive);

        //number of organisms
        cell = Sh.getRow(10).getCell(1);
        newCell = newSh.getRow(10).getCell(1);
        fusionCells(newCell, cell,additive);
        
        //genome
        for (int i=0;i<typeReplicon.length;i++){
        	row = Sh.getRow(3+i);
        	if (row != null) {
        		cell = row.getCell(5);
        		if (cell == null) cell = row.createCell(5);
        		String str = cell.getStringCellValue();
            	if (!str.equalsIgnoreCase("")){
            		boolean found=false;
            		int jfound=-1;
            		int j=0;
            		
            		while (!found && (j<typeReplicon.length)){
            			//on cherche la case de newSh qui a le meme nom
            			newRow = newSh.getRow(3+j);
            			if (newRow == null) newRow = newSh.createRow(3+j);
            			newCell = newRow.getCell(5);
            			if (newCell == null) newCell = newRow.createCell(5);
            			String newStr = newCell.getStringCellValue();
            			if (str.equalsIgnoreCase(newStr)){
            				//on a trouve deux cases ayant le meme nom
            				found=true;
            				jfound=j;
            			}
            			j++;
            		}
            		if (found){
            			cell = Sh.getRow(3+i).getCell(6);
        				newCell = newSh.getRow(3+jfound).getCell(6);
        				if (newCell == null) newCell = newSh.getRow(3+jfound).createCell(6);
        				fusionCells(newCell, cell,additive);
            		}
            		else if (additive){
            			//si on souhaite ajouter des stats mais qu'on n'a pas trouve la bonne case il faut la creer
            			
            			//recherche de la premiere ligne vide
            			j=0;
            			
            			while (!found && (j<typeReplicon.length)){
            				newRow = newSh.getRow(3+j);
                			if (newRow == null) newRow = newSh.createRow(3+j);
                			newCell = newRow.getCell(5);
                			if (newCell == null) newCell = newRow.createCell(5);
                			String newStr = newCell.getStringCellValue();
                			if (newStr.equalsIgnoreCase("")){
                				newCell.setCellValue(str);
                				newCell.setCellStyle(initStyle("green_light_title", newWB, HorizontalAlignment.RIGHT));
                				newCell = newRow.createCell(6);
                				setNumericCell(newCell,0);
                				newCell.setCellStyle(initStyle("blue_light", newWB, HorizontalAlignment.RIGHT));
                				found=true;
                				jfound=j;
                			}
                			j++;
            			}
            			
            			cell = Sh.getRow(3+i).getCell(6);
        				newCell = newSh.getRow(3+jfound).getCell(6);
        				fusionCells(newCell, cell,additive);
            		}
            	}
            		
        	}
        }


        //mise a jour des onglets Sum_
        for (String nameSheet : listSheets) {
            Sh = wb.getSheet(nameSheet);
            if (Sh != null) {
                newSh = initSheet(newWB, nameSheet);//renvoit l'onglet nameSheet si il existe et sinon le cree

                //TODO ces lignes sont appelees souvent => faire une methode qui fait juste ca ?
                int phase;
                for (int i = 0; i < 64; i++) {//boucle ligne
                    //trinucleotides
                    row = Sh.getRow(1 + i);
                    newRow = newSh.getRow(1 + i);
                    phase = 0;
                    for (int j = 1; j < 7; j++) {
                        cell = row.getCell(j);
                        newCell = newRow.getCell(j);
                        if ((j & 1) == 1) {  //phases

                        	fusionCells(newCell, cell,additive);

                            //Pref phase
                            cell = row.getCell(phase + 7);
                            newCell = newRow.getCell(phase + 7);
                            fusionCells(newCell, cell,additive);
                            phase++;
                        }
                    }
                    if (i < 16) {    //calcul des dinucleotide
                        phase = 0;
                        for (int j = 1; j < 5; j++) {
                            cell = row.getCell(j + 12);
                            newCell = newRow.getCell(j + 12);
                            if ((j & 1) == 1) {
                            	fusionCells(newCell, cell,additive);

                                //Pref phase
                                cell = row.getCell(phase + 17);
                                newCell = newRow.getCell(phase + 17);
                                fusionCells(newCell, cell,additive);
                                phase++;
                            }
                        }
                    }
                    //complements informations
                    if (i == 19 || i == 20) {//total cds
                        cell = row.getCell(12);
                        newCell = newRow.getCell(12);
                        fusionCells(newCell, cell,additive);
                    }
                }//fin boucle ligne
            }// fin if Sh!=null
        }//fin for sheetName
    }//fin addStats
	
	
	
	
    /**
     * methode qui répercute les statistiques de l'organisme o dans tous les fichiers excel des répertoires parents
     *
     * @param o  l'organisme a traité
     * @param wb le workbook qui contient les statistiques de o
     * @return un booléen qui montre si les écritures se sont bien déroulées
     */
    public static boolean writeInSubExcel(Organism o, XSSFWorkbook wb) {
        if (o.replicons == null) return true;
        String fichierOrgansim = Main.path + "/" + o.getExcelPath();
        File fileXLSX = new File(fichierOrgansim); //fileXLSX est le fichier cree dans la methode creatExcel
        if (fileXLSX.isFile() && fileXLSX.exists()) {

            if (thrResults != null) {
                waitAllThreads();
            }

            //test multi-threads
            thrResults = new ThreadSubExcel("Results", wb, Main.path + "/Results/Total_Results.xlsx", "Results",true);
            thrResults.start();
            thrKingdom = new ThreadSubExcel("Kingdom", wb, Main.path + "/Results/" + o.kingdom + "/Total_" + o.kingdom + ".xlsx", o.kingdom,true);
            thrKingdom.start();
            thrGroup = new ThreadSubExcel("Group", wb, Main.path + "/Results/" + o.kingdom + "/" + o.group + "/Total_" + o.group + ".xlsx", o.group,true);
            thrGroup.start();
            thrSubGroup = new ThreadSubExcel("SubGroup", wb, Main.path + "/Results/" + o.kingdom + "/" + o.group + "/" + o.sub_group + "/Total_" + o.sub_group + ".xlsx", o.sub_group,true);
            thrSubGroup.start();
        }
        return true;
    }
    
    /**
     * methode qui supprime les statistiques de l'organisme o dans tous les fichiers excel des répertoires parents car il n'est plus a jour
     *
     * @param o  l'organisme à supprimé
     * @return un booléen qui montre si les écritures se sont bien déroulées
     */
    public static boolean removeInSubExcel(Organism o) throws IOException {
        String fichierOrgansim = Main.path + "/" + o.getExcelPath();
        File fileXLSX = new File(fichierOrgansim); //fileXLSX est le fichier cree dans la methode creatExcel
        if (fileXLSX.isFile() && fileXLSX.exists()) {

            XSSFWorkbook wb=null;
        	FileInputStream fileInput = null;
        try {
            	fileInput = new FileInputStream(fileXLSX);
    			wb = new XSSFWorkbook(fileInput);
    		} catch (IOException e) {
                throw e;
    		}
        	
            if (thrResults != null) {
                waitAllThreads();
            }

            //test multi-threads
            thrResults = new ThreadSubExcel("Results", wb, Main.path + "/Results/Total_Results.xlsx", "Results",false);
            thrResults.start();
            thrKingdom = new ThreadSubExcel("Kingdom", wb, Main.path + "/Results/" + o.kingdom + "/Total_" + o.kingdom + ".xlsx", o.kingdom,false);
            thrKingdom.start();
            thrGroup = new ThreadSubExcel("Group", wb, Main.path + "/Results/" + o.kingdom + "/" + o.group + "/Total_" + o.group + ".xlsx", o.group,false);
            thrGroup.start();
            thrSubGroup = new ThreadSubExcel("SubGroup", wb, Main.path + "/Results/" + o.kingdom + "/" + o.group + "/" + o.sub_group + "/Total_" + o.sub_group + ".xlsx", o.sub_group,false);
            thrSubGroup.start();
            
            try {
    			fileInput.close();
    		} catch (IOException e) {
                throw e;
    		}
        }
        
        return true;
    }


    private static void waitAllThreads() {
        try {
            thrResults.join();
            thrKingdom.join();
            thrGroup.join();
            thrSubGroup.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
	
	/*fusionne la valeur de c dans la cellule out
    si additive est faux alors on retranche c a out
    sinon on additionne
    out et c doivent etre de type numeric*/
    public static void fusionCells(Cell out, Cell c,boolean additive) {
        double val = c.getNumericCellValue();
        double newVal = out.getNumericCellValue();
        if (additive) setNumericCell(out, newVal + val);
        else setNumericCell(out, newVal - val);
    }
	
	
	public static class ThreadSubExcel extends Thread {

        public XSSFWorkbook wb;
        public String path;
        public String orgName;
        public boolean addition;

        public ThreadSubExcel(String name, XSSFWorkbook w, String p, String org, boolean add) {
            super(name);
            wb = w;
            path = p;
            orgName = org;
            addition=add;
        }

        public void run() {
            File newXLSX = new File(path);
            XSSFWorkbook newWB = null;
            FileInputStream fileInput = null;
            if (!newXLSX.exists()) {
                //on initialise le nouveau fichier
                newWB = new XSSFWorkbook();
                initInfoSheetSubExcel(newWB, orgName);
            } else {
                //on tente de le lire
                try {
                	fileInput = new FileInputStream(newXLSX);
                    newWB = new XSSFWorkbook(fileInput);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            addStats(newWB, wb,addition);

            if(fileInput !=null)
				try {
					fileInput.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
            
            //on ecrit sur le disque
            File out = new File(path);
            if (out.exists()) out.delete();//on ne peut pas ecrire dans un fichier deja cree
            writeOnDisk(path, newWB);
            
        }
    }
}
