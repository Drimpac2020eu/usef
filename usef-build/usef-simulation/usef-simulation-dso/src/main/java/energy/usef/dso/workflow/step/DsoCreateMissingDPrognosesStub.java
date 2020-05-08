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


import energy.usef.dso.config.ConfigDso; //drimpac
import energy.usef.core.util.PtuUtil;
import energy.usef.core.workflow.WorkflowContext;
import energy.usef.core.workflow.WorkflowStep;
import energy.usef.core.workflow.dto.PrognosisDto;
import energy.usef.core.workflow.dto.PrognosisTypeDto;
import energy.usef.core.workflow.dto.PtuPrognosisDto;
import energy.usef.dso.workflow.validate.gridsafetyanalysis.CreateMissingDPrognosisParameter;
import energy.usef.dso.workflow.validate.gridsafetyanalysis.CreateMissingDPrognosisParameter.IN;

import java.math.BigInteger;
import java.util.*;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.io.File;
import java.io.IOException; //drimpac

import org.codehaus.jackson.JsonNode;  //Drimpac
import org.codehaus.jackson.map.ObjectMapper; //Drimpac
import org.codehaus.jackson.type.TypeReference; //Drimpac

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow step implementation for the Workflow 'Grid Safety Analysis workflow'. The step is responsible for missing D-Prognosis
 * creation. This implementation expects to find the following parameters as input:
 * <ul>
 * <li>CONGESTION_POINT_ENTITY_ADDRESS: The entity address of the congestion point ({@link String})</li>
 * <li>AGGREGATOR_DOMAIN: The aggregator domain ({@link String})</li>
 * <li>ANALYSIS_DAY: Analysis day ({@link LocalDate})</li>
 * <li>PTU_DURATION: PTU duration ({@link Integer})</li>
 * <li>AGGREGATOR_CONNECTION_AMMOUNT: Aggregator connection ammount ({@link Integer})</li>
 * </ul>
 * The step provides the following parameters as output:
 * <ul>
 * <li>D_PROGNOSIS: D-Prognosis DTO List ({@link List})</li>
 * </ul>
 * <p>
 * The step generates a replacement D-prognosis with a random power value for each PTU equal to M*N where M is the number of AGR
 * connections from the PBC input list and N a random value between -500 and 500.
 */
class PtuJson  //Drimpac
{
  //  private int Index=0;
    private int Power=0;

  //  public int  getIndex() { return this.Index; }
  //  public void setIndex (int Index) { this.Index = Index; }

    public int  getPower() { return this.Power; }
    public void setPower (int Power) { this.Power = Power; }
}



