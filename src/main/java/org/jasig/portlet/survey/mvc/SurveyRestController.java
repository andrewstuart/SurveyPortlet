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

package org.jasig.portlet.survey.mvc;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.jasig.portlet.survey.mvc.service.ISurveyDataService;
import org.jasig.portlet.survey.service.dto.*;
import org.jasig.portlet.survey.service.report.ISurveyReportGenerator;
import org.jasig.portlet.survey.service.report.ISurveyReportMapper;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiBodyObject;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiPathParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Api(name = "SurveyPortlet services", description = "Methods for managing surveys")
@Controller
@RequestMapping(value = SurveyRestController.REQUEST_ROOT, produces = MediaType.APPLICATION_JSON_VALUE)
public class SurveyRestController {

    public static final String REQUEST_ROOT = "/v1/surveys";

    @Autowired
    private ISurveyDataService dataService;

    @Autowired
    private ISurveyReportMapper reportMapper;

    @Resource(name="viewOtherUsersResponseRoles")
    private List<String> viewOtherUsersResponseRoles;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Create a new question that is not associated with a survey.
     * <p>
     * Security:  Requires SURVEY_ADMIN.
     *
     * @param question
     * @return
     */
    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @ApiMethod(description = "Create a new question that is not associated with a survey.", responsestatuscode = "201 - Created")
    @RequestMapping(method = RequestMethod.POST, value = "/questions")
    public @ApiResponseObject ResponseEntity<QuestionDTO> addQuestion(@ApiBodyObject @RequestBody QuestionDTO question) {
        QuestionDTO newQuestion = dataService.createQuestion(question);
        return new ResponseEntity<>(newQuestion, HttpStatus.CREATED);
    }

    /**
     * Create a survey
     * <p>
     * Security:  Requires SURVEY_ADMIN.
     *
     * @param survey
     * @return
     */
    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @ApiMethod(description = "Create a survey", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.POST, value = "/")
    public @ApiResponseObject ResponseEntity<SurveyDTO> addSurvey(@ApiBodyObject @RequestBody SurveyDTO survey, Principal principal) {
        survey.setLastUpdateUser(principal.getName());
        SurveyDTO newSurvey = null;
        HttpStatus status = HttpStatus.CREATED;

        try {
            newSurvey = dataService.createSurvey(survey);
        }
        catch (Exception e) {
            status = HttpStatus.BAD_REQUEST;
            log.error("Error creating survey: " + survey.toString(), e);
        }

        return new ResponseEntity<>(newSurvey, status);
    }

    /**
     * Create a text group
     * <p>
     * Security:  Requires SURVEY_ADMIN.
     */
    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @ApiMethod(description = "Create a text group", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.POST, value = "/textGroup")
    public @ApiResponseObject ResponseEntity<ITextGroup> addTextGroup(@ApiBodyObject @RequestBody TextGroupImpl textGroup) {
        ITextGroup tg = dataService.createTextGroup(textGroup);
        return new ResponseEntity<>(tg, HttpStatus.CREATED);
    }

