import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;


public class Main extends Application {

    private static String USER_NAME = "mvhs.seniorcheckout@mvla.net";  // GMail user name including @mvla.net
    private static String PASSWORD = "mvhsseniorcheckout"; // GMail password
    private static Label label1;
    private static Label label2;
    private static Label label3;
    private static StackPane layout;

    private static UpdateLabel updateLabel;
    private static Thread thread;

    @Override
    public void start(Stage primaryStage) throws Exception{
        final FileChooser fileChooser = new FileChooser();

        final Button openButton = new Button("Upload a new Spreadsheet");

        openButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        File file = fileChooser.showOpenDialog(primaryStage);
                        if (file != null) {
                            try {
                                scanCSV(file);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
        layout = new StackPane();
        openButton.setTranslateY(50);
        layout.getChildren().add(openButton);

        label1 = new Label("Senior Checkout Emails");
        label1.setTranslateY(-120);
        label1.setFont(new Font("Arial", 30));
        layout.getChildren().add(label1);

        label2 = new Label("Please download the spreadsheet from google sheets by going to \n File > Download as > Comma Separated Values. Then upload the file here by clicking \n the button below. The email process will begin automatically. \n The program will quit when done.");
        label2.setTranslateY(-50);
        layout.getChildren().add(label2);

        label3 = new Label("");
        label3.setTranslateY(100);
        layout.getChildren().add(label3);

        updateLabel = new UpdateLabel(label3);
        thread = new Thread(updateLabel);

        Scene scene = new Scene(layout, 600, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private static void sendFromGMail(String from, String pass, String[] to, String subject, String body) {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            message.setContent(body, "text/html; charset=utf-8");

            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }

    private static void scanCSV(File file) throws InterruptedException {

        Thread complete = new Thread(new Runnable() {
            @Override public void run() {
                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        Stage stage = (Stage) label1.getScene().getWindow();
                        stage.close();
                    }
                });
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> recipients = new ArrayList<String>();
                String from = USER_NAME;
                String pass = PASSWORD;

                String subject = "IMPORTANT SENIOR CHECKOUT INFORMATION | ACTION REQUIRED";
                String bodyAllDone = "Congratulations you have completed the requirements for Senior Checkout! Please report to the theater steps on Senior Checkout Day, Wednesday May 30th at 9:45 AM to receive your ticket to claim your cap & gown. <br><br> MVHS Administration";
                String bodyGeneral = "Dear MVHS Senior,<br><br>Senior Checkout is quickly approaching (Weds., May 30)! In order for you to receive your graduation cap & gown and to participate in the Graduation Ceremony, <u>every senior must</u> successfully <u>complete</u> the <u>checkout</u> process.<br><br>";
                String bodyEnder = "Thank you!<br>MVHS Administration";
                String ccc = "Our records indicate that you are missing one or more items from the <b><i>College & Career Center</i></b>. Please follow up with the College & Career Center coordinators to complete the process.<br><br>";
                String finance = "Our records indicate that you are missing one or more items from the <b><i>Finance Office</i></b>. Please follow up with the Finance Office to complete the process.<br><br>";
                String library = "Our records indicate that you are missing one or more items from the <b><i>Library</i></b>. Please follow up with the Librarian or Library Assistant to complete the process.<br><br>";
                String tbc = "Our records indicate that you are missing one or more items from the <b><i>Textbook Center (TBC)</i></b>. Please follow up with the Librarian or Library Assistant to complete the process.<br><br>";

                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    br.readLine();
                    String nextLine;

                    int lines = 0;
                    while (br.readLine() != null) lines++;
                    br.close();

                    br = new BufferedReader(new FileReader(file));
                    br.readLine();

                    while ((nextLine = br.readLine()) != null) {
                        if(nextLine != "" && nextLine != " " && nextLine != null) {
                            String finalBody = "";
                            String[] valuesOriginal = nextLine.split(",");
                            String[] values = new String[9];
                            boolean needtoCheck = false;
                            for (int i = 0; i < values.length; i++) {
                                values[i] = valuesOriginal[i];
                                if(values[i].equals("")) {
                                    needtoCheck = true;
                                }
                            }

                            if (needtoCheck) {
                                updateLabel.completedCountIncrease();
                                updateLabel.setTotalCount(lines);
                                thread = new Thread(updateLabel);
                                thread.start();
                                thread.join();
                                thread.interrupt();
                                finalBody = bodyGeneral;
                                for (int i = 0; i < values.length; i++) {
                                    if (i == 5 && values[i].equals("")) { //CCC
                                        finalBody += ccc;
                                    }
                                    if (i == 6 && values[i].equals("")) { //Library
                                        finalBody += library;
                                    }
                                    if (i == 7 && values[i].equals("")) { //TBC
                                        finalBody += tbc;
                                    }
                                    if (i == 8 && values[i].equals("")) { //Finance Office
                                        finalBody += finance;
                                    }
                                }
                                finalBody += "<br>" + bodyEnder;

                                recipients.add(values[4]);
                                String[] to = {values[4]};
                                sendFromGMail(from, pass, to, subject, finalBody);
                            } else {
                                if (values[4] != null) {
                                    recipients.add(values[4]);
                                    finalBody = bodyAllDone;

                                    String[] to = {values[4]};
                                    sendFromGMail(from, pass, to, subject, finalBody);
                                }
                            }
                        }
                    }
                    br.close();
                    complete.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}