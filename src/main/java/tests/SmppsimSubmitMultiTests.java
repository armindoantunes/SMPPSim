package tests;
import tests.exceptions.*;
import junit.framework.*;

import java.net.*;
import com.logica.smpp.*;
import com.logica.smpp.pdu.*;
import org.slf4j.LoggerFactory;

public class SmppsimSubmitMultiTests extends TestCase {

	String smppServiceType = "tests";
	String srcAddress = "12345";
	String smppAccountName = "smppclient";
	String smppPassword = "password";
	String smppSystemType = "tests";
	String smppAddressRange = "[0-9]";
	int numberOfDestination = 10;
	String[] destAddresses =
		{
			"447787123456",
			"447787123457",
			"447787123458",
			"447787123459",
			"447787123460",
			"447787123461",
			"447787123462",
			"447787123463",
			"447787123464",
			"448765786543" };

	String[] dodgyAddresses =
		{
			"447787123456",
			"abcdefghijkl", // dodgy
			"447787123458",
			"4477871aa459",	// dodgy
			"447787123460",
			"447787123461",
			"Z47787123462",	// dodgy
			"447787123463",
			"447787123464",
			"448765786543" };

	boolean txBound;
	boolean rxBound;
	Session session;
	String smppHost = "localhost";
	int smppPort = 2775;
	int smppAltPort1 = 2776;
    private static org.slf4j.Logger logger = LoggerFactory.getLogger("test");

//	private static Logger logger = Logger.getLogger("smppsim.tests");

	public SmppsimSubmitMultiTests() {
	}

	/*
	 * Condition: Basic submit_multi sent
	 * 
	 * Expected: No exceptions. ESME_ROK in response.
	 */

	public void test001SubmitMulti()
		throws BindTransmitterException, SubmitMultiFailedException, SocketException {
		logger.info("Attempting to establish transmitter session");
		Response resp = null;
		Connection conn;
		// get a transmitter session
		try {
			conn = new TCPIPConnection(smppHost, smppPort);
			session = new Session(conn);
//			Session.getDebug().activate();
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

		// now send a multi-message

		String messageid = "";
		try {
			SubmitMultiSM request = new SubmitMultiSM();
			SubmitMultiSMResp response;

			// input values and set some :-)
			logger.info("Setting destination addresses");
			for (int i = 0; i < numberOfDestination; i++) {
				request.addDestAddress(
					new DestinationAddress(destAddresses[i]));
			}
			// set other values
			logger.info("Setting request values");
			request.setServiceType(smppServiceType);
			request.setSourceAddr(srcAddress);
			request.setShortMessage("This message is from a submit_multi");

			// send the request
			logger.info("Sending submit_multi");
			response = session.submitMulti(request);
			logger.info("Got submit_multi response");
			messageid = response.getMessageId();
			logger.info("message_id=" + messageid);
			Assert.assertEquals(
				"SUBMIT_MULTI failed: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
		} catch (SocketException se) {
			logger.error("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new SubmitMultiFailedException();
		}

		// Now unbind and disconnect ready for the next test
		try {
			UnbindResp response = session.unbind();
		} catch (Exception e) {
			logger.error(
				"Unbind operation failed for TX session. " + e.getMessage());
		}
	}

	/*
	 * Condition: Basic submit_multi sent
	 * 
	 * Expected: No exceptions. ESME_ROK in response.
	 */

	public void test002SubmitMultiInvalidAddresses()
		throws BindTransmitterException, SubmitMultiFailedException, SocketException {
		logger.info("Attempting to establish transmitter session");
		Response resp = null;
		Connection conn;
		// get a transmitter session
		try {
			conn = new TCPIPConnection(smppHost, smppAltPort1);
			session = new Session(conn);
			// Session.getDebug().activate();
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

		// now send a multi-message

		try {
			SubmitMultiSM request = new SubmitMultiSM();
			SubmitMultiSMResp response;

			// input values and set some :-)
			logger.info("Setting destination addresses");
			for (int i = 0; i < numberOfDestination; i++) {
				request.addDestAddress(
					new DestinationAddress((byte) 1, (byte) 1, dodgyAddresses[i]));
			}
			// set other values
			logger.info("Setting request values");
			request.setServiceType(smppServiceType);
			request.setSourceAddr(srcAddress);
			request.setShortMessage("This message is from a submit_multi");

			// send the request
			logger.info("Sending submit_multi");
			response = session.submitMulti(request);
			logger.info("Got submit_multi response");
			response.getMessageId();
			Assert.assertEquals(
				"SUBMIT_MULTI failed: response was not ESME_ROK",
				Data.ESME_ROK,
				response.getCommandStatus());
			short failures = response.getNoUnsuccess();
			Assert.assertEquals("SUBMIT_MULTI failed: number of failures wrong. ",3,failures);
			UnsuccessSME u = response.getUnsuccessSME(0);
			Assert.assertEquals("SUBMIT_MULTI failed: first failure address wrong. ","abcdefghijkl",u.getAddress());
			u = response.getUnsuccessSME(1);
			Assert.assertEquals("SUBMIT_MULTI failed: second failure address wrong. ","4477871aa459",u.getAddress());
			u = response.getUnsuccessSME(2);
			Assert.assertEquals("SUBMIT_MULTI failed: third failure address wrong. ","Z47787123462",u.getAddress());
		} catch (SocketException se) {
			logger.error("Connection has dropped");
			throw se;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new SubmitMultiFailedException();
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