/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.isec.deis.mis.arduinosimulatorweb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/*
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
 */
/**
 * Web application lifecycle listener.
 *
 * @author pafgoncalves@ipc.pt
 */
@WebListener
public class ServletListener implements ServletContextListener/*, HttpSessionBindingListener*/ {

    static private ExecutorService executorService = null;

    java.sql.Connection con = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        //criar um ExecuterService para as threads de simulação
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("number of processors: " + processors);
        int nthreads = processors > 1 ? processors - 1 : processors;
        
        if( System.getProperty("SIMTHREADS")!=null ) {
            try {
                nthreads = Integer.parseInt(System.getProperty("SIMTHREADS"));
                System.out.println("SIMTHREADS property: "+nthreads);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        if( nthreads<4 ) {
            nthreads = 4;
        }
        executorService = Executors.newFixedThreadPool(nthreads);
        System.out.println("created executers pool with " + nthreads + " threads");

        try {
            javax.naming.InitialContext cxt = new javax.naming.InitialContext();
            if( cxt == null ) {
                throw new Exception("Uh oh -- no context!");
            }

            javax.sql.DataSource ds = (javax.sql.DataSource) cxt.lookup("java:/comp/env/jdbc/DB");

            if( ds == null ) {
                throw new Exception("Data source not found!");
            }

            con = ds.getConnection();

            if( con == null ) {
                throw new Exception("Connection not found!");
            }

            boolean tableExists = false;
            ResultSet rset = con.getMetaData().getTables(null, null, "simulation", null);
            if( rset.next() ) {
                tableExists = true;
            }
            try {
                rset.close();
            } catch(Exception e) {}

            if( !tableExists ) {
                System.out.println("Database is empty!");
                //TODO: remover dependencia do H2
                //https://github.com/BenoitDuffez/ScriptRunner
                PreparedStatement pst = con.prepareStatement("RUNSCRIPT FROM 'classpath:/pt/isec/deis/mis/arduinosimulatorweb/create.sql'");
                pst.execute();
                try {
                    pst.close();
                } catch(Exception e) {}
                System.out.println("Database: created tables");
            } else {
                System.out.println("Database OK");
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        shutdownAndAwaitTermination(executorService);
        executorService = null;

        if( con != null ) {
            try {
                //TODO: remover dependencia do H2
                //no ninimo testar se a BD é H2 e só correr se for
                //https://stackoverflow.com/questions/254213/how-to-determine-database-type-for-a-given-jdbc-connection
                try (Statement stat = con.createStatement()) {
                    stat.execute("SHUTDOWN");
                }
            } catch(Exception e) {
            }
            try {
                con.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static ExecutorService getExecutorService() {
        return executorService;
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        System.out.println("stopping executers pool...");
        if( pool == null ) {
            return;
        }
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if( !pool.awaitTermination(5, TimeUnit.SECONDS) ) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if( !pool.awaitTermination(5, TimeUnit.SECONDS) ) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch(InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        System.out.println("executers pool stopped");
    }
}