    /**
     * Search for all surveys
     * <p>
     * Security:  Requires SURVEY_USER.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod(description = "Fetch all surveys", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public @ApiResponseObject ResponseEntity<List<SurveyDTO>> getAllSurveys() {
        log.debug("Get all surveys");
        List<SurveyDTO> surveyDTOList = dataService.getAllSurveys();
        return new ResponseEntity<>(surveyDTOList, HttpStatus.OK);
    }

    /**
     * Fetch a survey by id
     * <p>
     * Security:  Requires SURVEY_USER.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod(description = "Fetch a survey by id", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.GET, value = "/{survey}")
    public @ApiResponseObject ResponseEntity<SurveyDTO> getSurvey(@ApiPathParam(name = "survey") @PathVariable Long survey) {
        log.debug("Get survey: " + survey);
        SurveyDTO surveyDTO = dataService.getSurvey(survey);
        return new ResponseEntity<>(surveyDTO, HttpStatus.OK);
    }

    /**
     * Fetch a survey by name
     * <p>
     * Security:  Requires SURVEY_USER.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod(description = "Fetch a survey by name", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.GET, value = "/surveyByName/{surveyName}")
    public ResponseEntity<SurveyDTO> getSurvey(@ApiPathParam(name = "surveyName") @PathVariable String surveyName) {
        log.debug("Get survey: " + surveyName);
        SurveyDTO surveyDTO = dataService.getSurveyByName(surveyName);
        return new ResponseEntity<>(surveyDTO, HttpStatus.OK);
    }

    /**
     * Search for questions/answers that are associated with the specified survey.
     * <p>
     * Security:  Requires SURVEY_USER.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod
    @RequestMapping(method = RequestMethod.GET, value = "/{survey}/questions")
    public ResponseEntity<List<SurveyQuestionDTO>> getSurveyQuestions(@ApiPathParam(name = "survey") @PathVariable Long survey) {
        log.debug("Get survey: " + survey);
        List<SurveyQuestionDTO> sqList = dataService.getSurveyQuestions(survey);
        return new ResponseEntity<>(sqList, HttpStatus.OK);
    }

    /**
     * Fetch method for getting text detail by key.
     * <p>
     * Security:  Requires SURVEY_USER.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod(description = "Fetch a text group by key", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.GET, value = "/textGroup/{textKey}")
    public ResponseEntity<ITextGroup> getTextGroup(@PathVariable String textKey) {
        log.debug("Get text group by key: " + textKey);
        ITextGroup textGroup = dataService.getTextGroup(textKey);
        return new ResponseEntity<>(textGroup, HttpStatus.OK);
    }

    /**
     * Associate an existing question to an existing survey
     * <p>
     * Security:  Requires SURVEY_ADMIN.
     */
    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @ApiMethod(description = "Associate an existing question to an existing survey", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.POST, value = "/{survey}/questions/{question}")
    public @ApiResponseObject ResponseEntity<Boolean> linkQuestionToSurvey(
                    @ApiPathParam(name = "survey") @PathVariable Long survey,
                    @ApiPathParam(name = "question") @PathVariable Long question,
                    @ApiBodyObject @RequestBody SurveyQuestionDTO surveyQuestion) {
        Boolean ret;
        HttpStatus status = HttpStatus.CREATED;

        try {
            ret = dataService.addQuestionToSurvey(survey, question, surveyQuestion);
        }
        catch (Exception e) {
            status = HttpStatus.BAD_REQUEST;
            ret = false;
            log.error("Error linking question to survey", e);
        }
        return new ResponseEntity<>(ret, status);
    }

    /**
     * Update a question
     * <p>
     * Security:  Requires SURVEY_ADMIN.
     */
    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @ApiMethod(description = "Update a question", responsestatuscode = "201 - Created")
    @RequestMapping(method = RequestMethod.PUT, value = "/questions/{questionId}")
    public ResponseEntity<QuestionDTO> updateQuestion(@ApiPathParam(name = "questionId") @PathVariable Long questionId,
                    @ApiBodyObject @RequestBody QuestionDTO question) {
        HttpStatus status = HttpStatus.CREATED;
        QuestionDTO updatedQuestion = null;

        try {
            question.setId(questionId);
            updatedQuestion = dataService.updateQuestion(question);

            if (updatedQuestion == null) {
                status = HttpStatus.BAD_REQUEST;
            }
        }
        catch (Exception e) {
            status = HttpStatus.BAD_REQUEST;
            log.error("Error linking question to survey", e);
        }

        return new ResponseEntity<>(updatedQuestion, status);
    }

