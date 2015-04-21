package org.culpan.herosim.plugin.bonjour;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import org.apache.log4j.Logger;
import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;
import org.culpan.herosim.plugin.HeroSimPlugin;
import org.culpan.herosim.plugin.PluginManager;
import org.culpan.herosim.plugin.network.NetworkMonitorPlugin;

import com.apple.dnssd.DNSSD;
import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;

public class BonjourMonitorPlugin implements HeroSimPlugin, RegisterListener {
	private final static Logger logger = Logger.getLogger(BonjourMonitorPlugin.class);

	public final static String SERVICE_TYPE = "_herosim._tcp";

	protected DNSSDRegistration dnssdReg;

	final protected ClientMessage clientMessage = new ClientMessage();

	public void changePhase(int turn, int phase, List<Person> chars) {
		logger.debug("Sending PHASE_UPDATE to clients");

		StringBuffer msg = new StringBuffer();
		msg.append(NetworkMonitorPlugin.PHASE_UPDATE).append("|");
		msg.append(Integer.toString(turn)).append("|");
		msg.append(Integer.toString(phase)).append("|");

		for (Person p : chars) {
			if (p.actsInPhase(phase)) {
				msg.append(Utils.toString(p.toXml())).append("|");
			}
		}

		final String message = msg.toString();

		new Thread() {
			public void run() {
				clientMessage.putMessage(message);
			}
		}.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#event(java.lang.String,
	 *      java.lang.Object)
	 */
	public void event(String eventName, Object data) {
		if (eventName.equalsIgnoreCase(PERSON_ADD)) {
			logger.warn("Person add not implemented");
			// personAdded((Person) data);
		} else if (eventName.equalsIgnoreCase(PERSON_DEL)) {
			logger.warn("Person delete not implemented");
			// personRemoved((Person) data);
		} else if (eventName.equalsIgnoreCase(CHANGE_PHASE)) {
			PluginManager.PhaseReport pr = (PluginManager.PhaseReport) data;
			changePhase(pr.turn, pr.phase, pr.chars);
		}
	}

	class ClientMessage {
		int numClients = 0;

		int waitingForMessage = 0;

		String message;

		public void addClient() {
			logger.debug("Adding client");
			numClients++;
		}

		public void removeClient() {
			logger.debug("Removing client");
			numClients--;
			
			if (waitingForMessage > 0) {
				waitingForMessage--;
			}
		}

		synchronized public void putMessage(String msg) {
			try {
				if (waitingForMessage > 0) {
					wait();
				}
				logger.debug("Writing message : " + msg);
				logger.debug("numClients = " + numClients);
				waitingForMessage = numClients;
				message = msg;
				notifyAll();
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}
		}

		synchronized public String getMessage() {
			try {
				if (waitingForMessage < 1 || message == null) {
					wait();
				}
				logger.debug("Getting message");
				waitingForMessage--;
				logger.debug("numClients = " + numClients);
				notifyAll();
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
			}
			return message;
		}
	}

	class ClientHandler implements Runnable {
		Socket socket;
		ClientMessage cm;
		BufferedWriter output;

		public ClientHandler(Socket socket, ClientMessage cm) {
			this.socket = socket;
			this.cm = cm;
			cm.addClient();
			new Thread(this).start();
		}

		public void run() {
			try {
				output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

				while (true) {
					String msg = cm.getMessage();
					logger.debug("Sending message to client...");
					output.write(msg);
					output.newLine();
					output.flush();
				}
			} catch (Throwable t) {
				logger.error(t.getLocalizedMessage());
			} finally {
				cm.removeClient();
			}
		}
	}

	public void initialize() {
		try {
			final ServerSocket ss = new ServerSocket(0);
			
			dnssdReg = DNSSD.register("HeroSim Host", SERVICE_TYPE, ss.getLocalPort(), this);

			new Thread() {
				public void run() {
					try {
						while (true) {
							logger.debug("Got client");
							Socket s = ss.accept();
							new ClientHandler(s, clientMessage);
						}
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage());
						terminate();
					}
				}
			}.start();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		} catch (DNSSDException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	public void terminate() {
		logger.debug("Terminate called; sending TERMINATE");
		clientMessage.putMessage("TERMINATE");
		if (dnssdReg != null) {
			dnssdReg.stop();
		}
	}

	public void serviceRegistered(DNSSDRegistration registration, int flags, String serviceName, String regType,
			String domain) {
		logger.info("Service registered");
		logger.info("  Name   : " + serviceName);
		logger.info("  Type   : " + regType);
		logger.info("  Domain : " + domain);
	}

	public void operationFailed(DNSSDService arg0, int arg1) {
		logger.error("Registration failed; code = " + arg1);
	}

}
