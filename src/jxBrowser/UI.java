package jxBrowser;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import main.Main;

import java.net.URL;

public class UI extends Thread {
	private Thread t;
	private String name;
	public Browser browser;
	public BrowserView view; // la vue du browser est ajouter dans la frame
	public ProgressBar mainProgressBar;
	public ProgressBar orgProgressBar;
	public Tree tree;
	public Label label;

	// le fichier HTML de la page pour Javascript
	/* utilisé ? */
	public DOMDocument document;

	public UI () {
		name = "UI";
		browser = new Browser();
		view = new BrowserView(browser);
		mainProgressBar = new ProgressBar(browser, "probar", "Organismes");
		orgProgressBar = new ProgressBar(browser, "orgbar", "Replicons");
		tree = new Tree();
		label = new Label(browser);
	}

	public void start() {
		if (t == null) {
			t = new Thread (this, name);
			t.start ();
		}
	}

	public void run() {
		getOrganismesCount();
		browser.addLoadListener(new LoadAdapter() {
			@Override
			public void onFinishLoadingFrame(FinishLoadingEvent event) {
				if (event.isMainFrame()) {
					System.out.println("HTML is loaded.");
					document = event.getBrowser().getDocument();

					//TODO checker s'il y a des fichiers locaux pour mise a jour
					// recherche de données locale en vu de mise a jours a faire ici
					// ou plutot dans main et passer un parametre ici apres
					// tree.drawTree();
				}
			}
		});

		// Ce listener sert a récupérer les inputs HTML de l'utilisateur
		browser.addScriptContextListener(new ScriptContextAdapter() {
			@Override
			public void onScriptContextCreated(ScriptContextEvent event) {
				Browser browser = event.getBrowser();
				JSValue window = browser.executeJavaScriptAndReturnValue("window");
				// le script JS startStats() va utiliser l'objet IO pour appeler du code Java
				window.asObject().setProperty("IO", new IO());
			}
		});


		// affectation des variables MAIN.PATH et INDEX.PATH pour index.html selon l'environnement d'execution
		String indexPath = "";
		URL res = getClass().getResource("/index.html");
		if (res.toString().startsWith("jar:")) { // teste si on execute le jar ou pas
			indexPath = getClass().getProtectionDomain().getCodeSource().getLocation().toString(); // recupere le path exact du jar
			indexPath = indexPath.substring(0,indexPath.length()-4); // suppression de .jar dans le path
			Main.path = indexPath.substring(6) + "/../";  // suppression de 'file:/'
		} else {
			indexPath = "file:///"+System.getProperty("user.dir");
			Main.path = System.getProperty("user.dir")+"/../";
		}

		indexPath += "/UI/index.html";

		// indique la page HTML a charger dans le browser
		browser.loadURL(indexPath);
		System.out.println("Main path : " + Main.path);
		System.out.println("Res path : " + indexPath);
	}

	public void setTreatmentSubtitle(String s) {
		JSScript("document.getElementById('traitmntSmall').innerHTML = \""+s+"\";");
	}

	// charge la page GenBank/browse pour recuperer le nb d'organismes
	public void getOrganismesCount() {
		Browser temp = new Browser();
		temp.loadURL("https://www.ncbi.nlm.nih.gov/genome/browse"); //TODO check if not bug
		temp.addLoadListener(new LoadAdapter() {
			@Override
			public void onFinishLoadingFrame(FinishLoadingEvent event) {
				if (event.isMainFrame()) {
					System.out.println("GenBank is loaded.");
					// cherche les nombres par des requetes jaavscript
                    JSValue jsEuk = temp.executeJavaScriptAndReturnValue("document.getElementById('genome_euks_id').getElementsByTagName('b')[0].innerHTML");
                    JSValue jsPro = temp.executeJavaScriptAndReturnValue("document.getElementById('genome_proks_id').getElementsByTagName('b')[0].innerHTML");
                    JSValue jsVir = temp.executeJavaScriptAndReturnValue("document.getElementById('genome_viruses_id').getElementsByTagName('b')[0].innerHTML");
					// controle si les nbs sont trouvés (sinon la connexion/la page a buguée
                    if(jsEuk.isNull() || jsPro.isNull() || jsVir.isNull()) {
                    	// message d'erreur et bouton Recharger
						JSScript("$(\"#load\").html(\"<b>Impossible de se connecter &agrave; GenBank</b>\")");
						JSScript("document.getElementById(\"load\").style.color = \"#DA4453\"");
						JSScript("$(\"#buttonHolder\").html(" +
								"\"<a id='btnReload' class='btn btn-danger' style='margin-top: -10px;' onclick='reload();'>" +
								"Relancer</a>\");");
					}
					else { // connexion OK
                    	// recuperation des nombres dans Java
						mainProgressBar.nbEuk = Integer.parseInt(jsEuk.asString().getStringValue());
						mainProgressBar.nbPro = Integer.parseInt(jsPro.asString().getStringValue());
						mainProgressBar.nbVir = Integer.parseInt(jsVir.asString().getStringValue());
						// Ajout du bouton Demarrer
						JSScript("$(\"#buttonHolder\").html( " +
								"\"<a style='float: right; margin-top: -10px' class='btn btn-success' id='btnConfirm' href='#carousel' " +
								"onclick='startStats();' data-slide='next' data-dismiss='modal'> D&eacute;marrer </a>\");");
						// Affiche le nombre des organismes sur la page
						JSScript("$(\"#load\").html(\" Nombre d'organismes : &emsp;" +
								"<span style='color:#3BAFDA'>Eucaryotes :</span> <span class='badge badge-primary'>"+ mainProgressBar.nbEuk +"</span>  &emsp;" +
								"<span style='color:#8CC152'>Procaryotes :</span> <span class='badge badge-success'>"+ mainProgressBar.nbPro +"</span>  &emsp;" +
								"<span style='color:#F6BB42'>Virus :</span> <span class='badge badge-warning'>"+ mainProgressBar.nbVir +"</span> \")");
						// Check si des royaumes sont cocher a l'apparition du bouton Demarrer (ensuite JQery s'en charge)
						JSScript("checkSelectedKingdoms();");
					}
				}
			}
		});
	}

	// executer du code JS
	public void JSScript (String s) {
		browser.executeJavaScript(s);
	}

	/**
	 * Write a text in the consoles of step 2
	 * @param s line to append
	 * @param status type of the message. Could be one of  {0: Normal, 1: Good, 2: Exception}
	 * @param term id of the console, must be 1 or 2
	 */
	public void writeTerm (String s, int status, int term) {
		JSScript("writeTerm(\""+s+"\","+status+","+term+")");
	}

	/**
	 * Append a '-' line in the choosen console
	 * @param term must be 1 or 2
	 */
	public void writeTermSeparator(int term) {
		writeTerm("---------------------------------------------------------------------------", 0, term);
	}
}
