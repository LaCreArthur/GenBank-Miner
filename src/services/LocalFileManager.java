package services;

import bio_classes.Organism;
import bio_classes.Replicon;
import main.Main;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * All methods related to read or write files on the local computer
 */
public class LocalFileManager {

    /**
     * Compare the local organisms list with the newly downloaded one.
     *  - delete local files that doesn't exist anymore in downloaded list,
     *  - remove from downloaded list item that are already present on local folder
     *
     * @param kingdom
     * @param downloaded
     * @return number of organisms already present in local
     */
    public static int diffLocalOrganisms(String kingdom, ArrayList<Organism> downloaded) {
        Main.ui.writeTerm("Importation du dossier local", 0, 1);
        ArrayList<Organism> locals = getLocalOrganisms(kingdom);

        ArrayList<Organism> localsOnly = new ArrayList<>(locals);
        localsOnly.removeAll(downloaded);
        locals.removeAll(localsOnly);
        deleteLocalOrganisms(localsOnly);

        if (downloaded.removeAll(locals))
            Main.ui.writeTerm("Organismes trouv&eacute;s : " + locals.size() + " (dont " + localsOnly.size() + " supprim&eacute;s)", 1, 1);
        else
            Main.ui.writeTerm("Pas d'organismes trouv&eacute;s localement", 2, 1);

//        if ( (countDownload - downloaded.size()) != countLocal )
//            System.err.println("Tout les organismes en local n'ont pas ete enleves de la liste telechargee !");

        return locals.size();
    }

