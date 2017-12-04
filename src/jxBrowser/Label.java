package jxBrowser;

import com.teamdev.jxbrowser.chromium.Browser;

public class Label { //TODO a quoi sert cette classe
		// le browser dans lequel on va ecrire
		Browser browser;

	    public Label(Browser browser) {
	        this.browser = browser;
	    }
	    
	 // executer du code JS
		public void JSScript (String s) {
			browser.executeJavaScript(s);
		}

		// methodes pour ecrire dans la console de l'étape de traitement
		public void writeEstimateTime (String s) {
			JSScript("writeEstimateTime(\""+s+"\")");
		}
}
