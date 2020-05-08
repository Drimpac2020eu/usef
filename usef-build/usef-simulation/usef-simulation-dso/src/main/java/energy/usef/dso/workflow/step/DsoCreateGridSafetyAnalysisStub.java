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

package energy.usef.dso.workflow.step;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.io.IOException; //drimpac
import java.util.stream.IntStream;
import org.codehaus.jackson.JsonNode;  //Drimpac
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper; //Drimpac
import org.codehaus.jackson.type.TypeReference; //Drimpac
import java.io.BufferedReader; //Drimpac
import java.io.InputStreamReader; //Drimpac
import javax.net.ssl.HttpsURLConnection;//Drimpac
import java.net.URL; //Drimpac
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import energy.usef.dso.config.ConfigDso;
import energy.usef.dso.config.ConfigDsoParam;
import energy.usef.core.config.Config;
import energy.usef.core.workflow.WorkflowContext;
import energy.usef.core.workflow.WorkflowStep;
import energy.usef.core.workflow.dto.DispositionTypeDto;
import energy.usef.core.workflow.dto.PrognosisDto;
import energy.usef.core.workflow.dto.PtuPrognosisDto;
import energy.usef.dso.pbcfeederimpl.PbcFeederService;
import energy.usef.dso.workflow.dto.GridSafetyAnalysisDto;
import energy.usef.dso.workflow.dto.NonAggregatorForecastDto;
import energy.usef.dso.workflow.dto.PtuGridSafetyAnalysisDto;
import energy.usef.dso.workflow.dto.PtuNonAggregatorForecastDto;
import energy.usef.dso.workflow.validate.gridsafetyanalysis.CreateGridSafetyAnalysisStepParameter;
import energy.usef.pbcfeeder.dto.PbcPowerLimitsDto;

class PtuJson2  //Drimpac
{
  //  private int Index=0;
    private int Power=0;

  //  public int  getIndex() { return this.Index; }
  //  public void setIndex (int Index) { this.Index = Index; }

    public int  getPower() { return this.Power; }
    public void setPower (int Power) { this.Power = Power; }
}
/**
 * Implementation of a workflow step generating the Grid Safety Analysis. The step works as follows: - The step retrieves the
 * Non-Aggregator forecast and D-prognosis forecast - The step goes through the Non-Aggregator forecast, because this forecast
 * contains all possible values for congestion point, PTU date and PTU index - The step combines the forecasted power of the
 * Non-Aggregator forecast with the forecast in the D-Prognosis - Based on the prognosis, the disposition is determined: - A Max
 * load for a congestion point can be set. If the total forecasted power is within this value (+ or -), there is no congestion.
 * Available power is calculated based on the max load and used for the grid safety analysis. - When the forecasted power is not
 * within the max load, the requested power is calculated in order to reduce production or consumption. This value is added to the
 * grid safety analysis.
 */
public class DsoCreateGridSafetyAnalysisStub implements WorkflowStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsoCreateGridSafetyAnalysisStub.class);

    @Inject
    private PbcFeederService pbcFeederService;

    @Inject
    private Config config;

    @Inject
    private ConfigDso configDso;
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public WorkflowContext invoke(WorkflowContext context) {
        LOGGER.info("Grid Safety Analysis invoked with context: {}", context);

        String congestionPointEntityAddress = context.get(CreateGridSafetyAnalysisStepParameter.IN.CONGESTION_POINT_ENTITY_ADDRESS.name(), String.class);
        NonAggregatorForecastDto nonAggregatorForecastDto = context.get(CreateGridSafetyAnalysisStepParameter.IN.NON_AGGREGATOR_FORECAST.name(),
                NonAggregatorForecastDto.class);
        List<PrognosisDto> dPrognosisInputList = (List<PrognosisDto>) context.getValue(
                CreateGridSafetyAnalysisStepParameter.IN.D_PROGNOSIS_LIST.name());
        LocalDate period = context.get(CreateGridSafetyAnalysisStepParameter.IN.PERIOD.name(), LocalDate.class);

////////// drimpac
javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
new javax.net.ssl.HostnameVerifier(){

    public boolean verify(String hostname,
            javax.net.ssl.SSLSession sslSession) {
        //return hostname.equals("160.40.49.244");
         return hostname.equals("localhost");
    }
});
String eanIDs= "?congestionID=";
eanIDs += congestionPointEntityAddress;
 String hostD=config.getProperties().getProperty("HOST_DOMAIN");
        hostD=hostD.replace(".", "_");
        hostD=hostD.toUpperCase() + "_DSO";
        eanIDs +="&hostD="+hostD;
       String url = "https://localhost:9000/drimpac-aggregator/rest/api/v1/get_load_forecast" + eanIDs;
       
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

            
               // con.setSSLSocketFactory(SocketFactory());
   
   con.setRequestMethod("GET");
 
 

            StringBuilder content;

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {

                    content.append(line + "\n");
                }
            }
System.out.println("GRIDSAFETYANALYSIS");      
  System.out.println(content.toString());          

