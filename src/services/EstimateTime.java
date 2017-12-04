package services;

import static main.Main.ui;

/**
 * Created for CALIF bogoss team
 */
public class EstimateTime {
    private long timeStart;
    private float nbrOrgFinished;
    private float nbrOrgToDo;

    public EstimateTime(float nbrOrgToDo) {
        this.nbrOrgToDo = nbrOrgToDo;
        this.nbrOrgFinished = 0;
        timeStart = System.currentTimeMillis();
    }

    public void updateEstimateTime() {
        nbrOrgFinished++;

        long timeSinceStart = System.currentTimeMillis() - timeStart;
        long totalTimeEstimate = (long) ((float) (timeSinceStart) *
            (nbrOrgToDo / nbrOrgFinished)); // extrapolation pour le temps total estime
        long timeRemaining = Math.max(totalTimeEstimate - timeSinceStart, 0); // devrait toujours etre >0

        int h = (int) (timeRemaining / (1000 * 60 * 60));
        int m = (int) (timeRemaining / (1000 * 60)) - (60 * h);
        int s = (int) (timeRemaining / 1000) - (60 * 60 * h) - (60 * m);

        ui.label.writeEstimateTime(h + " heures, " + m + " minutes et " + s + " secondes.");
    }
}
