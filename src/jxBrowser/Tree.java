package jxBrowser;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import main.Main;

import java.io.File;

public class Tree {

	//profondeur
	static int dep_count = -1;
	
	// parcours recursif de l'arbo
	public String Generate(File aFile, String strTree) {
		dep_count++;
		for (int i = 0; i < dep_count; i++)
			strTree += " ";
		
		// Fichier
		if (aFile.isFile())	
			strTree += "<li class=\"file\"><button class ='btn btn-link' id= \""+aFile.getName()+"\" onclick='openXlsx(this.id);'>" + aFile.getName() + "</button></li>\n";

		// Dossier
		else if (aFile.isDirectory()) {
			strTree += "<li>"
					+ "<label for=\"" + aFile.getName() + "\">" + aFile.getName() + "</label> "
							+ "<input type=\"checkbox\" id=\"" + aFile.getName() + "\" /> <ol id='"+ aFile.getName() +"'>\n";
			File[] listOfFiles = aFile.listFiles();
	
			if (listOfFiles != null) {
				for (int i = 0; i < listOfFiles.length; i++)
					strTree = Generate(listOfFiles[i], strTree);
			} else {
				strTree += " [ACCESS DENIED]";
			}
			strTree += "</ol></li>\n";
		}
		dep_count--;
		return strTree;
		
	}
	
	// Lance le parcours a partir de la racine
	public void drawTree() {
		
		DOMElement treeBox = Main.ui.document.findElement(By.id("treeBox"));
		String rootstr = Main.path +"/Results";
        File root = new File(rootstr);
        
		String strTree = "<ol class=\"tree\"> <li>";
		strTree = Generate(root, strTree);
        
		Main.ui.browser.executeJavaScript("document.getElementById('treeBox').style.visibility=\"visible\"");
		
		strTree += "</li></ol>";
		
		treeBox.setInnerHTML(strTree);
	}
}
