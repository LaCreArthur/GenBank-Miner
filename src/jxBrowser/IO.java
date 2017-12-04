package jxBrowser;

import com.teamdev.jxbrowser.chromium.JSArray;
import main.Main;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

// IO pour input/output du Web vers le Java et inversement
// Une methode est utilisée dans JavaScrpt par un appel a "window.IO.MaMethode;"
public class IO {

	// Recupere les options utilisateur et lance le traitement
	public void startHandler (JSArray p_kingdoms, boolean dl_full_genome, boolean dl_valid_sequence,boolean dl_archive) {
		System.out.println("Operation started :"
			+ "\nKingdoms selected: " + p_kingdoms.length()
			+ "\tFull genome: " + dl_full_genome
			+ "\tValid sequence: " + dl_valid_sequence
			+ "\tArchivage: " + dl_archive);

		ArrayList<String> kingdoms = new ArrayList<>();
		for (int i = 0; i < p_kingdoms.length(); ++i)
			kingdoms.add( p_kingdoms.get(i).getStringValue() );

		Main.mp.start(kingdoms, dl_full_genome, dl_valid_sequence,dl_archive);
	}

	public String getLocalSrc() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(DIRECTORIES_ONLY); // seulement les repertoires
        int returnVal = fc.showOpenDialog(Main.frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
			String path = null;
			try {
				path = file.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Opening: " + path);
			Main.path = path;
            return path;
        } else {
            System.out.println("Open command cancelled by user");
            return null;
        }
    }

    public void pause() {
		System.out.println("Clicked");
		Main.mp.threadSuspended = !Main.mp.threadSuspended;
	}

	public void reload() {
		System.out.println("Reloading ....");
		Main.ui.getOrganismesCount();
	}

	public void callDrawTree(){
		Tree t = new Tree();
		t.drawTree(); //TODO mettre le bon path
	}

	public void openXlsx(String path) {
		path = Main.path + path;
		File f = new File(path);
		System.out.println("openXlsx va tenter d'ouvrir le fichier " + path);
		try {
			java.awt.Desktop.getDesktop().open(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void exit() {
		System.exit(0);
	}
}
