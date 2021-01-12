/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gwendolen.crcl.kitting.gwendolencrclclient;

/**
 * Runs a separate thread to retrieve and report on the status
 * of crcl communication. Not used for now.
 * @author michalos
 */
public class StatusThread implements Runnable {

    Thread statuthread;
    private String threadname;
    public Runnable parent;

    StatusThread(Runnable obj, String name) {
                  // save parent
        parent = obj;

        threadname = name;
    }

    @Override
    public void run() {
        System.out.println("Thread running" + threadname);
        for (int i = 0; i < 4; i++) {
            System.out.println(i);
            System.out.println(threadname);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Thread has been interrupted");
            }
        }
    }

    public void start() {
        System.out.println("Thread started");
        if (statuthread == null) {
            statuthread = new Thread(this, threadname);
            statuthread.start();
        }

    }
}
