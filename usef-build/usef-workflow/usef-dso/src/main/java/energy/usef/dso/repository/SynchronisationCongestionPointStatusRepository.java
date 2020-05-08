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

package energy.usef.dso.repository;

import energy.usef.core.repository.BaseRepository;
import energy.usef.dso.model.*;

import javax.ejb.Stateless;
import javax.persistence.Query;

/**
 * Repository class for the {@link SynchronisationConnection} entity. This class provides methods to interact with the BRP database.
 */
@Stateless
public class SynchronisationCongestionPointStatusRepository extends BaseRepository<SynchronisationCongestionPointStatus> {

    /**
     * Count's the amount of statusses with a certain status Type.
     * 
     * @param statusType
     * @return
     */
    public Long countSynchronisationConnectionStatusWithStatus(SynchronisationConnectionStatusType statusType) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT count(scs) ");
        queryBuilder.append("FROM SynchronisationCongestionPointStatus scs ");
        queryBuilder.append("WHERE scs.status = :status ");

        Query query = entityManager.createQuery(queryBuilder.toString());
        query.setParameter("status", statusType);
        return (Long) query.getSingleResult();
    }

    /**
     * Deletes all the {@link SynchronisationCongestionPointStatus} entities.
     */
    public void deleteAll() {
        entityManager.createQuery("DELETE FROM SynchronisationCongestionPointStatus").executeUpdate();
    }

    /**
     * Deletes all the {@link SynchronisationCongestionPointStatus} objects for a {@Link synchronisationCongestionPoint}.
     *
     * @param synchronisationCongestionPoint
     */
    public void deleteFor (SynchronisationCongestionPoint synchronisationCongestionPoint) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM SynchronisationCongestionPointStatus scs ");
        queryBuilder.append("WHERE scs.synchronisationCongestionPoint = :synchronisationCongestionPoint");

        Query query = entityManager.createQuery(queryBuilder.toString());
        query.setParameter("synchronisationCongestionPoint", synchronisationCongestionPoint).executeUpdate();
    }

    /**
     * Deletes all the {@link SynchronisationCongestionPointStatus} objects for a {@Link CommonReferenceOperator}.
     *
     * @param commonReferenceOperator
     */
    public void deleteFor (CommonReferenceOperator commonReferenceOperator) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM SynchronisationCongestionPointStatus scs ");
        queryBuilder.append("WHERE scs.commonReferenceOperator = :commonReferenceOperator");

        Query query = entityManager.createQuery(queryBuilder.toString());
        query.setParameter("commonReferenceOperator", commonReferenceOperator).executeUpdate();
    }
}
