/*
 * Created on Feb 1, 2005
 *
 */
package org.culpan.herosim.plugin.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.culpan.herosim.HeroSimException;
import org.culpan.herosim.Person;
import org.culpan.herosim.Utils;
import org.culpan.herosim.plugin.HeroSimPlugin;
import org.culpan.herosim.plugin.PluginManager;

/**
 * @author CulpanH
 * 
 */
public class NetworkMonitorPlugin implements HeroSimPlugin {
	public final static int DEFAULT_SERVER_PORT = 10931;

	public final static int DEFAULT_CLIENT_PORT = 10932;

	public final static String ADD = "add";

	public final static String REMOVE = "remove";
	
	public final static String PHASE_UPDATE = "phase";
	
	public final static String TERMINATE = "terminate";

	protected static Logger logger = Logger.getLogger(NetworkMonitorPlugin.class);

	protected final List<Client> clients = new LinkedList<Client>();
	
	protected int lastTurn = 0;
	
	protected int lastPhase = 0;
	
	protected List<Person> lastChars = new ArrayList<Person>();

	class Client {
		public String host;

		public int port = DEFAULT_CLIENT_PORT;

		public Socket socket;
		
		public BufferedWriter output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#initialize()
	 */
	public void initialize() {
		// Listen for monitor client ADD and REMOVE
		new Thread() {
			public void run() {
				try {
					ServerSocket dsocket = new ServerSocket(DEFAULT_SERVER_PORT);
//					byte[] buffer = new byte[2048];
//					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

					while (true) {
						try {
							Socket s = dsocket.accept();
							
							BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
							String msg = r.readLine();
							String[] fields = StringUtils.split(msg, "|");
							if (fields[0].equalsIgnoreCase(ADD)) {
								Client client = new Client();
//								client.host = fields[1];
//								client.port = Integer.parseInt(fields[2]);
								client.socket = s;
								client.output = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
								clients.add(client);
								
								logger.info("Net client added : " + client.host + ":" + client.port);
							} else if (fields[0].equalsIgnoreCase(REMOVE)) {
								if (fields.length != 3) {
									throw new HeroSimException("Invalid number of parameters with REMOVE");
								}
								Client client = new Client();
								client.host = fields[1];
								client.port = Integer.parseInt(fields[2]);

								for (Iterator<Client> i = clients.iterator(); i.hasNext();) {
									Client c = i.next();
									if (c.host.equals(client.host) && c.port == client.port) {
										clients.remove(c);
										logger.info("Net client removed : " + c.host + ":" + c.port);
										break;
									}
								}
							}
						} catch (Exception e) {
							logger.error(e);
						}
					}
				} catch (SocketException e) {
					logger.error(e);
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}.start();
	}

	public static String buildMessage(String command, String[] params) {
		String[] s = new String[params.length];
		s[0] = command;
		for (int i = 1; i < params.length; i++) {
			s[i] = params[i - 1];
		}

		return StringUtils.join(s, '|');
	}

	protected void sendMessageToClients(String message) {
		for (Iterator<Client> i = clients.iterator(); i.hasNext();) {
			Client c = i.next();
			try {
				if (c.socket == null || c.socket.isClosed() || !c.socket.isConnected()) {
					c.socket = new Socket(InetAddress.getByName(c.host), c.port);
				}
				
				if (c.output == null) {
					c.output = new BufferedWriter(new OutputStreamWriter(c.socket.getOutputStream()));
				}

				c.output.write(message);
				c.output.newLine();
				c.output.flush();
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				i.remove();
				logger.info("Client removed from list");
				continue;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#terminate()
	 */
	public void terminate() {
		// First send terminate notice to client
		sendMessageToClients(TERMINATE);
		
		for (Client c : clients) {
			if (c.socket != null && !c.socket.isClosed() && c.socket.isConnected()) {
				try {
					c.socket.close();
					c.socket = null;
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#personAdded(org.culpan.herosim.Person)
	 */
	public void personAdded(Person person) {
/*		logger.debug("Sending ADD to clients");
		String msg = ADD + "|" + person.toXml();
		sendMessageToClients(msg);*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#personRemoved(org.culpan.herosim.Person)
	 */
	public void personRemoved(Person person) {
/*		logger.debug("Sending REMOVE to clients");
		String msg = REMOVE + "|" + person.toXml();
		sendMessageToClients(msg);*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#changePhase(int, int,
	 *      java.util.List)
	 */
	public void changePhase(int turn, int phase, List<Person> chars) {
		logger.debug("Sending PHASE_UPDATE to clients");
		StringBuffer msg = new StringBuffer();
		msg.append(PHASE_UPDATE).append("|");
		msg.append(Integer.toString(turn)).append("|");
		msg.append(Integer.toString(phase)).append("|");
		
		for (Person p : chars) {
			if (p.actsInPhase(phase)) {
				msg.append(Utils.toString(p.toXml())).append("|");
			}
		}
		
		lastTurn = turn;
		lastPhase = phase;
		lastChars = chars;
		
		sendMessageToClients(msg.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.culpan.herosim.plugin.HeroSimPlugin#event(java.lang.String,
	 *      java.lang.Object)
	 */
	public void event(String eventName, Object data) {
		if (eventName.equalsIgnoreCase(PERSON_ADD)) {
			personAdded((Person) data);
		} else if (eventName.equalsIgnoreCase(PERSON_DEL)) {
			personRemoved((Person) data);
		} else if (eventName.equalsIgnoreCase(CHANGE_PHASE)) {
			PluginManager.PhaseReport pr = (PluginManager.PhaseReport) data;
			changePhase(pr.turn, pr.phase, pr.chars);
		}
	}

}