package org.contineo.web.ws;

import java.io.File;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;

/**
 * A stub implementation with support for attachment upload
 * 
 * @author Marco Meschieri
 * @version $Id:$
 * @since 3.0
 */
public class DmsStubWA extends DmsStub {
	public DmsStubWA(ConfigurationContext configurationContext) throws AxisFault {
		super(configurationContext);
	}

	public DmsStubWA(org.apache.axis2.context.ConfigurationContext configurationContext,
			java.lang.String targetEndpoint, boolean useSeparateListener) throws org.apache.axis2.AxisFault {
		super(configurationContext, targetEndpoint, useSeparateListener);
	}

	public DmsStubWA() throws AxisFault {
		super();
	}

	public DmsStubWA(ConfigurationContext configurationContext, String targetEndpoint) throws AxisFault {
		super(configurationContext, targetEndpoint);
	}

	public DmsStubWA(String targetEndpoint) throws AxisFault {
		super(targetEndpoint);
	}

	public org.contineo.web.ws.DmsStub.CreateDocumentResponse createDocument(
			org.contineo.web.ws.DmsStub.CreateDocument createDocument0, File file) throws java.rmi.RemoteException,
			org.contineo.web.ws.ExceptionException0 {
		try {
			Options options = new Options();
			options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
			options.setSoapVersionURI(_serviceClient.getOptions().getSoapVersionURI());
			// Increase the time out when sending large attachments
			options.setTimeOutInMilliSeconds(10000);
			options.setTo(_serviceClient.getOptions().getTo());
			options.setAction(_serviceClient.getOptions().getAction());

			ServiceClient sender = new ServiceClient(null, null);
			sender.setOptions(options);
			OperationClient _operationClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);

			_operationClient.getOptions().setAction("urn:createDocument");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

			addPropertyToOperationClient(_operationClient,
					org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

			// create a message context
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), createDocument0,
					optimizeContent(new javax.xml.namespace.QName("http://ws.web.contineo.org", "createDocument")));

			// adding SOAP soap_headers
			sender.addHeadersToEnvelope(env);

			// set the message context with that soap envelope
			_messageContext.setEnvelope(env);

			// Add the 'document' attachment
			DataHandler dataHandler = new DataHandler(new FileDataSource(file));
			_messageContext.addAttachment("document", dataHandler);

			// add the message context to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

			java.lang.Object object = null;
			try {
				object = org.contineo.web.ws.DmsStub.CreateDocumentResponse.Factory.parse(_returnEnv.getBody()
						.getFirstElement().getXMLStreamReaderWithoutCaching());
			} catch (java.lang.Exception e) {
				throw org.apache.axis2.AxisFault.makeFault(e);
			}

			_messageContext.getTransportOut().getSender().cleanup(_messageContext);

			return (org.contineo.web.ws.DmsStub.CreateDocumentResponse) object;
		} catch (org.apache.axis2.AxisFault f) {
			throw f;
		}
	}

	public org.contineo.web.ws.DmsStub.CheckinResponse checkin(org.contineo.web.ws.DmsStub.Checkin checkin, File file)
			throws java.rmi.RemoteException, org.contineo.web.ws.ExceptionException0 {
		try {
			Options options = new Options();
			options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
			options.setSoapVersionURI(_serviceClient.getOptions().getSoapVersionURI());
			// Increase the time out when sending large attachments
			options.setTimeOutInMilliSeconds(10000);
			options.setTo(_serviceClient.getOptions().getTo());
			options.setAction(_serviceClient.getOptions().getAction());

			ServiceClient sender = new ServiceClient(null, null);
			sender.setOptions(options);
			OperationClient _operationClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);

			_operationClient.getOptions().setAction("urn:checkin");
			_operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

			addPropertyToOperationClient(_operationClient,
					org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");

			// create a message context
			org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

			// create SOAP envelope with that payload
			org.apache.axiom.soap.SOAPEnvelope env = null;

			env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()), checkin,
					optimizeContent(new javax.xml.namespace.QName("http://ws.web.contineo.org", "checkin")));

			// adding SOAP soap_headers
			sender.addHeadersToEnvelope(env);

			// set the message context with that soap envelope
			_messageContext.setEnvelope(env);

			// Add the 'document' attachment
			DataHandler dataHandler = new DataHandler(new FileDataSource(file));
			_messageContext.addAttachment("document", dataHandler);

			// add the message context to the operation client
			_operationClient.addMessageContext(_messageContext);

			// execute the operation client
			_operationClient.execute(true);

			org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient
					.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

			java.lang.Object object = null;
			try {
				object = org.contineo.web.ws.DmsStub.CheckinResponse.Factory.parse(_returnEnv.getBody()
						.getFirstElement().getXMLStreamReaderWithoutCaching());
			} catch (java.lang.Exception e) {
				throw org.apache.axis2.AxisFault.makeFault(e);
			}

			_messageContext.getTransportOut().getSender().cleanup(_messageContext);

			return (org.contineo.web.ws.DmsStub.CheckinResponse) object;
		} catch (org.apache.axis2.AxisFault f) {
			throw f;
		}
	}
}
