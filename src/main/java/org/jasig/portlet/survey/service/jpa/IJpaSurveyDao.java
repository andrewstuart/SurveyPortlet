/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.survey.service.jpa;

import java.util.List;

/**
 * DAO Interface for all survey related database operations.
 * 
 * @since 1.0
 */
public interface IJpaSurveyDao {
    public JpaSurveyQuestion attachQuestionToSurvey(Long survey, Long question, JpaSurveyQuestion surveyQuestion);

    public JpaAnswer createAnswer(JpaAnswer answer);
    public JpaQuestion createQuestion(JpaQuestion question);
    public JpaQuestionAnswer createQuestionAnswer(JpaQuestion question, JpaAnswer answer, Integer sequence);
    public JpaSurvey createSurvey(JpaSurvey survey);
    public JpaSurveyText createSurveyText(JpaSurveyText text);

    public JpaQuestion getQuestion(Long id);
    public JpaAnswer getAnswer(Long id);
    public List<JpaSurvey> getAllSurveys();
    public JpaSurvey getSurvey(Long id);
    public JpaSurvey getSurveyByCanonicalName(String canonicalName);
    public JpaSurveyText getText(String key, String variant);

    public JpaQuestion updateQuestion(JpaQuestion question);
    public JpaSurvey updateSurvey(JpaSurvey survey);

    JpaResponse createResponse(JpaResponse jpaResponse);

    JpaResponse getResponse(long id);

    List<JpaResponse> getResponseByUser(String user);

    JpaResponse getResponseByUserAndSurvey(String user, long surveyId);

    JpaResponse updateResponse(JpaResponse jpaResponse);

    List<JpaResponse> getResponseBySurvey(Long surveyId);
}
