/*
 * Copyright 2015-2016 USEF Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package energy.usef.dso.controller;

import static energy.usef.core.data.xml.bean.message.MessagePrecedence.ROUTINE;

import energy.usef.core.config.Config;
import energy.usef.core.config.ConfigParam;
import energy.usef.core.controller.BaseIncomingMessageController;
import energy.usef.core.data.xml.bean.message.DispositionAcceptedRejected;
import energy.usef.core.data.xml.bean.message.FlexOffer;
import energy.usef.core.data.xml.bean.message.FlexOfferResponse;
import energy.usef.core.data.xml.bean.message.MessageMetadata;
import energy.usef.core.data.xml.bean.message.USEFRole;
import energy.usef.core.exception.BusinessException;
import energy.usef.core.exception.BusinessValidationException;
import energy.usef.core.model.DocumentStatus;
import energy.usef.core.model.DocumentType;
import energy.usef.core.model.Message;
import energy.usef.core.model.PlanboardMessage;
import energy.usef.core.model.PtuContainerState;
import energy.usef.core.service.business.CorePlanboardBusinessService;
import energy.usef.core.service.helper.JMSHelperService;
import energy.usef.core.service.helper.MessageMetadataBuilder;
import energy.usef.core.service.validation.CorePlanboardValidatorService;
import energy.usef.core.util.XMLUtil;
import energy.usef.dso.workflow.coloring.ColoringProcessEvent;


import java.io.IOException; //drimpac
import java.util.stream.IntStream;
import org.codehaus.jackson.JsonNode;  //Drimpac
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper; //Drimpac
import org.codehaus.jackson.type.TypeReference; //Drimpac
import org.json.JSONObject;
import org.json.JSONException;
import org.json.XML;
import java.io.BufferedReader; //Drimpac
import java.io.InputStreamReader; //Drimpac
import java.io.OutputStream;
import javax.net.ssl.HttpsURLConnection;//Drimpac
import java.net.URL; //Drimpac
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import energy.usef.core.post.PostUsef;

class PostOffer
{
private String Sequence;
 private String CongestionPoint;
 private String FlexRequestSequence;
 private String Result;
 private String Aggregator;
 private String Dso;


 // Getter Methods 

 public String getSequence() {
  return Sequence;
 }

 public String getCongestionPoint() {
  return CongestionPoint;
 }

 public String getFlexRequestSequence() {
  return FlexRequestSequence;
 }

 public String getResult() {
  return Result;
 }

 public String getAggregator() {
  return Aggregator;
 }

 public String getDso() {
  return Dso;
 }

 // Setter Methods 

 public void setSequence(String Sequence) {
  this.Sequence = Sequence;
 }

 public void setCongestionPoint(String CongestionPoint) {
  this.CongestionPoint = CongestionPoint;
 }

 public void setFlexRequestSequence(String FlexRequestSequence) {
  this.FlexRequestSequence = FlexRequestSequence;
 }

 public void setResult(String Result) {
  this.Result = Result;
 }

 public void setAggregator(String Aggregator) {
  this.Aggregator = Aggregator;
 }

 public void setDso(String Dso) {
  this.Dso = Dso;
 }

}
/**
 * Incoming FlexRequestResponse controller.
 */
