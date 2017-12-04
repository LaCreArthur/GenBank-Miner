package main;

import jxBrowser.UI;
import org.apache.poi.openxml4j.util.ZipSecureFile;

import javax.swing.*;
import java.awt.*;


/**
 * Point d'entree principal du programme
 */

/**
 * @author CALIF
 */
public class Main {

    // le thread de l'UI qui contient le browser
    public static UI ui;
    // la JFrame qui affichera le browser
    public static JFrame frame;
    // le thread du MainProcess ou se feront tout les calculs
    public static MainProcess mp;
    //chemin vers la racine du programme
    public static String path = null;

    public static void main(String[] args) {

        ui = new UI();
        mp = new MainProcess();
        frame = new JFrame();

        //pour se debarasser du Zip Bomb (si il y a un probleme il faut le mettre a 0)
        ZipSecureFile.setMinInflateRatio(0.00000001);

        // parametrage de la frame ....
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(ui.view, BorderLayout.CENTER);
        frame.setSize(1200, 900);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // lancement de la fenetre
        ui.start();
    }

}
