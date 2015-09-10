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
package org.jasig.portlet.survey.service.dto;

import java.io.Serializable;
import java.util.List;

import org.jasig.portlet.survey.PublishedState;

import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@ApiObject(name = "QuestionDTO")
public class QuestionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiObjectField
    private String altText;
    @ApiObjectField
    private String canonicalName;
    @ApiObjectField
    private String helpText;
    @ApiObjectField
    private long id;
    @ApiObjectField
    private List<QuestionAnswerDTO> questionAnswers;
    @ApiObjectField
    private PublishedState status;
    @ApiObjectField
    private String text;

    public String getAltText() {
        return altText;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public String getHelpText() {
        return helpText;
    }

    public long getId() {
        return id;
    }

    public List<QuestionAnswerDTO> getQuestionAnswers() {
        return questionAnswers;
    }

    public PublishedState getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setQuestionAnswers(List<QuestionAnswerDTO> jpaQuestionAnswers) {
        this.questionAnswers = jpaQuestionAnswers;
    }

    public void setStatus(PublishedState status) {
        this.status = status;
    }

    public void setText(String text) {
        this.text = text;
    }
}
