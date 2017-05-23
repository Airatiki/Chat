package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

class Daemon implements Runnable{
    public TextArea area;
    public DataInputStream dis;
    String message;
    Daemon(TextArea a){
        area = a;

    }
    public void setDis(DataInputStream dataInputStream){
        dis = dataInputStream;
    }
    @Override
    public void run(){
        while (true){
            try{
            message = dis.readUTF();
            System.out.println(message);
            area.setText(area.getText() + message + "\n");
            area.selectPositionCaret(area.getLength());
            area.deselect();
           }catch (Exception e){area.setText("Disconnected from server");}
        }
    }
}


public class Controller {
    
    public TextField sendBat;
    public String address = "";
    public int port = 0;
    public TextField addr;
    public TextField p;
    public TextArea area;
    public TextField UserName;
    private Socket socket;
    static DataInputStream dis;
    static DataOutputStream dos;
    public static Daemon demon;
    public static Thread thread;
    @FXML
    public Button conectedButton;

    // Вызывается окно для подключения к серверу
    @FXML
    private void getConnection(){
        try{
            area.setText("");

            demon = new Daemon(area);

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("connection.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Connection");
            stage.setScene(new Scene(root1));
            stage.show();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    // Отправляем сообщение на сервер
    public void sendText(ActionEvent actionEvent){
        try {
                String line = sendBat.getText();
                dos.writeUTF(line); // отсылаем введенную строку текста серверу
                dos.flush(); // заставляем поток закончить передачу данных
                sendBat.setText("");
            }catch (Exception e){e.getStackTrace();}
    }


public void connectToServer(String name){
    try{
        InetAddress ipAddress = InetAddress.getByName(address);
        socket = new Socket(ipAddress, port);

        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        dos.writeUTF(name);
        dos.flush();

        demon.setDis(dis);
        if(thread != null)
            thread.stop();
        thread = new Thread(demon);
        thread.setDaemon(true);
        thread.start();   // настраиваем поток для получения сообщений с сервера


    }catch (Exception e){
        alertMessage("Connection is failed...");
    }
}

    public void getAddress(ActionEvent actionEvent) {
        address = addr.getText();
        if(address.equals("") || address.equals("Enter server address (default: localhost)"))
            address = "localhost";

        if(p.getText().equals("") || p.getText().equals("Enter port (default: 3000)"))
            port = 3000;
        else {
            try {
                port = Integer.parseInt(p.getText());
                if(port < 1 || port > 65535){
                    alertMessage("Port must be a number from 1 to 65535");
                    return;
                }
            } catch (Exception e) {
                alertMessage("Port must be a number from 1 to 65535");
                return;
            }
        }
        Stage stage = (Stage) conectedButton.getScene().getWindow();
        stage.close();

        connectToServer(UserName.getText());

    }

    private void alertMessage(String context){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(context);
        alert.showAndWait();
    }
    public void onExit(ActionEvent actionEvent) {
        exitApplication(actionEvent);
    }
    @FXML
    public void exitApplication(ActionEvent event) {
        Platform.exit();
    }
}

