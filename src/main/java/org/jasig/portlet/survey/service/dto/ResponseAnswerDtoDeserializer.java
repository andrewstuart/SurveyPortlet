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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Custom deserializer for {@link ResponseAnswerDTO}.
 *
 * @since 1.1
 */
public final class ResponseAnswerDtoDeserializer extends JsonDeserializer<ResponseAnswerDTO> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public ResponseAnswerDTO deserialize(JsonParser parser, DeserializationContext context)
    throws IOException, JsonProcessingException {
        log.debug("deserializing responseAnswer JSON");
        JsonNode node = parser.getCodec().readTree(parser);
        ResponseAnswerDTO dto = new ResponseAnswerDTO();

        JsonNode questionNode = node.get("question");
        log.debug(questionNode.toString());
        if (questionNode != null && questionNode.canConvertToLong()) {
            Long questionId = questionNode.asLong();
            dto.setQuestion(questionId);
        } else {
            throw new IllegalArgumentException("ResponseAnswer Json missing/bad question field");
        }

        JsonNode answerNode = node.get("answer");
        if (answerNode == null) {
            throw new IllegalArgumentException("ResponseAnswer Json missing answer field");
        }
        if (answerNode.canConvertToLong()) {
            dto.addAnswerId(answerNode.asLong());
        } else if (answerNode.isObject()) {
            for (Iterator<Entry<String, JsonNode>> fields = answerNode.fields(); fields.hasNext(); ) {
                Entry<String, JsonNode> field = fields.next();
                Long answerId = Long.parseLong(field.getKey());
                assert(field.getValue().isBoolean());
                boolean answerSelected = field.getValue().asBoolean(false);
                if (answerSelected) {
                    dto.addAnswerId(answerId);
                }
            }
        } else {
            throw new IllegalArgumentException("ResponseAnswer Json bad answer argument field");
        }

        return dto;
    }
}
