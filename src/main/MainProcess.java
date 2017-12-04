package main;

import bio_classes.Organism;
import bio_classes.Replicon;
import services.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static main.Main.ui;
import static services.LocalFileManager.archiveAll;

/**
 * The main entry for the global application algorithm
 */
public class MainProcess extends Thread {

    private Thread t;
    public boolean threadSuspended;

    private String name;
    private List<String> kingdomsToProcess;
    private boolean storeFullGenomes;
    private boolean storeValidSequences;
    private boolean archivesNeeded;

    // le constructeur ne crée pas encore le thread
    MainProcess() {
        this.name = "Main Process";
        threadSuspended = false;
    }

    // Déclenche la création du thread
    public void start(List<String> kingdomsToProcess, boolean storeFullGenomes, boolean storeValidSequences, boolean archivesNeeded) {
        this.kingdomsToProcess = kingdomsToProcess;
        this.storeFullGenomes = storeFullGenomes;
        this.storeValidSequences = storeValidSequences;
        this.archivesNeeded = archivesNeeded;

        if (t == null) {
            t = new Thread(this, name);
            t.start();
        }
    }

    // Déclenche le démarrage du thread
    public void run() {
        mainProcess();
    }


    public synchronized void pause(String s) {
        ui.JSScript("$('#wait').html($(\"#wait\").html() + '" + s + "' ...');");
        ui.JSScript("document.getElementById(\"btnStop\").setAttribute(\"class\",\"btn btn-warning\")");
        ui.JSScript("document.getElementById(\"wait\").innerHTML = " +
            "\"<a style='float: right; margin-right: 8px' class='btn btn-danger' id='btnExit' href='#carousel' data-slide='next' onclick='startArbo();' >Terminer</a>\"");
        while (threadSuspended) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void mainProcess() {
//        //List of Thread which will be launched
//        LinkedList<Thread> listThreads = new LinkedList<>();
        // Kingdom loop
        for (String k : kingdomsToProcess) {
            if (threadSuspended) pause("D&eacute;but d'un traitement du royaume " + k);

            ui.writeTerm("T&eacute;l&eacute;chargement de la liste de organismes pour le royaume : " + k, 0, 1);
            ui.setTreatmentSubtitle("Traitement du royaume " + k);
            ui.label.writeEstimateTime("<i>estimation en cours...</i>");

            // Retreive organisms list
            ArrayList<Organism> organisms;
            try {
                organisms = DataManager.getOrganisms(k, ui.mainProgressBar);
                ui.writeTerm("Organismes t&eacute;l&eacute;charg&eacute;s (dont " + organisms.size() + " organismes valides)", 1, 1);
            } catch (IOException e) {
                ui.writeTerm("Erreur de t&eacute;l&eacute;chargement de la liste des organismes", 2, 1);
                e.printStackTrace();
                continue;
            }

            // Compare with local arborescence
            int nbrLocal = LocalFileManager.diffLocalOrganisms(k, organisms);

            //initialisation de l'organisme et du temps restant
            ui.mainProgressBar.reset(organisms.size() + nbrLocal, (double) nbrLocal);
            EstimateTime remainingTime = new EstimateTime(organisms.size());

            // Organism loop
            Iterator<Organism> itOrg = organisms.iterator();
            while (itOrg.hasNext()) {
                Organism o = itOrg.next();
                if (threadSuspended) pause("D&eacute;but du traitement de l'organisme " + o.name);

                ui.writeTerm("Analyse de l'organisme : " + o.name, 0, 1);
                ui.orgProgressBar.reset(o.replicons.size());

                ArrayList<String> unknownReplicon = new ArrayList<>();
                ArrayList<Replicon> copyReplicon = new ArrayList<>();

                for (Replicon repl : o.replicons){
                    if (repl.name.contains("nknown"))
                        unknownReplicon.add(repl.id);
                    else
                        copyReplicon.add(repl);
                }

                if(unknownReplicon.size() > 0){
                    copyReplicon.addAll(DataManager.findRepliconType(unknownReplicon));
                    o.replicons = copyReplicon;
                }

                // Replicon loop
                LinkedList<Thread> listThreadOrg = new LinkedList<>();
                for (Replicon r : o.replicons) {
                    Thread thrReplicon = new Thread(() -> repliconProcess(o, r));
                    thrReplicon.start();
                    listThreadOrg.add(thrReplicon);
                }

                waitAllThreads(listThreadOrg);

                // Sauvegarde du fichier Excel
                ui.writeTermSeparator(2);
                ExcelHandler.creatExcel(o);

                if (storeFullGenomes) {
                    LocalFileManager.write(o, null, "Genome");
                    ui.writeTerm("G&eacute;nome complet enregistr&eacute;", 1, 1);
                }

                ui.writeTerm("Statistiques enregistr&eacute;es au format Excel", 1, 1);
                ui.mainProgressBar.increment();
                remainingTime.updateEstimateTime();

                itOrg.remove();

                if (threadSuspended) pause("Fin de traitement de l'organisme " + o.name);
            } // end Organism loop

//            // We wait that all threads are dead
//            Iterator<Thread> iter = listThreads.iterator();
//            try {
//                while (iter.hasNext()) {
//                    //wait
//                    iter.next().join();
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            listThreads.clear();

            // Archivage
            if (archivesNeeded) {
                ui.label.writeEstimateTime("Archivage.");
                archiveAll();
            }
            ui.writeTermSeparator(1);
            ui.label.writeEstimateTime("Fin du royaume.");
        } // end Kingdom loop
        // slide a l'arbo a la fin et lance l'arbo
        ui.JSScript("$('#carousel').carousel('next'); startArbo();");

    }

    private void repliconProcess(Organism o, Replicon r) {
        try {
            DataManager.getSequences(r);
        } catch (IOException e) {
            if (!e.getMessage().equals("EOF"))
                ui.writeTerm("Erreur pendant le t&eacute;l&eacute;chargement du r&eacute;plicon : " + r.name + ", " + r.id, 2, 2);
            e.printStackTrace();
        }

//        if (threadSuspended) pause("D&eacute;but du traitement du réplicon " + r.name);

        StatManager.analyzeReplicon(r);

        ui.writeTerm(r.name + " (" + r.id + ") : "
            + r.sequenceList.size() + " s&eacute;quences t&eacute;l&eacute;charg&eacute;es, "
            + r.sequences_dropped + " invalides", 1, 2);
        ui.orgProgressBar.increment();

        if (storeValidSequences) {
            LocalFileManager.write(o, r, "Gene");
            ui.writeTerm("G&eacute;ne enregistr&eacute; : " + r.name + " (" + r.id + ")", 1, 1);
        }
    }

    private void waitAllThreads(LinkedList<Thread> listThreadOrg) {
        Iterator<Thread> itRep = listThreadOrg.iterator();
        try {
            while (itRep.hasNext()) {
                itRep.next().join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listThreadOrg.clear();
    }
}
