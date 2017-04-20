/**
 * 
 */
package com.patsage.microservices.services;

import com.patsage.microservices.services.patent.USPTOPatentServer;
import com.patsage.microservices.services.registration.ServiceRegistrationServer;
import com.patsage.microservices.services.user.UserServer;
import com.patsage.microservices.services.usptoapp.USPTOAppServer;
import com.patsage.microservices.services.web.WebServer;

/**
 * @author dprakash
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String serverName = "NO-VALUE";

		switch (args.length) {
		case 2:
			// Optionally set the HTTP port to listen on, overrides
			// value in the <server-name>-server.yml file
			System.setProperty("server.port", args[1]);
			// Fall through into ..

		case 1:
			serverName = args[0].toLowerCase();
			break;

		default:
			usage();
			return;
		}

		if (serverName.equals("registration") || serverName.equals("reg")) {
			ServiceRegistrationServer.main(args);
		} else if (serverName.equals("user")) {
			UserServer.main(args);
		} else if (serverName.equals("web")) {
			WebServer.main(args);
		} else if (serverName.equals("usptoapp")) {
			USPTOAppServer.main(args);
		} else if (serverName.equals("usptopatent")) {
			USPTOPatentServer.main(args);
		} else if (serverName.equals("usptoappweb")) {
			USPTOPatentServer.main(args);
		} else {
			System.out.println("Unknown server type: " + serverName);
			usage();
		}
	}

	protected static void usage() {
		System.out.println("Usage: java -jar ... <server-name> [server-port]");
		System.out.println(
				"     where server-name is 'reg', 'registration', " + "'accounts' or 'web' and server-port > 1024");
	}
}
