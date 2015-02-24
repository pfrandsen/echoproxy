package proxy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EchoProxy extends ProxyServlet {
    public static String OPTION_HELP = "help";
    public static String OPTION_PORT = "port";
    public static String OPTIONS_SHUTDOWN_KEY = "shutdownKey";
    static private String USAGE = "Usage: java -jar <jar-file> <options>";

    private static int DEFAULT_PORT = 9901;
    private static int MIN_PORT_VALUE = 1024;
    private static int MAX_PORT_VALUE = 65535;

    private String echoPrefix = "";
    private String shutdownKey = null;
    private String shutdownContext = "/shutdown"; // default shutdown http://localhost/shutdown/{key}
    private boolean enableShutdown = false;

    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws
            ServletException, IOException {

        if (enableShutdown && "localhost".equalsIgnoreCase(request.getServerName())
                && (shutdownContext + "/" + shutdownKey).equals(request.getRequestURI())) {
            System.out.println("Received shutdown signal");
            System.exit(0);
        }
        System.out.println(echoPrefix + request.getRequestURI());
        super.service(request, response);
    }

    public void setEnableShutdown(boolean enable) {
        enableShutdown = enable;
    }

    public void setShutdownKey(String key) {
        shutdownKey = key;
    }

    public void setShutdownContext(String context) {
        shutdownContext = context;
    }


    protected Request addViaHeader(Request proxyRequest) {
        // proxyRequest.header(HttpHeader.VIA, VIA_HEADER);
        return proxyRequest;
    }

    public void run(CommandLine cmd) {
        String shutdownKey = null;
        String shutdownContext = "/shutdown";
        int port = DEFAULT_PORT;
        if (cmd.hasOption(OPTION_PORT)) {
            if (!isInteger(cmd.getOptionValue(OPTION_PORT), MIN_PORT_VALUE, MAX_PORT_VALUE)) {
                System.err.println("Port (" + cmd.getOptionValue(OPTION_PORT) + ") must be integer in range " +
                        MIN_PORT_VALUE + "-" + MAX_PORT_VALUE);
                return;
            }
            port = Integer.parseInt(cmd.getOptionValue(OPTION_PORT));
        }
        if (cmd.hasOption(OPTIONS_SHUTDOWN_KEY)) {
            shutdownKey = cmd.getOptionValue(OPTIONS_SHUTDOWN_KEY);
        }

        setEnableShutdown(shutdownKey != null);
        setShutdownKey(shutdownKey);
        setShutdownContext(shutdownContext);

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        context.setContextPath("/");

        ServletHolder proxyServletHolder = new ServletHolder(this); // just to set init parameters
        proxyServletHolder.setInitParameter("viaHost", "echo-proxy");
        context.addServlet(proxyServletHolder, "/*");
        server.setHandler(context);

        // now start the proxy server
        try {
            server.start();
            System.out.println("\nEcho proxy server running, listening on port " + port);
            if (shutdownKey != null) {
                System.out.println("Shutdown url http://localhost:" + port + "/" + shutdownContext + "/"
                        + shutdownKey + "\n");
            }
        } catch (Exception e) {
            System.err.println("Server got exception, " + e.getMessage());
        }
        try {
            server.join();
        } catch (InterruptedException ignored) {
        }
    }

    public boolean isInteger(String value, int minValue, int maxValue) {
        try {
            int val = Integer.parseInt(value);
            return val >= minValue && val <= maxValue;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static Options getCommandlineOptions() {
        Options options = new Options();
        Option help = new Option(OPTION_HELP, "Show usage information.");
        // port
        Option port = new Option(OPTION_PORT, true, "Port transparent proxy server will listen on. Valid " +
                "range " + MIN_PORT_VALUE + "-" + MAX_PORT_VALUE + ". Default port is " + DEFAULT_PORT + ". Optional.");
        port.setRequired(false);
        port.setType(Number.class);
        // shutdownKey
        Option shutdownKey = new Option(OPTIONS_SHUTDOWN_KEY, true, "Key used for shutting down proxy server. "
                + "Optional.");
        shutdownKey.setRequired(false);

        options.addOption(help);
        options.addOption(port);
        options.addOption(shutdownKey);
        return options;
    }

    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser(); // replace with BasicParser when Apache commons-cli is released
        CommandLine cmd;
        try {
            Options options = getCommandlineOptions();
            // parse the command line arguments
            cmd = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("\nParse error, " + exp.getMessage() + "\n");
            printHelp();
            return;
        }
        if (cmd.hasOption(OPTION_HELP)) {
            printHelp();
            return;
        }
        EchoProxy proxy = new EchoProxy();
        proxy.run(cmd);
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        Options options = getCommandlineOptions();
        options.addOption(new Option(OPTION_HELP, "Print this message"));
        formatter.printHelp(USAGE, options);
    }

}
