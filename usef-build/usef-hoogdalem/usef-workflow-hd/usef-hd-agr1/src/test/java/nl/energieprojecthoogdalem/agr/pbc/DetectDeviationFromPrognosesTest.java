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

package nl.energieprojecthoogdalem.agr.pbc;

import info.usef.agr.dto.ConnectionPortfolioDto;
import info.usef.agr.dto.PowerContainerDto;
import info.usef.agr.dto.UdiPortfolioDto;
import info.usef.agr.workflow.operate.deviation.DetectDeviationFromPrognosisStepParameter.IN;
import info.usef.agr.workflow.operate.deviation.DetectDeviationFromPrognosisStepParameter.OUT;
import info.usef.core.util.PtuUtil;
import info.usef.core.workflow.DefaultWorkflowContext;
import info.usef.core.workflow.WorkflowContext;
import info.usef.core.workflow.dto.PrognosisDto;
import info.usef.core.workflow.dto.PtuPrognosisDto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test to test the {@link DetectDeviationFromPrognoses}.
 */
public class DetectDeviationFromPrognosesTest {
    private static final int PTU_DURATION = 120; // minutes

    private DetectDeviationFromPrognoses detectDeviationFromPrognoses = new DetectDeviationFromPrognoses();

    @Test
    public void testInvoke()
    {
        WorkflowContext workflowContext = new DefaultWorkflowContext();

        LocalDateTime timestamp = new LocalDateTime().withHourOfDay(0)
                .withMinuteOfHour(20).withSecondOfMinute(1).withMillisOfSecond(1);

        int currentPtuIndex = PtuUtil.getPtuIndex(timestamp, PTU_DURATION);
        int numberOfPtusPerDay = PtuUtil.getNumberOfPtusPerDay(timestamp.toLocalDate(), PTU_DURATION);

        workflowContext.setValue(IN.PTU_DURATION.name(), PTU_DURATION);
        workflowContext.setValue(IN.PERIOD.name(), timestamp.toLocalDate());
        workflowContext.setValue(IN.CURRENT_PTU_INDEX.name(), currentPtuIndex);

        PrognosisDto prognosisDto = new PrognosisDto();

        for (int ptuIndex = 1; ptuIndex <= numberOfPtusPerDay; ptuIndex++) {
            PtuPrognosisDto ptuPrognosisDto = new PtuPrognosisDto();
            ptuPrognosisDto.setPtuIndex(BigInteger.valueOf(ptuIndex));
            ptuPrognosisDto.setPower(BigInteger.valueOf(550 + ptuIndex * 150));
            prognosisDto.getPtus().add(ptuPrognosisDto);
        }

        workflowContext.setValue(IN.CONNECTION_PORTFOLIO_DTO.name(), buildUdiPortfolio(timestamp.toLocalDate()));
        workflowContext.setValue(IN.USEF_IDENTIFIER.name(), "brp.usef-example.com");
        workflowContext.setValue(IN.LATEST_PROGNOSIS.name(), prognosisDto);

        workflowContext = detectDeviationFromPrognoses.invoke(workflowContext);

        @SuppressWarnings("unchecked")
        List<Integer> deviationIndexList = workflowContext.get(OUT.DEVIATION_INDEX_LIST.name(), List.class);

        Assert.assertNotNull(deviationIndexList);
        Assert.assertTrue( deviationIndexList.isEmpty());
    }

    private List<ConnectionPortfolioDto> buildUdiPortfolio(LocalDate period) {
        List<ConnectionPortfolioDto> portfolioDTOs = new ArrayList<>();

        for (int connectionCount = 1; connectionCount <= 5; connectionCount++) {
            final int finalConnectionCount = connectionCount;

            ConnectionPortfolioDto connectionPortfolioDTO = new ConnectionPortfolioDto("EAN." + connectionCount);

            UdiPortfolioDto udiPortfolio = new UdiPortfolioDto("endpoint:" + finalConnectionCount, 60, "");

            IntStream.rangeClosed(1, 24).forEach(dtuIndex -> {
                PowerContainerDto powerContainerDto = new PowerContainerDto(period, dtuIndex);

                BigInteger consumption = BigInteger.valueOf(((long) (Math.floor((dtuIndex + 1) / 2)) * 10 * finalConnectionCount));

                if (consumption.compareTo(BigInteger.ZERO) < 0) {
                    powerContainerDto.getForecast().setAverageConsumption(BigInteger.ZERO);
                    powerContainerDto.getForecast().setAverageProduction(BigInteger.ZERO);
                } else {
                    powerContainerDto.getForecast().setAverageConsumption(consumption);
                    powerContainerDto.getForecast().setAverageProduction(BigInteger.ZERO);
                }

                udiPortfolio.getUdiPowerPerDTU().put(dtuIndex, powerContainerDto);
            });

            connectionPortfolioDTO.getUdis().add(udiPortfolio);

            // add uncontrolled load on connections level
            for (int ptuIndex = 1; ptuIndex <= 12; ptuIndex++) {
                PowerContainerDto uncontrolledLoad = new PowerContainerDto(period, ptuIndex);
                uncontrolledLoad.getForecast().setUncontrolledLoad(BigInteger.valueOf(100));
                connectionPortfolioDTO.getConnectionPowerPerPTU().put(ptuIndex, uncontrolledLoad);
            }

            portfolioDTOs.add(connectionPortfolioDTO);
        }

        return portfolioDTOs;
    }

}