    private static ArrayList<Organism> getLocalOrganisms(String kingdom) {
        ArrayList<Organism> res = new ArrayList<>();

        String folderToExplore = Main.path + "/Results/" + kingdom;

        if (! new File(folderToExplore).exists())
            return res;

        try (Stream<Path> paths = Files.walk(Paths.get(folderToExplore))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    String fileName = filePath.getFileName().toString();
                    if (fileName.endsWith(".xlsx") && !fileName.contains("otal_")) {
                        int pathSize = filePath.getNameCount();
                        
                        String name = fileName.replaceAll(".xlsx","");
                        String sub_group = filePath.subpath(pathSize-2, pathSize-1).toString();
                        String group = filePath.subpath(pathSize-3, pathSize-2).toString();
                        res.add(new Organism(name, kingdom, group, sub_group, null, ""));
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static void deleteLocalOrganisms(ArrayList<Organism> organisms) {
        for(Organism o : organisms) {
            Path f = FileSystems.getDefault().getPath(Main.path+"/"+o.getExcelPath());
            try {
            	//on supprime aussi les donnees de cet organisme dans les fichiers excels des sous-dossiers
                try {
                    SubExcelHandler.removeInSubExcel(o);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Files.delete(f);
                Main.ui.writeTerm("Suppression du fichier : " + o.getExcelPath(), 1, 2);
            } catch (IOException e) {
                Main.ui.writeTerm("Erreur dans la suppression du fichier : " + o.getExcelPath(), 2, 2);
                e.printStackTrace();
            }
        }
    }

    /**
     * @param o         l'organisme pour lequel on doit lancer l'écriture
     * @param r
     * @param directory le dossier dans lequel on va écrire (Genome ou Gene)
     */
    public static void write(Organism o, Replicon r, String directory) {
        if (!directory.equalsIgnoreCase("Genome") &&
            !directory.equalsIgnoreCase("Gene")) {
            System.err.println("--Erreur LocalFileManager.write() : le répertoire n'est pas bon");
        }
        String pathOrg = Main.path + "/" + directory + "/" + o.getFilePath()+"/"+o.name;
        File file = new File(pathOrg);
        boolean allDirsCreated = true; //TODO opti de ce bool ?
        boolean someDirsMissing = !file.exists();
        if (someDirsMissing) allDirsCreated = file.mkdirs();
        if (allDirsCreated) {
            try {
                if (r != null) {//write sequence
                    PrintWriter writer = new PrintWriter(file + "/" + r.name + "_" + r.id + ".txt", "UTF-8");
                    writeReplicon(r, writer);
                    writer.close();
                } else {//write genome
                    PrintWriter writer = new PrintWriter(file + "/Genome.txt", "UTF-8");
                    writeOrganism(o, writer);
                    writer.close();
                }

            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("--Impossible de creer les dossiers pour l organisme : " + o.name);
        }
    }

    private static void writeReplicon(Replicon r, PrintWriter writer) {
        r.sequenceList.stream().filter(SequenceTester::isValid).forEach(sb -> writer.print(sb.toString()));
        writer.println();
    }

    private static void writeOrganism(Organism o, PrintWriter writer) {
        for (Replicon r : o.replicons) {
            for (StringBuilder sb : r.sequenceList) {
                writer.print(sb.toString());
            }
        }
        writer.println();
    }

    public static void archiveAll() {
        File fileGene = new File(Main.path + "/Gene");
        if (fileGene.exists()) {
            startArchivage(fileGene);
        }

        File fileGenome = new File(Main.path + "/Genome");
        if (fileGenome.exists()) {
            startArchivage(fileGenome);
        }

        File fileResults = new File(Main.path + "/Results");
        if (fileResults.exists()) {
            startArchivage(fileResults);
        }
    }

    private static boolean startArchivage(File rep) {
        LinkedList<File> list_ss_doss = getListSSRepertories(rep);

        list_ss_doss.forEach(LocalFileManager::startArchivage);

        LinkedList<File> listFilesForZip;
        FilenameFilter filters;

        if (list_ss_doss.size() == 0) {
            //nous sommes dans une extrémité
            filters = (dir, name) -> (name.endsWith(".txt")) || (name.endsWith(".xlsx"));
        } else {
            filters = (dir, name) -> (name.endsWith(".zip")) || (name.endsWith(".xlsx"));
        }
        listFilesForZip = getListFilesFiltered(rep, filters);
        String parent = rep.getParent();
        String currentDir = rep.getName();

        boolean allFilesWereZipped = true;
        try {
            //create zip here with the name currentRep

            File fileZip = new File(parent + "/" + currentDir + ".zip");
            if (fileZip.exists()) fileZip.delete();

            FileOutputStream fos = new FileOutputStream(parent + "/" + currentDir + ".zip");
            ZipOutputStream zip = new ZipOutputStream(fos);

            for (File aListFilesForZip : listFilesForZip) {
                //put this file in the zip
                putFileInZip(aListFilesForZip, zip);
            }

            zip.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
            allFilesWereZipped = false;
        }

        if (allFilesWereZipped) {
            //remove currentDir and its files
            deleteDir(rep);
            return true;
        } else
            return false;
    }


    private static LinkedList<File> getListSSRepertories(File rep) {
        LinkedList<File> list = new LinkedList<>();
        File[] arrayFiles = rep.listFiles();

        if (arrayFiles == null) return list; // rep is not created

        for (File f : arrayFiles) {
            if (f.isDirectory() && (!f.getName().startsWith("."))) {
                list.add(f);
            }
        }
        return list;
    }


    private static LinkedList<File> getListFilesFiltered(File rep, FilenameFilter filters) {
        LinkedList<File> list = new LinkedList<>();

        File[] arrayFiles = rep.listFiles(filters);

        for (File f : arrayFiles) {
            if (f.isFile())
                list.add(f);
        }

        return list;
    }


    private static void putFileInZip(File file, ZipOutputStream zipOut) throws IOException {
        FileInputStream fileInput = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fileInput.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        zipOut.closeEntry();
        fileInput.close();
    }

    private static void deleteDir(File dir) {
        File[] listFiles = dir.listFiles();
        if (listFiles != null) {
            for (File f : listFiles) {
                deleteDir(f);
            }
        }
        dir.delete();
    }
}