String temp2 = content.toString();
temp2=temp2.substring(27);
temp2=temp2.substring(0,temp2.length()-2);
System.out.println(temp2);
     ObjectMapper objectMapper = new ObjectMapper(); //Drimpac
       // PtuJson[] ptuJson = objectMapper.readValue(file, PtuJson[].class);  //Drimpac
        PtuJson2[] ptuJson = objectMapper.readValue(temp2, PtuJson2[].class);  //Drimpac


        PbcPowerLimitsDto powerLimitsDto = fetchPowerLimits(congestionPointEntityAddress);
        Map<Integer, BigInteger> totalLoadPerPtu = computeTotalLoad(nonAggregatorForecastDto, dPrognosisInputList);
        GridSafetyAnalysisDto gridSafetyAnalysisDto = buildGridSafetyAnalysis2(congestionPointEntityAddress, period, powerLimitsDto,
                totalLoadPerPtu,ptuJson);

        context.setValue(CreateGridSafetyAnalysisStepParameter.OUT.GRID_SAFETY_ANALYSIS.name(), gridSafetyAnalysisDto);
  }
       catch(IOException e) {
           LOGGER.info("Can't find prognosis file");
        e.printStackTrace();

          PbcPowerLimitsDto powerLimitsDto = fetchPowerLimits(congestionPointEntityAddress);
        Map<Integer, BigInteger> totalLoadPerPtu = computeTotalLoad(nonAggregatorForecastDto, dPrognosisInputList);
        GridSafetyAnalysisDto gridSafetyAnalysisDto = buildGridSafetyAnalysis(congestionPointEntityAddress, period, powerLimitsDto,
                totalLoadPerPtu);

        context.setValue(CreateGridSafetyAnalysisStepParameter.OUT.GRID_SAFETY_ANALYSIS.name(), gridSafetyAnalysisDto);

         }
       finally {

            con.disconnect();
        }

        return context;
    }

    private PbcPowerLimitsDto fetchPowerLimits(String congestionPointEntityAddress) {
        return pbcFeederService.getCongestionPointPowerLimits(congestionPointEntityAddress);
    }

    /**
     * Computes the total load from the Non-Aggregator forecast and the different available prognoses.
     *
     * @param nonAggregatorForecastDto {@link NonAggregatorForecastDto} the non-aggregator forecast.
     * @param prognoses                {@link List} of {@link PrognosisDto}.
     * @return a {@link Map} with the PTU index as key ({@link Integer}) and the total load as value ({@link BigInteger}).
     */
    private Map<Integer, BigInteger> computeTotalLoad(NonAggregatorForecastDto nonAggregatorForecastDto,
                                                      List<PrognosisDto> prognoses) {
        Map<Integer, PtuNonAggregatorForecastDto> nonAggregatorForecastPerPtu = nonAggregatorForecastDto.getPtus()
                .stream()
                .collect(Collectors.toMap(PtuNonAggregatorForecastDto::getPtuIndex, Function.identity()));
        Map<Integer, Optional<BigInteger>> prognosisPowerPerPtu = prognoses.stream()
                .flatMap(prognosis -> prognosis.getPtus().stream())
                .collect(Collectors.groupingBy(ptuPrognosisDto -> ptuPrognosisDto.getPtuIndex().intValue(),
                        Collectors.mapping(PtuPrognosisDto::getPower, Collectors.reducing(BigInteger::add))));
        Map<Integer, BigInteger> totalPowerPerPtu = new HashMap<>();
        for (Integer ptuIndex : prognosisPowerPerPtu.keySet()) {
            totalPowerPerPtu.put(ptuIndex, prognosisPowerPerPtu.get(ptuIndex)
                    .orElse(BigInteger.ZERO)
                    .add(BigInteger.valueOf(nonAggregatorForecastPerPtu.get(ptuIndex).getPower())));
        }
        return totalPowerPerPtu;
    }

    private GridSafetyAnalysisDto buildGridSafetyAnalysis(String congestionPointEntityAddress, LocalDate period,
                                                          PbcPowerLimitsDto powerLimitsDto, Map<Integer, BigInteger> totalLoadPerPtu) {
        GridSafetyAnalysisDto gridSafetyAnalysisDto = new GridSafetyAnalysisDto();
        gridSafetyAnalysisDto.setEntityAddress(congestionPointEntityAddress);
        gridSafetyAnalysisDto.setPtuDate(period);
        for (Integer ptuIndex : totalLoadPerPtu.keySet()) {
            PtuGridSafetyAnalysisDto ptuGridSafetyAnalysisDto = new PtuGridSafetyAnalysisDto();
            ptuGridSafetyAnalysisDto.setPtuIndex(ptuIndex);
            DispositionTypeDto dispositionTypeDto = fetchDisposition(powerLimitsDto, totalLoadPerPtu.get(ptuIndex));
            BigInteger power = totalLoadPerPtu.get(ptuIndex); //fetchPowerValue(dispositionTypeDto, powerLimitsDto, totalLoadPerPtu.get(ptuIndex));
            LOGGER.trace("Disposition [{}] and power [{}]W for grid safety analysis for PTU [{}]", dispositionTypeDto, power,
                    ptuIndex);
            ptuGridSafetyAnalysisDto.setDisposition(dispositionTypeDto);
            ptuGridSafetyAnalysisDto.setPower(power.longValue());
            gridSafetyAnalysisDto.getPtus().add(ptuGridSafetyAnalysisDto);
        }
        return gridSafetyAnalysisDto;
    }

        private GridSafetyAnalysisDto buildGridSafetyAnalysis2(String congestionPointEntityAddress, LocalDate period,
                                                          PbcPowerLimitsDto powerLimitsDto, Map<Integer, BigInteger> totalLoadPerPtu,PtuJson2[] _ptuJson) {
        GridSafetyAnalysisDto gridSafetyAnalysisDto = new GridSafetyAnalysisDto();
        gridSafetyAnalysisDto.setEntityAddress(congestionPointEntityAddress);
        gridSafetyAnalysisDto.setPtuDate(period);
        Integer jsonINT = 0;
        for (Integer ptuIndex : totalLoadPerPtu.keySet()) {
            PtuGridSafetyAnalysisDto ptuGridSafetyAnalysisDto = new PtuGridSafetyAnalysisDto();
            ptuGridSafetyAnalysisDto.setPtuIndex(ptuIndex);
            DispositionTypeDto dispositionTypeDto = fetchDisposition(powerLimitsDto, totalLoadPerPtu.get(ptuIndex));
            BigInteger power = totalLoadPerPtu.get(ptuIndex); //fetchPowerValue(dispositionTypeDto, powerLimitsDto, totalLoadPerPtu.get(ptuIndex));
            LOGGER.trace("Disposition [{}] and power [{}]W for grid safety analysis for PTU [{}]", dispositionTypeDto, power,
                    ptuIndex);
            ptuGridSafetyAnalysisDto.setDisposition(dispositionTypeDto);
            BigInteger tmp = BigInteger.valueOf(_ptuJson[jsonINT].getPower());
            ptuGridSafetyAnalysisDto.setPower(tmp.longValue());
            jsonINT+=1;
            gridSafetyAnalysisDto.getPtus().add(ptuGridSafetyAnalysisDto);
        }
        return gridSafetyAnalysisDto;
    }

    /**
     * Fetches the required disposition for the grid safety analysis.
     * <p>
     * Disposition will be AVAILABLE if the total load is lesser than or equal to the Consumption Limit (Upper Limit) or if the
     * total load is bigger than or equal to the Production limit (Lower Limit).
     *
     * @param powerLimitsDto {@link PbcPowerLimitsDto} the power limits.
     * @param totalLoad      {@link BigInteger} the total load for the PTU.
     * @return {@link DispositionTypeDto} the disposition for the PTU.
     */
    private DispositionTypeDto fetchDisposition(PbcPowerLimitsDto powerLimitsDto, BigInteger totalLoad) {
        //drimpac
        //if (totalLoad.compareTo(powerLimitsDto.getUpperLimit().toBigInteger()) != 1
         //       && totalLoad.compareTo(powerLimitsDto.getLowerLimit().toBigInteger()) != -1) {
         //   return DispositionTypeDto.AVAILABLE;
        //}
        // usef is uncomment
        return DispositionTypeDto.REQUESTED;
    }

    /**
     * Fetches the required power value for the grid safety analysis.
     * <p>
     * If the disposition is AVAILABLE, power will be the difference between Consumption Limit and Total Load (if load bigger
     * than 0) or the difference between Production Limit and Total Load (if load lesser than or equal to 0).
     * <p>
     * If the disposition is REQUESTED, power will be the difference between Total Load and Consumption Limit (if load bigger
     * than Consumption Limit) or the difference between Total Load and Production Limit (if load lesser than Production Limit).
     *
     * @param disposition    {@link DispositionTypeDto} the already known disposition for the PTU of the grid safety analysis.
     * @param powerLimitsDto {@link PbcPowerLimitsDto} the power limits.
     * @param totalLoad      {@link BigInteger} the total load for the PTU.
     * @return a {@link BigInteger} which is the power for the grid safety analysis.
     */
    private BigInteger fetchPowerValue(DispositionTypeDto disposition, PbcPowerLimitsDto powerLimitsDto, BigInteger totalLoad) {
        BigInteger power = null;
        if (disposition == DispositionTypeDto.AVAILABLE) {
            if (totalLoad.compareTo(BigInteger.ZERO) == 1) {
                power = powerLimitsDto.getUpperLimit().toBigInteger().subtract(totalLoad);
            } else {
                power = powerLimitsDto.getLowerLimit().toBigInteger().subtract(totalLoad);
            }
        }
        if (disposition == DispositionTypeDto.REQUESTED) {
            if (totalLoad.compareTo(powerLimitsDto.getUpperLimit().toBigInteger()) == 1) {
                power = totalLoad.subtract(powerLimitsDto.getUpperLimit().toBigInteger());
            } else {
                power = totalLoad.subtract(powerLimitsDto.getLowerLimit().toBigInteger());
            }
        }
        return power;
    }

}
