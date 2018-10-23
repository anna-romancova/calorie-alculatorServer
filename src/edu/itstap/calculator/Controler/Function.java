package edu.itstap.calculator.Controler;



import edu.itstap.calculator.Model.UserServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Function {

    private ServerSocket ss;
    private int port;


    public Function() {

        try {
            port=6447;
            ss=new ServerSocket(port);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public void start() {
        while (true) {
            try {
                Socket client=	ss.accept();
                UserServer c=new UserServer(client);
                Thread tr =new Thread(c);
                tr.start();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }

    }



}
