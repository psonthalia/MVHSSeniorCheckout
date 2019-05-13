package MVHS_Senior_Checkout;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class UpdateLabel implements Runnable {
    private Label label;
    private int completedCount = 1;
    private int totalCount = 0;
    public UpdateLabel(Label label) {
        this.label = label;
    }
    public void run () {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                label.setText(completedCount + "/" + totalCount);
            }
        });
    }
    public void completedCountIncrease() {
        completedCount++;
    }
    public void setTotalCount(int total) {
        totalCount = total;
    }
}
