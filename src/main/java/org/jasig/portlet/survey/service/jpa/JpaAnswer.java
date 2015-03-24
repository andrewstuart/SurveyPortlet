/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.survey.service.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * The persistent class for the survey_answer database table.
 * 
 * @author chasegawa
 * @since 1.0
 */
@Entity
@Table(name = JpaSurveyService.TABLENAME_PREFIX + "answer")
class JpaAnswer implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "ALT_TEXT", nullable = true)
    private String altText;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private long id;

    @Column(name = "TEXT", nullable = false)
    private String text;

    public String getAltText() {
        return this.altText;
    }

    public long getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }
}