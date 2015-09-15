package ev3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

//Modified Version in lejos.ev3.tools
public abstract class EV3ScpUpload implements Runnable{
	
	class DummyUserInfo implements UserInfo  {

		@Override
		public String getPassphrase() {
			return null;
		}

		@Override
		public String getPassword() {
			return null;
		}

		@Override
		public boolean promptPassphrase(String arg0) {
			return false;
		}

		@Override
		public boolean promptPassword(String arg0) {
			return false;
		}

		@Override
		public boolean promptYesNo(String arg0) {
			return true;
		}

		@Override
		public void showMessage(String arg0) {	
		}
	}
	
	private String host;
	private String from;
	private String to;
	private boolean run;
	private boolean debug;
	
	private static final String JAVA_RUN_JAR = "cd /home/lejos/programs;jrun -jar ";
	private static final String JAVA_DEBUG_JAR = "cd /home/lejos/programs;jrun -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=y -jar ";
	private boolean debugging = false;

	private int upload() throws IOException, JSchException {

		if (debugging) System.out.println("Copying to host " + host + " from " + from + " to "
				+ to + " run = " + run + " and debug = " + debug);

		JSch jsch = new JSch();
			Session session = jsch.getSession("root", host, 22);

			session.setPassword("");
			UserInfo ui = new DummyUserInfo();
			session.setUserInfo(ui);
			session.connect();

			String command = "scp -t " + to;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				System.err.println("EV3ScpUpload: scp command failed");
				System.exit(1);
			}

			// send "C0644 filesize filename", where filename should not include
			// '/'
			long filesize = new File(from).length();
			command = "C0644 " + filesize + " ";
			if (from.lastIndexOf('/') > 0) {
				command += from.substring(from.lastIndexOf('/') + 1);
			} else {
				command += from;
			}

			command += "\n";
			out.write(command.getBytes());
			out.flush();

			if (checkAck(in) != 0) {
				System.err.println("EV3ScpUload: C0644 failed");
				System.exit(1);
			}

			// send contents of local file
			FileInputStream fis = new FileInputStream(from);
			byte[] buf = new byte[1024];

			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				//System.out.println("Sending " + len + " bytes");
				out.write(buf, 0, len); // out.flush();
			}

			fis.close();
			fis = null;

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			if (checkAck(in) != 0) {
				System.err.println("EV3ScpUoload: send contents failed");
				System.exit(1);
			}
			out.close();
			channel.disconnect();

			if (run || debug) {
				command = (debug ? JAVA_DEBUG_JAR : JAVA_RUN_JAR) + to;

				System.out.println("Running program ... ");

				channel = session.openChannel("exec");
				((ChannelExec) channel).setCommand(command);

				out = channel.getOutputStream();
				in = channel.getInputStream();

				channel.connect();

				byte[] tmp = new byte[1024];

				while (true) {
					while (in.available() > 0) {
						int i = in.read(tmp, 0, 1024);
						if (i < 0) break;
						System.out.print(new String(tmp, 0, i));
						outputLine(new String(tmp, 0, i));
					}

					if (channel.isClosed()) {
						if (debugging) System.out.println("exit-status: " + channel.getExitStatus());
						break;
					}

					try {
						Thread.sleep(1000);
					} catch (Exception ee) {
					}
				}

				if (debugging) System.out.println("Run finished");
			}

			channel.disconnect();
			session.disconnect();

		return 0;
	}

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b <= 0) return b;
		else {
			StringBuilder sb = new StringBuilder();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			
			System.out.print(sb.toString());
			return b;
		}
	}

	@Override
	public void run() 
	{
		try {
			upload();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			outputLine(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	public EV3ScpUpload(String hostAddress, String fileTo, String fileFrom, boolean doRun, boolean doDebug)
	{
		host = hostAddress;
		to = fileTo;
		from = fileFrom;
		run = doRun;
		debug = doDebug;
	}
	
	public abstract void outputLine(String line);
}
