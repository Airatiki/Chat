package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

class Daemon implements Runnable{

    String name;
    DataInputStream dis;
    DataOutputStream dos;
    public Daemon(DataInputStream dataInputStream, DataOutputStream dataOutputStream, String n){
        name = n;
        dis = dataInputStream;
        dos = dataOutputStream;
    }
    @Override
    public void run() {
        while (true){
            try{
                String message;
                if(dis != null){
                    try {
                        message = dis.readUTF();
                        for(DataOutputStream d : Controller.toSend){
                            d.writeUTF("["+name + "]:" + message);
                            d.flush();
                        }
                    }catch (Exception e){
                        Controller.toSend.remove(dos);
                    }

                }

            }catch (Exception e){e.getStackTrace();}
        }

    }
}
public class Controller {
    public static ServerSocket ss;
    public static CopyOnWriteArrayList<DataOutputStream> toSend = new CopyOnWriteArrayList<>();
    public TextArea area;
    public TextField port;
    Thread listener;

    public void connecting() throws IOException {

        Socket socket = ss.accept();

        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        String name = dis.readUTF();

        System.out.println(name);
        toSend.add(dos);
        Thread dem = new Thread(new Daemon(dis, dos, name));
        dem.setDaemon(true);
        dem.start();

    }
    public void start()
    {
        int p = 0;

        if(port.getText().equals("") || port.getText().equals("Enter port (default: 3000)"))
            p = 3000;
        else {
            try {
                p = Integer.parseInt(port.getText());
                if(p < 1 || p > 65535){
                    alertMessage("Port must be a number from 1 to 65535");
                    return;
                }
            } catch (Exception e) {
                alertMessage("Port must be a number from 1 to 65535");
                port.setText("Enter port (default: 3000)");
                return;
            }
        }
        try
        {
            ss = new ServerSocket(p);
            area.setText("Server is running. \naddress: localhost:"+p);
            listener = new Thread(()-> {
                while (true){
                    try{
                        connecting();
                    }catch (Exception e){e.getStackTrace();};
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.setDaemon(true);
        listener.start();

    }
    private void alertMessage(String context){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(context);
        alert.showAndWait();
    }


    public void stop(ActionEvent actionEvent) {
        Platform.exit();
    }
}