    /**
     * Update survey
     * <p>
     * Security:  Requires SURVEY_ADMIN.
     */
    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @ApiMethod(description = "Update survey", responsestatuscode = "201 - Created")
    @RequestMapping(method = RequestMethod.PUT, value = "/{surveyId}")
    public ResponseEntity<SurveyDTO> updateSurvey(@ApiPathParam(name = "surveyId") @PathVariable Long surveyId,
                    @ApiBodyObject @RequestBody SurveyDTO survey, Principal principal) {
        survey.setLastUpdateUser(principal.getName());
        HttpStatus status = HttpStatus.CREATED;
        SurveyDTO updatedSurvey = null;

        try {
            survey.setId(surveyId);
            updatedSurvey = dataService.updateSurvey(survey);

            if (updatedSurvey == null) {
                status = HttpStatus.BAD_REQUEST;
            }
        }
        catch (Exception e) {
            status = HttpStatus.BAD_REQUEST;
            log.error("Error linking question to survey", e);
        }

        return new ResponseEntity<>(updatedSurvey, status);

    }

    /**
     * Fetch one's own answers for the specified survey
     * <p>
     * Security:  Requires SURVEY_USER.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod(description = "Fetch a user's answers for the specified survey", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.GET, value = "/surveyAnswers")
    public @ApiResponseObject ResponseEntity<ResponseDTO> getResponseBySurveyAndUser(
            @RequestParam("survey") Long surveyId,
            HttpServletRequest req, Principal principal) {

        log.debug("GET surveyAnswers with survey = {}", surveyId);
        ResponseDTO responseDTO = null;
        try {
            responseDTO = dataService.getResponseByUserAndSurvey(principal.getName(), surveyId);
            log.debug(responseDTO != null ? responseDTO.toString() : "response is null");
        } catch (Exception e) {
            log.error("Error retrieving all survey responses for user: " + principal.getName() + ", survey: " + surveyId, e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);

    }

    /**
     * Fetch a user's answers by id
     * <p>
     * Security:  Requires SURVEY_USER.  Users may GET their own ResponseDTO;
     * to obtain a ResponseDTO for another user, you must belong to one of
     * viewOtherUsersResponseRoles.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod(description = "Fetch a user's answers by id", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.GET, value = "/surveyAnswers/{responseId}")
    public @ApiResponseObject ResponseEntity<ResponseDTO> getResponse(
            @ApiPathParam(name = "responseId") @PathVariable Long responseId,
            HttpServletRequest req, Principal principal) {

        ResponseDTO responseDTO = dataService.getResponse(responseId);
        boolean allowed = false;  // default
        if (principal.getName().equals(responseDTO.getUser())) {
            // Accessing my own response;  this action is allowed
            allowed = true;
        } else {
            // Only allowed if user has one of viewOtherUsersResponseRoles
            for (String role : viewOtherUsersResponseRoles) {
                if (req.isUserInRole(role)) {
                    allowed = true;
                    break;
                }
            }
        }

        if (allowed) {
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    /**
     * Create a user's response (answers)
     * <p>
     * Security:  Requires SURVEY_USER.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod(description = "Create a user's response (answers)", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.POST, value = "/surveyAnswers")
    public @ApiResponseObject ResponseEntity<ResponseDTO> addResponse(
            @ApiBodyObject @RequestBody ResponseDTO response,
            Principal principal) {
        response.setUser(principal.getName());
        ResponseDTO newResponse = null;
        HttpStatus status = HttpStatus.CREATED;

        try {
            newResponse = dataService.createResponse(response);
        }
        catch (Exception e) {
            status = HttpStatus.BAD_REQUEST;
            log.error("Error creating user" + principal.getName() + " response (answers): " + response.toString(), e);
        }

        return new ResponseEntity<>(newResponse, status);
    }

    /**
     * Update user's answers
     * <p>
     * Security:  Requires SURVEY_USER.  Can only PUT data owned by the
     * currently authenticated Principal.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod(description = "Update user's answers", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.PUT, value = "/surveyAnswers/{responseId}")
    public @ApiResponseObject ResponseEntity<ResponseDTO> updateResponse(
            @ApiPathParam(name = "responseId") @PathVariable Long responseId,
            @ApiBodyObject @RequestBody String body,
            HttpServletRequest req, Principal principal) {

        log.debug(body);
        HttpStatus status = HttpStatus.CREATED;
        final ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO response = null;
        try {
            response = objectMapper.readValue(body, ResponseDTO.class);
        } catch (IOException e) {
            status = HttpStatus.BAD_REQUEST;
            log.error("Error parsing update to survey answers with id = " + responseId, e);
            return new ResponseEntity<>(response, status);
        }
        log.debug(response.toString());
        assert(responseId == response.getId());
        ResponseDTO existingResponse = dataService.getResponse(responseId);
        if (existingResponse == null) {
            status = HttpStatus.BAD_REQUEST;
            log.error("Error no response with id = " + responseId);
            return new ResponseEntity<>(response, status);
        }
        if (!existingResponse.getUser().equals(principal.getName())) {
            status = HttpStatus.BAD_REQUEST;
            log.error("Updated response user does not match existing response user");
            return new ResponseEntity<>(response, status);
        }
        ResponseDTO updatedResponse = null;
        try {
            updatedResponse = dataService.updateResponse(response);
        } catch (Exception e) {
            status = HttpStatus.BAD_REQUEST;
            log.error("Error updating user" + principal.getName() + " response (answers): " + response.toString(), e);
        }
        return new ResponseEntity<>(updatedResponse, status);
    }

    /**
     * Return summary of user responses for a survey.
     * <p>
     * Security:  Requires SURVEY_ADMIN.  (Should it?)
     */
    @PreAuthorize("hasRole('SURVEY_ADMIN')")
    @ApiMethod(description = "Return summary of user response for a survey", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.GET, value = "/{survey}/summary")
    public @ApiResponseObject ResponseEntity<SurveySummaryDTO> getSurveySummary(@ApiPathParam(name = "survey") @PathVariable Long survey) {
        SurveySummaryDTO summaryDTO = dataService.getSurveySummary(survey);
        log.debug(summaryDTO.toString());
        return new ResponseEntity<>(summaryDTO, HttpStatus.OK);
    }

