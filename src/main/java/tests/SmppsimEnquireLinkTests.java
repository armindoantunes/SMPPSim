package tests;
import tests.exceptions.*;
import junit.framework.*;

import java.net.*;
import com.logica.smpp.*;
import com.logica.smpp.pdu.*;
import org.slf4j.LoggerFactory;

public class SmppsimEnquireLinkTests extends TestCase {

	String smppServiceType = "tests";
	String srcAddress = "12345";
	String destAddress = "4477805432122";
	String smppAccountName = "smppclient";
	String smppPassword = "password";
	String smppSystemType = "tests";
	String smppAddressRange = "[0-9]";
	boolean txBound;
	boolean rxBound;
	Session session;
	String smppHost = "localhost";
	int smppPort = 2775;
	int smppAltPort1 = 2776;
    private static org.slf4j.Logger logger = LoggerFactory.getLogger("test");

//	private static Logger logger = Logger.getLogger("smppsim.tests");

	public SmppsimEnquireLinkTests() {
	}

	/*
	 * Condition: Basic enquire_link sent
	 * Expected: No exceptions. ESME_ROK in response.
	 */

	public void test001EnquireLink()
		throws BindTransmitterException, EnquireLinkFailedException, SocketException {
		logger.info("Attempting to establish transmitter session");
		Response resp = null;
		Connection conn;
		// get a transmitter session
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
			BindRequest breq = new BindTransmitter();
			breq.setSystemId(smppAccountName);
			breq.setPassword(smppPassword);
			breq.setInterfaceVersion((byte) 0x34);
			breq.setSystemType(smppSystemType);
			resp = session.bind(breq);
		} catch (Exception e) {
			logger.error(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			Assert.fail(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
			throw new BindTransmitterException(
				"Exception whilst setting up or executing bind transmitter. "
					+ e.getMessage());
		}
		Assert.assertEquals(
			"BindTransmitter failed: response was not ESME_ROK",
			Data.ESME_ROK,
			resp.getCommandStatus());
		logger.info("Established transmitter session successfully");

		// now send an EnquireLink
		try {
			EnquireLinkResp response = new EnquireLinkResp();
			response = session.enquireLink();
			Assert.assertEquals(
				"ENQUIRE_LINK failed: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
		} catch (SocketException se) {
			logger.error("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new EnquireLinkFailedException();
		}

		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.error(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

}