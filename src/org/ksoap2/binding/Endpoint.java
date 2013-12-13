package org.ksoap2.binding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ksoap2.serialization.AttributeContainer;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Endpoint implements Runnable {

	private static final Logger LOG = Logger.getLogger(Endpoint.class);
	public final int soapVersion;
	public final String uri;
	private MessageListener messageListener;
	private ServerSocket serverSocket;
	private String localHost;
	private boolean running = false;

	public interface MessageListener {
		boolean messageReceived(String soapAction, AttributeContainer message);
	}

	public Endpoint(String uri, int soapVersion) {
		if (uri == null || uri.trim().length() < 1) {
			throw new IllegalArgumentException("Wrong uri");
		}
		this.uri = uri;
		this.soapVersion = soapVersion;
	}

	public Endpoint(String uri, int soapVersion, MessageListener messageListener) {
		if (uri == null || uri.trim().length() < 1) {
			throw new IllegalArgumentException("Wrong uri");
		}
		this.uri = uri;
		this.soapVersion = soapVersion;
		this.messageListener = messageListener;
	}

	public String publish(int port) throws IOException {
		getLocalHost();
		serverSocket = new ServerSocket(port < 0 ? 0 : port, 0, InetAddress.getByName(localHost));
		serverSocket.setReuseAddress(true);
		running = true;
		(new Thread(this)).start();
		LOG.info("Endpoint " + this + " has been started");
		return "http://" + localHost + ":" + serverSocket.getLocalPort() + uri;
	}

	public boolean isPublished() {
		return running;
	}

	public void stop() {
		if (running) {
			running = false;
			try {
				serverSocket.close();
			} catch (IOException e) {
				LOG.error(e);
			}
			LOG.info("Endpoint " + this + " has been stopped");
			serverSocket = null;
		}
	}

	public int getPort() {
		return serverSocket != null ? serverSocket.getLocalPort() : -1;
	}

	
	public MessageListener getMessageListener() {
		return messageListener;
	}

	
	public void setMessageListener(MessageListener newMessageListener) {
		messageListener = newMessageListener;
	}

	public String getLocalHost() {
		if (localHost != null) {
			return localHost;
		}
		findLocalHost();
		return localHost;
	}

	public boolean setLocalHost(String newLocalHost) {
		if (serverSocket == null) {
			try {
				ArrayList<String> ipAddressList = new ArrayList<String>();
				Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
				while (networkInterfaces.hasMoreElements()) {
					NetworkInterface networkInterface = networkInterfaces.nextElement();
					Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses();
					while (ipAddresses.hasMoreElements()) {
						InetAddress inetAddress = ipAddresses.nextElement();
						if (!inetAddress.isLoopbackAddress() && !inetAddress.isAnyLocalAddress() &&
							inetAddress.getAddress().length == 4) {
							ipAddressList.add(inetAddress.getHostAddress());
						}
					}
				}
				if (ipAddressList.contains(newLocalHost)) {
					localHost = newLocalHost;
					return true;
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	protected void findLocalHost() {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				String name = networkInterface.getName();
				if (name != null && name.indexOf("usb") < 0) { // exclude USB-Ethernet connections
					Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses();
					while (ipAddresses.hasMoreElements()) {
						InetAddress inetAddress = ipAddresses.nextElement();
						if (!inetAddress.isLoopbackAddress() && !inetAddress.isAnyLocalAddress() &&
							inetAddress.getAddress().length == 4) {
							localHost = inetAddress.getHostAddress();
							return;
						}
					}
				}
			}
		} catch (SocketException e) {
			localHost = "127.0.0.1";
		}
	}

	private static void logMessage(String startingLine, Map<String, String> headers, String message) {
		LOG.debug(startingLine);
		for (String key : headers.keySet()) {
			LOG.debug(key + ": " + headers.get(key));
		}
		LOG.debug(message);
	}

	private static String removeQuotes(String string) {
		if (string.charAt(0) == '"') {
			string = string.substring(1);
		}
		if (string.charAt(string.length() - 1) == '"') {
			string = string.substring(0, string.length() - 1);
		}
		return string;
	}

	public void run() {
		try {
			while (running) {
				(new HttpServerConnection(serverSocket.accept())).start();
			}
		} catch (IOException e) {
			if (running) {
				LOG.error(e);
			}
		}
	}

	public String toString() {
		return localHost != null && serverSocket != null ? "http://" + localHost + ":" + serverSocket.getLocalPort() + uri : uri;
	}

	private class HttpServerConnection extends Thread {
		
		private static final String CONTENT_LENGTH_TAG = "Content-Length";
		private static final String METHOD = "POST";
		private static final int BUFFER_SIZE = 8192;
		private static final int READ_STARTING_LINE_STATE = 0;
		private static final int READ_HEADERS_STATE = 1;
		private static final int READ_BODY_STATE = 2;
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;

		private HttpServerConnection (Socket socket) {
			this.socket = socket;
		}

		private void sendOk() {
			sendResponse("200 OK",  null);
		}

		private void sendBadRequest() {
			sendResponse("400 Bad Request",  null);
		}

		private void sendServerInternalError() {
			sendResponse("500 Server Internal Error",  null);
		}

		private void sendResponse(String status, Map<String, String> headers) {
			out.print("HTTP/1.0 " + status + " \r\n");
			if (headers != null) {
				for (String key : headers.keySet()) {
					String value = headers.get(key);
					out.print(key + ": " + value + "\r\n");
				}
			}
			out.print("\r\n");
			out.flush();
		}

		public void run() {
			try {
				LOG.debug("Connection accepted from host " + socket.getInetAddress().getHostName());
				out = new PrintWriter(socket.getOutputStream());
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				char[] buffer = new char[BUFFER_SIZE];
				Map<String, String> headers = new HashMap<String, String>();
				StringBuilder sb = new StringBuilder();
				String startingLine = "";
				String line = "";
				int state = READ_STARTING_LINE_STATE;
				int contentLength = 0;
				int count = 0;
				int length = 0;
				while (true) {
					if (state == READ_BODY_STATE) {
						length = in.read(buffer, 0, BUFFER_SIZE);
						if (length <= 0) {
							break;
						}
					} else {
						line = in.readLine();
						if (line == null) {
							break;
						}
					}
					if (state == READ_STARTING_LINE_STATE) {
						startingLine = line.trim();
						headers.clear();
						state = READ_HEADERS_STATE;
					} else if (state == READ_HEADERS_STATE) {
						if (line.length() > 0) {
							int index = line.indexOf(":");
							if (index > 0) {
								headers.put(line.substring(0, index).trim(), line.substring(index + 1).trim());
							} else {
								LOG.warn("Unrecognized header line: " + line);
							}
						} else {
							try {
								contentLength = Integer.valueOf(headers.get(CONTENT_LENGTH_TAG));
								count = 0;
								state = READ_BODY_STATE;
							} catch (NumberFormatException e) {
								LOG.error(e);
								break;
							}
						}
					} else {
						count += length;
						sb.append(buffer, 0, length);
						if (count >= contentLength) {
							String[] parts = startingLine.split(" ");
							String message = sb.toString().trim();
							if (parts.length > 1 && METHOD.equals(parts[0]) && uri.equals(parts[1])) {
								String soapAction = removeQuotes(headers.get("SOAPAction"));
								if (soapAction == null || soapAction.length() < 1) {
									LOG.error("Header SOAPAction not found:");
									logMessage(startingLine, headers, message);
									sendBadRequest();
								} else {
									LOG.debug("SOAPAction: " + soapAction);
									LOG.debug("message: " + message);
									if (messageListener != null) {
										StringReader sr = new StringReader(message);
										try {
											KXmlParser kXmlParser = new KXmlParser();
											kXmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
											kXmlParser.setInput(sr);
											SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(soapVersion);
											envelope.parse(kXmlParser);
											if (messageListener.messageReceived(soapAction, (AttributeContainer) envelope.bodyIn)) {
												sendOk();
											} else {
												sendServerInternalError();
											}
										} catch (XmlPullParserException e) {
											LOG.error("Failed to parse XML");
											LOG.debug(message, e);
											sendServerInternalError();
										}
										sr.close();
									} else {
										sendOk();
									}
								}
							} else {
								LOG.error("Unexpected message:");
								logMessage(startingLine, headers, message);
								sendBadRequest();
							}
							state = READ_STARTING_LINE_STATE;
						}
					}
				}
			} catch (IOException e) {
				LOG.error(e);
			}
			try {
				out.close();
				in.close();
				socket.close();
			} catch (IOException e) {
				LOG.error(e);
			}
		}
	}
}
