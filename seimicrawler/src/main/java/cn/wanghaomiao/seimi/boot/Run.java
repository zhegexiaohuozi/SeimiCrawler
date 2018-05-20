/*
   Copyright 2015 Wang Haomiao<seimimaster@gmail.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package cn.wanghaomiao.seimi.boot;

import cn.wanghaomiao.seimi.core.Seimi;
import org.apache.commons.cli.*;

/**
 * @author SeimiMaster seimimaster@gmail.com
 * @author skyqty@github.com
 * @since 2015/12/30.
 */
public class Run {

    static final Option HTTPD_PORT = new Option("p", "port", true, "Port number of API server");
    static final Option CRAWLER_NAMES = new Option("c", "crawler_names", true, "which crawler will start interface " +
            "and split with comma.");
    static final Option HELP_OPT = new Option("h", "help", false, "Show this help and quit");

    private String[] crawlers;
    private Integer port;

    public int run(String[] args) {
        try {
            parseOptions(args);
        } catch (IllegalStateException e) {
            printHelpAndExit(e.getMessage(), getOptions());
        }
        return startHttpd();
    }

	private int startHttpd() {
		try {
			Seimi s = new Seimi();
            if (port != null) {
                s.goRunWithHttpd(port, crawlers);
            } else {
                s.goRun(true,crawlers);
            }
		} catch (Exception e) {
			return -1;
		}
		return 1;
	}

    private void parseOptions(String[] args) {
        Options options = getOptions();

        CommandLineParser parser = new PosixParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            printHelpAndExit("Error parsing command line options: " + e.getMessage(), options);
        }

        assert cmdLine != null;
        if (cmdLine.hasOption(HELP_OPT.getOpt())) {
            printHelpAndExit(options, 0);
        }

        if (cmdLine.hasOption(HTTPD_PORT.getOpt())) {
            String portValue = cmdLine.getOptionValue(HTTPD_PORT.getOpt());
            if (portValue.matches("\\d+")) {
                port = Integer.parseInt(portValue);
            } else {
                throw new IllegalArgumentException("port must be number: "
                        + portValue);
            }
        }

        if (!cmdLine.getArgList().isEmpty()) {
            throw new IllegalStateException("Got unexpected extra parameters: "
                    + cmdLine.getArgList());
        }

        if (cmdLine.hasOption(CRAWLER_NAMES.getOpt())) {
            String crawlerNames = cmdLine.getOptionValue(CRAWLER_NAMES.getOpt());
            crawlers = crawlerNames.split(",");
        }

    }

    public static void main(String[] args) {
        int run = new Run().run(args);
        System.exit(run);
    }

    private Options getOptions() {
        Options options = new Options();
        options.addOption(HTTPD_PORT);
        options.addOption(CRAWLER_NAMES);
        options.addOption(HELP_OPT);
        return options;
    }

    private void printHelpAndExit(String errorMessage, Options options) {
        System.err.println(errorMessage);
        printHelpAndExit(options, 1);
    }

    private void printHelpAndExit(Options options, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("help\n" +
                "If null of crawlers it will be start all crawlers interface.\n" +
                "If port of httpd is null,it will be just start all crawler with out API server.\n" +
                "If both of args are null,it will be just start a agent waiting for queue order.", options);
        System.exit(exitCode);
    }

}