@Stateless
@Transactional
public class FlexOfferController extends BaseIncomingMessageController<FlexOffer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlexOfferController.class);

    @Inject
    private JMSHelperService jmsService;

    @Inject
    private CorePlanboardBusinessService corePlanboardBusinessService;

    @Inject
    private CorePlanboardValidatorService corePlanboardValidatorService;

    @Inject
    private Event<ColoringProcessEvent> coloringEventManager;

    @Inject
    private Config config;

    /**
     * {@inheritDoc}
     */
    @Override
    public void action(FlexOffer flexOffer, Message savedMessage) throws BusinessException {
        LOGGER.info("FlexOffer received.");
        PostUsef.Post("FlexOffer received.",flexOffer.getCongestionPoint());
        try {
            PlanboardMessage flexRequestMessage = corePlanboardValidatorService
                    .validatePlanboardMessageExpirationDate(flexOffer.getFlexRequestSequence(), DocumentType.FLEX_REQUEST,
                            flexOffer.getMessageMetadata().getSenderDomain());

            // do some extra validation
            corePlanboardValidatorService.validateCurrency(flexOffer.getCurrency());
            corePlanboardValidatorService.validateTimezone(flexOffer.getTimeZone());
            corePlanboardValidatorService.validatePTUDuration(flexOffer.getPTUDuration());
            if (!flexOffer.getPTU().isEmpty()) {
                corePlanboardValidatorService.validatePTUsForPeriod(flexOffer.getPTU(), flexOffer.getPeriod(), false);
            }
            corePlanboardValidatorService.validateDomain(flexOffer.getFlexRequestOrigin());

            // The Period the offer applies to should have at least one PTU that is not already pending settlement.
            String usefIdentifier = flexOffer.getCongestionPoint();
            corePlanboardValidatorService.validateIfPTUForPeriodIsNotInPhase(usefIdentifier, flexOffer.getPeriod(),
                    PtuContainerState.PendingSettlement, PtuContainerState.Settled);

            // store the flex offer
            corePlanboardBusinessService.storeFlexOffer(usefIdentifier, flexOffer, DocumentStatus.ACCEPTED,
                    flexOffer.getMessageMetadata().getSenderDomain());

            // update status (RECEIVED_OFFER or RECEIVED_EMPTY_OFFER) of the flex requests
            updateFlexRequestsStatus(flexOffer, flexRequestMessage);

            // eventually start the coloring process
            if (isAllowedToStartColoringProcess(flexOffer)) {
                startColoringProcess(flexOffer);
            }

            // send response
            sendResponse(flexOffer, null);
        } catch (BusinessValidationException exception) {
            sendResponse(flexOffer, exception);
        }

    }

    /**
     * Check if the coloring process needs to be called. Only needed if there are only non-processed FLEX_REQUESTS with
     * DocumentStatus RECEIVED_EMPTY_OFFER for the same day and congestion point.
     * 
     * @param flexOffer
     * @return
     */
    private boolean isAllowedToStartColoringProcess(FlexOffer flexOffer) {
        // retrieve a list with all flex requests planboard messages of the same day and congestion point.
        List<PlanboardMessage> flexRequests = corePlanboardBusinessService.findPlanboardMessagesForConnectionGroup(
                flexOffer.getCongestionPoint(), null, DocumentType.FLEX_REQUEST, flexOffer.getPeriod(), null);

        // remove all flex requests planboard messages with status RECEIVED_EMPTY_OFFER or REJECTED.
        List<PlanboardMessage> flexRequestsWithOffers = flexRequests
                .stream()
                .filter(flexRequest -> flexRequest.getDocumentStatus() != DocumentStatus.RECEIVED_EMPTY_OFFER
                        && flexRequest.getDocumentStatus() != DocumentStatus.REJECTED)
                .collect(Collectors.toList());

        return flexRequestsWithOffers.isEmpty();
    }

    /**
     * Start the coloring process to determine if PTU(s) become orange.
     */
    private void startColoringProcess(FlexOffer flexOffer) {
        coloringEventManager.fire(new ColoringProcessEvent(flexOffer.getPeriod(), flexOffer.getCongestionPoint()));
    }

    /**
     * Updates planboard message with status RECEIVED_OFFER (flex offer received with PTU data available) or RECEIVED_EMPTY_OFFER
     * (empty flex offer received without PTU data).
     * 
     * @param flexOffer
     */
    private void updateFlexRequestsStatus(FlexOffer flexOffer, PlanboardMessage flexRequestMessage) {
        if (flexOffer.getPTU().isEmpty()) {
            flexRequestMessage.setDocumentStatus(DocumentStatus.RECEIVED_EMPTY_OFFER);
        } else {
            flexRequestMessage.setDocumentStatus(DocumentStatus.RECEIVED_OFFER);
        }
    }



    private void sendResponse(FlexOffer request, BusinessValidationException exception) {
        FlexOfferResponse response = new FlexOfferResponse();

        MessageMetadata messageMetadata = MessageMetadataBuilder
                .build(request.getMessageMetadata().getSenderDomain(), request.getMessageMetadata().getSenderRole(),
                        config.getProperty(ConfigParam.HOST_DOMAIN), USEFRole.DSO, ROUTINE)
                .conversationID(request.getMessageMetadata().getConversationID()).build();
        response.setMessageMetadata(messageMetadata);

String accepted = "Accepted";
        response.setSequence(request.getSequence());
        if (exception == null) {
            response.setResult(DispositionAcceptedRejected.ACCEPTED);
        } else {
            accepted = "Rejected";
            response.setResult(DispositionAcceptedRejected.REJECTED);
            response.setMessage(exception.getMessage());
        }

        ////////// drimpac
javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
new javax.net.ssl.HostnameVerifier(){

    public boolean verify(String hostname,
            javax.net.ssl.SSLSession sslSession) {
        //return hostname.equals("160.40.49.244");
         return hostname.equals("localhost");
    }
});

       String url = "https://localhost:9000/drimpac-aggregator/rest/api/v1/revoke_offer";
       
