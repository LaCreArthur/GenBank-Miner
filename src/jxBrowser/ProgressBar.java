package jxBrowser;

import com.teamdev.jxbrowser.chromium.Browser;

/**
 * Represent the state of advancement into a group of tasks
 */
public class ProgressBar {
    Browser browser;
    // default to 100 is arbitrary
    private double total = 100;
    private double progress;
    public int nbEuk = 4000; //TODO valeurs temporaires pour tester plus vite, et ca coute rien de les laisser, non ?
    public int nbPro = 80000;
    public int nbVir = 6000;
    public String id;
    public String content;

    public ProgressBar (Browser browser, String id, String content) {
        this.browser = browser;
        this.id = id;
        this.content = content;
    }

    public void setContent(String s) {
        this.content = s;
    }

    public void reset(double total) {
        this.reset(total, 0);
    }

    public void reset(double total, double init) {
        this.progress = init;
        this.total = total;
        this.displayValue();
    }

    public void increment() {
        if (progress==total)  throw new IndexOutOfBoundsException("The total of "+total+" has already been reached");

        progress++;
        this.displayValue();
    }

    public String toString() {
        return progress+"/"+total;
    }

    private void displayValue() {
        browser.executeJavaScript("document.getElementById('"+ id +"').style.width=\""+(progress/total)*100+"%\";");
        browser.executeJavaScript("document.getElementById('"+ id +"').innerHTML=\""+"&nbsp"+content+"&nbsp"+(int)progress+"&nbspsur&nbsp"+(int)total+"\";");
    }
}