public class DsoCreateMissingDPrognosesStub implements WorkflowStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(DsoCreateMissingDPrognosesStub.class);
    private static final Random RANDOM = new Random();
    private static final int POWER_LIMIT = 501;

 
    private ConfigDso configDso;
    /**
     * {@inheritDoc}
     */
    public WorkflowContext invoke(WorkflowContext context) {
        String entityAddress = (String) context.getValue(IN.CONGESTION_POINT_ENTITY_ADDRESS.name());

        // Not used in this PBC STUB implementation but supposed to be used in the real PBC.
        String aggregatorDomain = (String) context.getValue(IN.AGGREGATOR_DOMAIN.name());

        LOGGER.info("Starting workflow step 'DSOCreateMissingPrognosis' for AGR: {}, congestion point: {}.", aggregatorDomain,
                entityAddress);

        //ObjectMapper objectMapper = new ObjectMapper(); //Drimpac
        //String fentinityAddress="\\prognois\\" + entityAddress.replaceAll(":", ".") + ".json"; //Drimpac
        //Sting tmp = this.getClass().getProtectionDomain().getCodeSource().getLocation().getParent().getParent().getParent().getParent().getFileName();
       // File file = new File(ConfigDso.getConfigurationFolder() + fentinityAddress);  //Drimpac
       // List<PtuJson> ptuJson = objectMapper.readValue(file, new TypeReference<List<PtuJson>>(){});  //Drimpac
       //LOGGER.info(tmp);
       //File file = new File(tmp + fentinityAddress);

       
     //  try 
     //  {
      //  PtuJson[] ptuJson = objectMapper.readValue(file, PtuJson[].class);  //Drimpac
       // LocalDate analysisDay = (LocalDate) context.getValue(IN.ANALYSIS_DAY.name());
        //int ptuDuration = (int) context.getValue(IN.PTU_DURATION.name());
       // int aggregatorConnectionNumber = (int) context.getValue(IN.AGGREGATOR_CONNECTION_AMOUNT.name());

        //int numberOfPtusPerDay = PtuUtil.getNumberOfPtusPerDay(analysisDay, ptuDuration);

        //PrognosisDto prognosisDto = new PrognosisDto();
        //prognosisDto.setConnectionGroupEntityAddress(entityAddress);
        //prognosisDto.setPeriod(analysisDay);
        //prognosisDto.setType(PrognosisTypeDto.D_PROGNOSIS);
        //IntStream.rangeClosed(1, numberOfPtusPerDay).mapToObj(ptuIndex -> {
         //   PtuPrognosisDto ptuPrognosisDto = new PtuPrognosisDto();
          //  ptuPrognosisDto.setPtuIndex(BigInteger.valueOf(ptuIndex));
            //ptuPrognosisDto.setPower(generateRandomPowerValue(aggregatorConnectionNumber));
           // ptuPrognosisDto.setPower(generatePowerValueFromFile(ptuJson[ptuIndex].getPower()));
            //return ptuPrognosisDto;
        //}).forEach(ptuPrognosisDto -> prognosisDto.getPtus().add(ptuPrognosisDto));

        //context.setValue(CreateMissingDPrognosisParameter.OUT.D_PROGNOSIS.name(), prognosisDto);
      // }
      // catch(IOException e) {
       //    LOGGER.info("Can't find prognosis file");
        //e.printStackTrace();
        LocalDate analysisDay = (LocalDate) context.getValue(IN.ANALYSIS_DAY.name());
        int ptuDuration = (int) context.getValue(IN.PTU_DURATION.name());
        int aggregatorConnectionNumber = (int) context.getValue(IN.AGGREGATOR_CONNECTION_AMOUNT.name());

        int numberOfPtusPerDay = PtuUtil.getNumberOfPtusPerDay(analysisDay, ptuDuration);

        PrognosisDto prognosisDto = new PrognosisDto();
        prognosisDto.setConnectionGroupEntityAddress(entityAddress);
        prognosisDto.setPeriod(analysisDay);
        prognosisDto.setType(PrognosisTypeDto.D_PROGNOSIS);
        IntStream.rangeClosed(1, numberOfPtusPerDay).mapToObj(ptuIndex -> {
            PtuPrognosisDto ptuPrognosisDto = new PtuPrognosisDto();
            ptuPrognosisDto.setPtuIndex(BigInteger.valueOf(ptuIndex));
            ptuPrognosisDto.setPower(generateRandomPowerValue(aggregatorConnectionNumber));
            //ptuPrognosisDto.setPower(generatePowerValueFromFile(ptuJson[ptuIndex].getPower()));
            return ptuPrognosisDto;
        }).forEach(ptuPrognosisDto -> prognosisDto.getPtus().add(ptuPrognosisDto));

        context.setValue(CreateMissingDPrognosisParameter.OUT.D_PROGNOSIS.name(), prognosisDto);
      // }

       
        return context;
    }

    private BigInteger generateRandomPowerValue(int aggregatorConnectionNumber) {
        return BigInteger.valueOf(aggregatorConnectionNumber * RANDOM.nextInt(POWER_LIMIT) * (RANDOM.nextBoolean() ? 1 : -1));
    }

     private BigInteger generatePowerValueFromFile(int value) {
        return BigInteger.valueOf(value);
    }

}
