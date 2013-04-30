package br.com.javayuga.posa.concurrent.hsha;

import java.io.IOException;

/**
 * The SyncLayerEchoTask simply calls the echoMessage function in
 * the TransportHandle passing the verbose configuration
 *
 */
public class SyncLayerEchoTask implements Runnable {

    private String thrName;
    private boolean keepWorking;
    private boolean verbose;
    private SyncLayerMessageQueue queue;

    public SyncLayerEchoTask(SyncLayerMessageQueue q, int i, boolean v) {
        thrName = "worker thread " + i;
        keepWorking = true;
        verbose = v;
        queue = q;

    }

    @Override
    public void run() {
        while (keepWorking) {
            try {
                TransportHandle handle = queue.getMessage();

                handle.echoMessage(thrName, verbose);

            } catch (InterruptedException e) {
                System.err.println(e);

            } catch (IOException e) {
                System.err.println(e);

            }
        }

    }

    public void setKeepWorking(boolean keepWorking) {
        this.keepWorking = keepWorking;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}