TrustManager[] trustAllCerts = new TrustManager[]{
    new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public void checkClientTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
        public void checkServerTrusted(
            java.security.cert.X509Certificate[] certs, String authType) {
        }
    }
};

// Install the all-trusting trust manager
try {
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
} catch (Exception e) {
}

HttpsURLConnection con = null;

  try 
       {
            URL myurl = new URL(url);
            con = (HttpsURLConnection) myurl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
PostOffer poffer = new PostOffer();
poffer.setSequence(String.valueOf(request.getSequence()));
poffer.setFlexRequestSequence(String.valueOf(request.getFlexRequestSequence()));
poffer.setResult(accepted);
poffer.setCongestionPoint(request.getCongestionPoint());
poffer.setAggregator(request.getMessageMetadata().getSenderDomain());
poffer.setDso(request.getMessageMetadata().getRecipientDomain());

ObjectMapper objectMapper = new ObjectMapper();
String json = objectMapper.writeValueAsString(poffer);
System.out.println(json);
OutputStream os = con.getOutputStream();
  //  byte[] input = json.getBytes("utf-8");
  //  os.write(input, 0, input.length);           
os.write(json.getBytes("utf-8"));
os.flush();
os.close();

            StringBuilder content;

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {

                    content.append(line + "\n");
                }
            }
            System.out.println(content.toString()); 
 }
       catch(IOException e) {
           LOGGER.info("send offer response");
        e.printStackTrace();
  }
       finally {

            con.disconnect();
        }

String url2 = "https://localhost:9000/drimpac-aggregator/rest/api/v1/insertflexoffer";
        HttpsURLConnection con2 = null;
 try 
       {
            URL myurl2 = new URL(url2);
            con2 = (HttpsURLConnection) myurl2.openConnection();
            con2.setRequestMethod("POST");
            con2.setRequestProperty("Content-Type", "application/json");
            con2.setRequestProperty("Accept", "application/json");
            con2.setDoOutput(true);

String xml  = XMLUtil.messageObjectToXml(request);
JSONObject jsonObj = XML.toJSONObject(xml,true);  

            ObjectMapper objectMapper2 = new ObjectMapper();
           // String json2 = objectMapper2.writeValueAsString(request);
String json2 = objectMapper2.writeValueAsString(jsonObj.toString());
String json3 = json2.replace("\\\"", "\"");
String json4 = json3.replace("PTU-Duration", "PTUDuration");
String json5 = json4.substring(1,json4.length()-1);
            System.out.println(json5);
OutputStream os2 = con2.getOutputStream();      
os2.write(json5.getBytes("utf-8"));
os2.flush();
os2.close();

 StringBuilder content2;

            try (BufferedReader in2 = new BufferedReader(
                    new InputStreamReader(con2.getInputStream()))) {

                String line2;
                content2 = new StringBuilder();

                while ((line2 = in2.readLine()) != null) {

                    content2.append(line2 + "\n");
                }
            }
            System.out.println(content2.toString()); 
 }
       catch(IOException e2) {
           LOGGER.info("send offer response");
        e2.printStackTrace();
  }
       finally {

            con2.disconnect();
        }


        // send the response xml to the out queue.
        jmsService.sendMessageToOutQueue(XMLUtil.messageObjectToXml(response));

        LOGGER.info("FlexOfferResponse with conversation-id {} is sent to the outgoing queue.",
                response.getMessageMetadata().getConversationID());
    }
}