    /**
     * Provides a visual (HTML) report based an individual user's responses to
     * a survey.  This report typically appears when a user finishes a survey,
     * and can be as simple or complex as needed.  Report generation supports
     * pluggable strategies.
     * <p>
     * Security:  Requires SURVEY_USER.  Users may GET their own report;  to
     * obtain a report for another user, you must belong to one of
     * viewOtherUsersResponseRoles.
     */
    @PreAuthorize("hasRole('SURVEY_USER')")
    @ApiMethod(description = "Fetch the post-survey report that was generated based on the user's answers", responsestatuscode = "201")
    @RequestMapping(method = RequestMethod.GET, value = "/surveyReport/{responseId}")
    public @ApiResponseObject ModelAndView getSurveyReport(
            @ApiPathParam(name = "responseId") @PathVariable Long responseId,
            HttpServletRequest req, Principal principal) {

        final ResponseDTO response = dataService.getResponse(responseId);
        boolean allowed = false;  // default
        if (principal.getName().equals(response.getUser())) {
            // Accessing my own response;  this action is allowed
            allowed = true;
        } else {
            // Only allowed if user has one of viewOtherUsersResponseRoles
            for (String role : viewOtherUsersResponseRoles) {
                if (req.isUserInRole(role)) {
                    allowed = true;
                    break;
                }
            }
        }

        if (allowed) {
            final SurveyDTO survey = dataService.getSurvey(response.getSurvey());
            ISurveyReportGenerator generator = reportMapper.getReportGenerator(survey);
            return generator.generateReport(survey, response);
        } else {
            throw new AccessDeniedException("Forbidden");
        }

    }

}
