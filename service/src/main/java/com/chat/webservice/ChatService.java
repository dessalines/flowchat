package com.chat.webservice;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.chat.DataSources;
import com.chat.db.Actions;
import com.chat.db.Transformations;
import com.chat.tools.Tools;
import com.chat.types.*;
import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Paginator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static com.chat.db.Tables.*;
import static spark.Spark.*;

public class ChatService {

    static Logger log = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @Option(name="-loglevel", usage="Sets the log level [INFO, DEBUG, etc.]")
    private String loglevel = "INFO";

    @Option(name="-ui_dist",usage="The location of the ui dist folder.")
    private File uiDist = new File("../ui/dist");

    public void doMain(String[] args) {

        parseArguments(args);

        log.setLevel(Level.toLevel(loglevel));
        log.getLoggerContext().getLogger("org.eclipse.jetty").setLevel(Level.OFF);
        log.getLoggerContext().getLogger("spark.webserver").setLevel(Level.OFF);

        staticFiles.externalLocation(uiDist.getAbsolutePath());
        staticFiles.expireTime(600);

        // Instantiates the ranking constants
        ConstantsService.INSTANCE.getRankingConstants();

        // Set up websocket
        webSocket("/threaded_chat", ThreadedChatWebSocket.class);

        // Set up endpoints
        Endpoints.status();
        Endpoints.user();
        Endpoints.discussion();
        Endpoints.community();
        Endpoints.tag();
        Endpoints.reply();
        Endpoints.exceptions();

        init();

    }

    private void parseArguments(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            System.err.println("java -jar reddit-history.jar [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            System.exit(0);

            return;
        }
    }

    public static void main(String[] args) throws Exception {
        new ChatService().doMain(args);
    }

}
