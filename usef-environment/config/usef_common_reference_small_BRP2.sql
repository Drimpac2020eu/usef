--
-- Copyright 2015-2016 USEF Foundation
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
INSERT INTO BRP2_USEF_EXAMPLE_COM_BRP.COMMON_REFERENCE_OPERATOR(ID, DOMAIN) VALUES 
(-1, 'cro1.usef-example.com');
INSERT INTO BRP2_USEF_EXAMPLE_COM_BRP.METER_DATA_COMPANY(DOMAIN) VALUES 
('mdc1.usef-example.com');
INSERT INTO BRP2_USEF_EXAMPLE_COM_BRP.SYNCHRONISATION_CONNECTION(ID, ENTITY_ADDRESS, LAST_MODIFICATION_TIME, LAST_SYNCHRONISATION_TIME) VALUES 
(-20, 'ean.002030100000000054', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-19, 'ean.002030100000000053', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-18, 'ean.002030100000000052', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-17, 'ean.002030100000000051', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-16, 'ean.002030100000000050', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-15, 'ean.302030100000000044', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-14, 'ean.302030100000000043', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-13, 'ean.302030100000000042', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-12, 'ean.302030100000000041', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-11, 'ean.302030100000000040', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-10, 'ean.302030100000000039', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-9, 'ean.302030100000000038', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-8, 'ean.302030100000000037', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-7, 'ean.302030100000000036', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-6, 'ean.302030100000000035', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-5, 'ean.002020100000000014', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-4, 'ean.302020100000000011', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-3, 'ean.302020100000000010', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-2, 'ean.002010100000000004', TIMESTAMP '2015-05-21 08:00:43.0', NULL), 
(-1, 'ean.302010100000000002', TIMESTAMP '2015-05-21 08:00:43.0', NULL);
INSERT INTO BRP2_USEF_EXAMPLE_COM_BRP.SYNCHRONISATION_CONNECTION_STATUS(ID, SYNCHRONISATION_CONNECTION_STATUS, COMMON_REFERENCE_OPERATOR_ID, SYNCHRONISATION_CONNECTION_ID) VALUES 
(-20, 'MODIFIED', -1, -20), 
(-19, 'MODIFIED', -1, -19), 
(-18, 'MODIFIED', -1, -18), 
(-17, 'MODIFIED', -1, -17), 
(-16, 'MODIFIED', -1, -16), 
(-15, 'MODIFIED', -1, -15), 
(-14, 'MODIFIED', -1, -14), 
(-13, 'MODIFIED', -1, -13), 
(-12, 'MODIFIED', -1, -12), 
(-11, 'MODIFIED', -1, -11), 
(-10, 'MODIFIED', -1, -10), 
(-9, 'MODIFIED', -1, -9), 
(-8, 'MODIFIED', -1, -8), 
(-7, 'MODIFIED', -1, -7), 
(-6, 'MODIFIED', -1, -6), 
(-5, 'MODIFIED', -1, -5), 
(-4, 'MODIFIED', -1, -4), 
(-3, 'MODIFIED', -1, -3), 
(-2, 'MODIFIED', -1, -2), 
(-1, 'MODIFIED', -1, -1);